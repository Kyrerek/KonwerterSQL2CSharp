package org.model;


import antlr.SQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class SQLtoCSharpVisitor extends antlr.SQLBaseVisitor<String> {
    //TODO - raczej inaczej to trzeba dla wielu query
    private boolean isGrouped = false;
    private List<String> groupedColumns = new ArrayList<String>();

    @Override
    public String visitQuery(SQLParser.QueryContext ctx){
        StringBuilder csharp = new StringBuilder();
        for (var query : ctx.select_stm()){
            csharp.append(visit(query)).append(";\n");
        }
        return csharp.toString();
    }

    @Override
    public String visitSelect_stm(SQLParser.Select_stmContext ctx){
        String tableName = visit(ctx.from_stm());
        StringBuilder csharp = new StringBuilder(tableName);

        // JOIN
        if (ctx.join_stm() != null) {
            for (var join_stmContext : ctx.join_stm()) {
                csharp.append(visit(join_stmContext));
            };
        }

        // WHERE
        if (ctx.where_stm() != null){
            csharp.append("\t\n.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }

        // GROUP BY
        if (ctx.groupby_stm() != null) {
            csharp.append("\t\n.GroupBy(temp => ").append(visit(ctx.groupby_stm())).append(')');
        }

        // HAVING
        if (ctx.having_stm() != null) {
            csharp.append("\t\n.Where(temp => ").append(visit(ctx.having_stm())).append(')');
        }

        // ORDER BY
        if (ctx.order_stm() != null) {
            csharp.append(visit(ctx.order_stm()));
        }

        // SELECT
        csharp.append("\t\n.Select(temp => ").append(visit(ctx.select_list())).append(')');

        return csharp.toString();
    }

    @Override
    public String visitSelect_list(SQLParser.Select_listContext ctx){
        if (ctx.MULT() != null) return "temp";

        StringBuilder csharp = new StringBuilder();
        List<antlr.SQLParser.Select_itemContext> cols = ctx.select_item();
        if (cols.size() == 1) csharp.append(visit(cols.getFirst()));
        else {
            csharp.append("new {");
            for (var col : cols) {
                csharp.append(visit(col));
                if (cols.indexOf(col) != cols.size() - 1) csharp.append(",\n");
            }
            csharp.append("}");
        }
        return csharp.toString();
    }

    @Override
    public String visitSelect_item(SQLParser.Select_itemContext ctx){
        StringBuilder csharp = new StringBuilder();

        if (ctx.AS() != null) csharp.append(ctx.ID().getText()).append(" = ");

        //TODO - ma wykrywać, że kolumna jest zgrupowana
        if (this.isGrouped) {
            return csharp.append("temp.Key").toString();
        }

        if (ctx.column() != null) csharp.append(visit(ctx.column()));
        else if (ctx.agg_func() != null) csharp.append(visit(ctx.agg_func()));

        return csharp.toString();
    }

    @Override
    public String visitColumn(SQLParser.ColumnContext ctx){
        if (ctx.PER() != null){
            List<TerminalNode> ids = ctx.ID();
            return "temp."+ids.get(0).getText()+'.'+ids.get(1).getText();
        }
        return "temp."+ctx.getText();
    }

    //TODO - obsługiwanie "*"
    @Override
    public String visitAgg_func(SQLParser.Agg_funcContext ctx){
        if (ctx.MIN() != null) return "temp.Min(s => s."+visit(ctx.column())+")";

        if (ctx.MAX() != null) return "temp.Max(s => s."+visit(ctx.column())+")";

        if (ctx.COUNT() != null) return "temp.Count()";

        if (ctx.SUM() != null) return "temp.Sum(s => s."+visit(ctx.column())+")";

        if (ctx.AVG() != null) return "temp.Average(s => s."+visit(ctx.column())+")";
    }

    //TODO - zweryfikować, czy tyle starczy
    @Override
    public String visitWhere_stm(SQLParser.Where_stmContext ctx){
        return visit(ctx.logic_form());
    }

    @Override
    public String visitFrom_stm(SQLParser.From_stmContext ctx){
        return ctx.ID().getText();
    }

}
