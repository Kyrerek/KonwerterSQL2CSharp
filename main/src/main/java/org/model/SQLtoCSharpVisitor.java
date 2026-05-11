package org.model;


import antlr.SQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import static antlr.SQLParser.*;

public class SQLtoCSharpVisitor extends antlr.SQLBaseVisitor<String> {
    //TODO - raczej inaczej to trzeba dla wielu query
    private boolean isGrouped = false;
    private List<String> groupedColumns = new ArrayList<String>();

    @Override
    public String visitQuery(SQLParser.QueryContext ctx){
        StringBuilder csharp = new StringBuilder();
        for (var query : ctx.operation()){
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
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }

        // GROUP BY
        if (ctx.groupby_stm() != null) {
            csharp.append("\n\t.GroupBy(temp => ").append(visit(ctx.groupby_stm())).append(')');
        }

        // HAVING
        if (ctx.having_stm() != null) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.having_stm())).append(')');
        }

        // ORDER BY
        if (ctx.order_stm() != null) {
            csharp.append(visit(ctx.order_stm()));
        }

        // SELECT
        csharp.append("\n\t.Select(temp => ").append(visit(ctx.select_list())).append(')');

        // DISTINCT
        if (ctx.DISTINCT() != null) csharp.append("\n\t.Distinct()");

        return csharp.toString();
    }

    @Override
    public String visitSelect_list(SQLParser.Select_listContext ctx){
        if (ctx.MULT() != null) return "temp";

        StringBuilder csharp = new StringBuilder();
        List<SQLParser.Select_itemContext> cols = ctx.select_item();
        if (cols.size() == 1) csharp.append(visit(cols.getFirst()));
        else {
            csharp.append("new {");
            for (var col : cols) {
                csharp.append(visit(col));
                if (cols.indexOf(col) != cols.size() - 1) csharp.append(", ");
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

        return null;
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


    //Logic Form
    @Override
    public String visitLogic_form(SQLParser.Logic_formContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_or(SQLParser.Logic_orContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_and(SQLParser.Logic_andContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_not(SQLParser.Logic_notContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_atom(SQLParser.Logic_atomContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_cmp(SQLParser.Logic_cmpContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_simple_cmp(SQLParser.Logic_simple_cmpContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitLogic_between_cmp(SQLParser.Logic_between_cmpContext ctx) {
        StringBuilder csharp = new StringBuilder();
        String mainStr = visit(ctx.getChild(0));
        if (ctx.getChildCount() == 6){
            String lowerStr = visit(ctx.getChild(3));
            String higherStr = visit(ctx.getChild(5));
            csharp.append(mainStr);
            csharp.append(" < ");
            csharp.append(lowerStr);
            csharp.append(" && ");
            csharp.append(mainStr);
            csharp.append(" > ");
            csharp.append(higherStr);
        }else{
            String lowerStr = visit(ctx.getChild(2));
            String higherStr = visit(ctx.getChild(4));
            csharp.append(mainStr);
            csharp.append(" >= ");
            csharp.append(lowerStr);
            csharp.append(" && ");
            csharp.append(mainStr);
            csharp.append(" <= ");
            csharp.append(higherStr);
        }
        return csharp.toString();
    }

    @Override
    public String visitLogic_like_cmp(SQLParser.Logic_like_cmpContext ctx) {
        StringBuilder csharp = new StringBuilder();
        String colStr = visit(ctx.getChild(0));
        if (ctx.getChildCount() == 3){
            String str = ctx.getChild(2).getText();
            csharp.append(" == ");
            csharp.append(str);
        }else{
            String str = ctx.getChild(3).getText();
            csharp.append(" != ");
            csharp.append(str);
        }
        return csharp.toString();
    }

    @Override
    public String visitLogic_null_cmp(SQLParser.Logic_null_cmpContext ctx) {
        StringBuilder csharp = new StringBuilder();
        String colStr = visit(ctx.getChild(0));
        if (ctx.getChildCount() == 3){
            csharp.append(" == ");
        }else{
            csharp.append(" != ");
        }
        csharp.append("null");
        return csharp.toString();
    }

    @Override
    public String visitItem_form(SQLParser.Item_formContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitItem_plus_minus(SQLParser.Item_plus_minusContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitItem_multi_div(SQLParser.Item_multi_divContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitItem_atom(SQLParser.Item_atomContext ctx) {
        return simpleChangeTerm(ctx);
    }

    @Override
    public String visitItem(SQLParser.ItemContext ctx) {
        System.out.println(ctx.getText());
        return simpleChangeTerm(ctx);
    }

    public String simpleChangeTerm(ParserRuleContext ctx){
        StringBuilder csharp = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode termChild) {
                int termType = termChild.getSymbol().getType();
                if (termType == NUM || termType == INT || termType == STR) {
                    System.out.println(termChild.getText());
                    csharp.append(termChild.getText());
                }else{
                    System.out.println(SymbolMapper.getSymbolsMap().get(termType));
                    csharp.append(SymbolMapper.getSymbolsMap().get(termType));
                }
            } else if (child instanceof ParserRuleContext logicChild) {
                csharp.append(visit(logicChild));
            }
        }
        System.out.println(csharp.toString());
        return csharp.toString();
    }
}

