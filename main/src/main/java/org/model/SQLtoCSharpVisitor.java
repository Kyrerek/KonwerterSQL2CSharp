package org.model;


import antlr.SQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

import static antlr.SQLParser.*;

public class SQLtoCSharpVisitor extends antlr.SQLBaseVisitor<String> {
    private boolean isGrouped;
    private List<String> groupedColumns;
    private final String anonName = "temp";
    private int tablesToJoin;
    private List<String> prevJoins;
    private final List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String visitQuery(SQLParser.QueryContext ctx) {
        StringBuilder csharp = new StringBuilder();
        for (var query : ctx.operation()) {
            csharp.append(visit(query)).append(";\n\n");
        }
        return csharp.toString();
    }

    private String mainName;
    private String mainTable;

    @Override
    public String visitSelect_stm(SQLParser.Select_stmContext ctx) {
        isGrouped = false;
        tablesToJoin = 1;
        prevJoins = new ArrayList<>();

        mainName = visit(ctx.from_stm());
        mainTable = ctx.from_stm().ID().getFirst().getText();
        StringBuilder csharp = new StringBuilder();

        // JOIN
        if (!ctx.join_stm().isEmpty()) {
            if (ctx.join_stm().size() > 1) {
                boolean anyRight = ctx.join_stm().stream()
                        .anyMatch( x -> detectJoinType(x.join_bef()) == JoinType.RIGHT);
                if (anyRight) {
                    errors.add("Błąd w linii "+ctx.start.getLine()+": wiele joinów z RIGHT JOIN nie jest wspierane.");
                    return "/* Błąd przez brak wsparcia dla wielu joinów z RIGHT JOIN. */";
                }
            }
            for (var join_stmContext : ctx.join_stm()) {
                tablesToJoin++;
                csharp.append(visit(join_stmContext));
            }
        } else{
            csharp.append(mainTable);
        }

        // WHERE
        if (ctx.where_stm() != null) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }

        // GROUP BY
        if (ctx.groupby_stm() != null) {
            isGrouped = true;
            groupedColumns = new ArrayList<>();
            csharp.append("\n\t.GroupBy(temp => ").append(visit(ctx.groupby_stm())).append(')');
        }

        // HAVING
        if (ctx.having_stm() != null) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.having_stm())).append(')');
        }

        // SELECT
        csharp.append("\n\t.Select(temp => ").append(visit(ctx.select_list())).append(')');

        // DISTINCT
        if (ctx.DISTINCT() != null) csharp.append("\n\t.Distinct()");

        // ORDER BY
        if (ctx.order_stm() != null) {
            csharp.append(visit(ctx.order_stm()));
        }

        return csharp.toString();
    }

    @Override
    public String visitSelect_list(SQLParser.Select_listContext ctx) {
        if (ctx.MULT() != null) return anonName;

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
    public String visitSelect_item(SQLParser.Select_itemContext ctx) {
        StringBuilder csharp = new StringBuilder();

        if (ctx.AS() != null) csharp.append(ctx.ID().getText()).append(" = ");

        if (ctx.column() != null) {
            var col = visit(ctx.column());
            if (isGrouped && groupedColumns.contains(col)) {
                if (groupedColumns.size() == 1) return csharp.append(anonName).append(".Key").toString();
                else return csharp.append(anonName).append(".Key.").append(col).toString();
            }
            csharp.append(anonName).append('.').append(col);
        } else if (ctx.agg_func() != null) csharp.append(visit(ctx.agg_func()));

        return csharp.toString();
    }

    @Override
    public String visitColumn(SQLParser.ColumnContext ctx) {
        if (ctx.PER() != null) {
            List<TerminalNode> ids = ctx.ID();
            return ids.get(0).getText() + '.' + ids.get(1).getText();
        }
        return ctx.getText();
    }

    @Override
    public String visitAgg_func(SQLParser.Agg_funcContext ctx) {
        String col = ctx.column() != null ? visit(ctx.column()) : "";

        if (ctx.MIN() != null) return anonName + ".Min(s => s." + col + ")";

        if (ctx.MAX() != null) return anonName + ".Max(s => s." + col + ")";

        if (ctx.COUNT() != null) return anonName + ".Count()";

        if (ctx.SUM() != null) return anonName + ".Sum(s => s." + col + ")";

        if (ctx.AVG() != null) return anonName + ".Average(s => s." + col + ")";

        return null;
    }

    @Override
    public String visitWhere_stm(SQLParser.Where_stmContext ctx) {
        return visit(ctx.logic_form());
    }

    @Override
    public String visitFrom_stm(SQLParser.From_stmContext ctx) {
        return ctx.ID().size() == 1 ? ctx.ID().getFirst().getText() : ctx.ID().get(1).getText();
    }

    private enum JoinType {INNER, LEFT, RIGHT}

    @Override
    public String visitJoin_stm(SQLParser.Join_stmContext ctx) {
        JoinType joinType = detectJoinType(ctx.join_bef());

        String joinTable = ctx.ID().getFirst().getText();
        String joinName = ctx.ID().size() == 1 ? ctx.ID().getFirst().getText() : ctx.ID().get(1).getText();

        String mainNameJoin;

        List<String> mainSide = new ArrayList<>();
        List<String> joinSide = new ArrayList<>();
        for (var on : ctx.join_on()) {
            String first = visit(on.column(0));
            if (first.startsWith(joinName)){
                joinSide.add(first);
                mainSide.add(visit(on.column(1)));
            } else {
                mainSide.add(first);
                joinSide.add(visit(on.column(1)));
            }
        }

        if (tablesToJoin == 2) {
            mainNameJoin = mainName;
            prevJoins.addAll(Arrays.asList(mainNameJoin, joinName));
        } else {
            String temp = mainSide.getFirst();
            mainNameJoin = temp.substring(0, temp.indexOf('.'));
            prevJoins.add(joinName);
        }
        return switch (joinType) {
            case INNER -> tablesToJoin == 2 ? mainTable+buildInnerJoin(joinTable, mainNameJoin, joinName, mainSide, joinSide)
                    : buildInnerJoin(joinTable, mainNameJoin, joinName, mainSide, joinSide);
            case LEFT -> tablesToJoin == 2 ? mainTable+buildGroupJoin(joinTable, joinName, mainSide, joinSide, mainNameJoin, joinName)
                    : buildGroupJoin(joinTable, joinName, mainSide, joinSide, mainNameJoin, joinName);
            case RIGHT -> joinTable+buildGroupJoin(mainTable, mainNameJoin, joinSide, mainSide, joinName, mainNameJoin);
        };
    }

    private JoinType detectJoinType(SQLParser.Join_befContext ctx) {
        if (ctx == null) return JoinType.INNER;
        if (ctx.LEFT() != null) return JoinType.LEFT;
        if (ctx.RIGHT() != null) return JoinType.RIGHT;
        return JoinType.INNER;
    }

    private String buildKeySelector(String param, List<String> keys) {
        if (keys.size() == 1) {
            return param + " => " + keys.getFirst();
        }
        return param + " => new {" + String.join(", ", keys) + "}";
    }

    private String buildInnerJoin(String joinTable, String mainName, String joinName, List<String> mainSide, List<String> joinSide) {
        if (tablesToJoin > 2){
            mainSide.replaceAll(s -> "prev."+s);
            StringBuilder multiTable = new StringBuilder();
            for (int i = 0; i < tablesToJoin; i++) {
                if (i < tablesToJoin -1 ){
                    multiTable.append("prev.").append(prevJoins.get(i)).append(", ");
                }else{
                    multiTable.append(prevJoins.get(i));
                }
            }
            return """
                
                \t.Join(
                \t\t%s,
                \t\t%s,
                \t\t%s,
                \t\t(prev, %s) => new {%s}
                \t)""".formatted(
                    joinTable,
                    buildKeySelector("prev", mainSide),
                    buildKeySelector(joinName, joinSide),
                    joinName,
                    multiTable.toString()
            );
        }
        return """
                
                \t.Join(
                \t\t%s,
                \t\t%s,
                \t\t%s,
                \t\t(%s, %s) => new {%s, %s}
                \t)""".formatted(
                joinTable,
                buildKeySelector(mainName, mainSide),
                buildKeySelector(joinName, joinSide),
                mainName, joinName,
                mainName, joinName
        );
    }

    private String buildGroupJoin(String joinTable, String joinName, List<String> mainSide, List<String> joinSide, String outerParam, String innerParam) {
        String groupName = joinName + "Group";
        if (tablesToJoin > 2){
            mainSide.replaceAll(s -> "prev."+s);
            StringBuilder multiTable = new StringBuilder();
            for (int i = 0; i < tablesToJoin; i++) {
                if (i < tablesToJoin -1){
                    multiTable.append(prevJoins.get(i))
                            .append(" = ")
                            .append(anonName)
                            .append(".prev.")
                            .append(prevJoins.get(i)).append(", ");
                }else{
                    multiTable.append(prevJoins.get(i));
                }
            }
            return """
                
                \t.GroupJoin(
                \t\t%s,
                \t\t%s,
                \t\t%s,
                \t\t(%s, %s) => new {%s, %s}
                \t)
                \t.SelectMany(
                \t\t%s => %s.%s.DefaultIfEmpty(),
                \t\t(%s, %s) => new {%s}
                \t)""".formatted(
                    joinTable,
                    buildKeySelector("prev", mainSide),
                    buildKeySelector(innerParam, joinSide),
                    "prev", groupName, "prev", groupName,
                    anonName, anonName, groupName,
                    anonName, joinName,
                    multiTable, joinName
            );
        }
        return """
                
                \t.GroupJoin(
                \t\t%s,
                \t\t%s,
                \t\t%s,
                \t\t(%s, %s) => new {%s, %s}
                \t)
                \t.SelectMany(
                \t\t%s => %s.%s.DefaultIfEmpty(),
                \t\t(%s, %s) => new {%s = %s.%s, %s}
                \t)""".formatted(
                joinTable,
                buildKeySelector(outerParam, mainSide),
                buildKeySelector(innerParam, joinSide),
                outerParam, groupName, outerParam, groupName,
                anonName, anonName, groupName,
                anonName, joinName,
                outerParam, anonName, outerParam, joinName
        );
    }

    @Override
    public String visitGroupby_stm(SQLParser.Groupby_stmContext ctx) {
        StringBuilder csharp = new StringBuilder();

        if (ctx.column().size() == 1) {
            groupedColumns.add(visit(ctx.column().getFirst()));
            csharp.append(anonName).append('.').append(visit(ctx.column().getFirst()));
        } else {
            csharp.append("new {");
            for (var col : ctx.column()) {
                groupedColumns.add(visit(col));
                csharp.append(anonName).append('.').append(visit(col));
                if (ctx.column().indexOf(col) != ctx.column().size() - 1) csharp.append(", ");
            }
            csharp.append("}");
        }

        return csharp.toString();
    }

    private boolean firstOrder;
    @Override
    public String visitOrder_stm(SQLParser.Order_stmContext ctx){
        firstOrder = true;
        return visit(ctx.order_list());
    }

    @Override
    public String visitOrder_list(SQLParser.Order_listContext ctx){
        StringBuilder csharp = new StringBuilder();
        for (var ord : ctx.order_item()){
            csharp.append(visit(ord));
        }
        return csharp.toString();
    }

    @Override
    public String visitOrder_item(SQLParser.Order_itemContext ctx){
        if (firstOrder) {
            firstOrder = false;
            if (ctx.ASC() != null || ctx.getChildCount() == 1) {
                return "\n\t.OrderBy("+anonName+" => "+anonName+'.'+visit(ctx.column())+')';
            }
            return "\n\t.OrderByDescending("+anonName+" => "+anonName+'.'+visit(ctx.column())+')';
        }
        if (ctx.ASC() != null || ctx.getChildCount() == 1){
            return "\n\t.ThenBy("+anonName+" => "+anonName+'.'+visit(ctx.column())+')';
        }
        return "\n\t.ThenByDescending("+anonName+" => "+anonName+'.'+visit(ctx.column())+')';
    }

    @Override
    public String visitDelete_stm(Delete_stmContext ctx) {
        StringBuilder csharp = new StringBuilder(ctx.from_stm().ID().getFirst().getText());

        if (ctx.getChildCount() == 3) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }

        csharp.append("\n\t.ExecuteDelete()");
        return csharp.toString();
    }

    @Override
    public String visitUpdate_stm(Update_stmContext ctx) {
        StringBuilder csharp = new StringBuilder(ctx.ID().getText());

        if (ctx.getChildCount() == 4) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }
        csharp.append(visit(ctx.set_stm()));
        return csharp.toString();
    }

    @Override
    public String visitSet_stm(Set_stmContext ctx) {
        return "\n\t.ExecuteUpdate(setters => setters" + visit(ctx.set_list()) + "\n\t)";
    }

    @Override
    public String visitSet_list(Set_listContext ctx) {
        var csharp = new StringBuilder();
        var setItems = ctx.set_item();
        for (var item : setItems){
            csharp.append(visit(item));
        }
        return csharp.toString();
    }

    @Override
    public String visitSet_item(Set_itemContext ctx) {
        var csharp = new StringBuilder();
        csharp.append("\n\t\t.SetProperty(");
        csharp.append(anonName).append(" => ");
        csharp.append(anonName).append('.').append(ctx.ID().getText()).append(", ");
        var child = ctx.getChild(2);
        if (hasColumnInside(child)) {
            csharp.append(anonName).append(" => ");
        }
        csharp.append(visit(ctx.getChild(2))).append(")");
        return csharp.toString();
    }

    private boolean hasColumnInside(ParseTree node) {
        if (node instanceof SQLParser.ColumnContext) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (hasColumnInside(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String visitInsert_stm(Insert_stmContext ctx) {
        StringBuilder csharp = new StringBuilder();
        var idStr = ctx.into_stm().ID().getText();
        idStr = idStr.substring(0, idStr.length() - 1);
        var parStr = new ArrayList<String>();
        for (var id: ctx.into_stm().into_bracket_list().ID()){
            parStr.add(id.getText());
        }
        csharp.append("BulkInsert(new ").append(idStr).append("[] {");
        var val_ll = ctx.values_stm().values_list().values_item();
        for (int i=0; i<val_ll.size() - 1; i++){
            csharp.append("\n\tnew ").append(idStr).append(" { ");
            var val_l = val_ll.get(i).values_item_list().value();
            fillInsideInsert(csharp,val_l,parStr);
            csharp.append("},");
        }
        csharp.append("\n\tnew ").append(idStr).append(" { ");
        var val_l = val_ll.getLast().values_item_list().value();
        fillInsideInsert(csharp,val_l,parStr);
        csharp.append("}");
        csharp.append("\n})");
        return csharp.toString();
    }

    private void fillInsideInsert(StringBuilder csharp, List<SQLParser.ValueContext> val_l,ArrayList<String> parStr){
        for (int j=0; j<val_l.size() - 1; j++){
            csharp.append(parStr.get(j)).append(" = ").append(visit(val_l.get(j))).append(", ");
        }
        csharp.append(parStr.getLast()).append(" = ").append(visit(val_l.getLast())).append(" ");
    }

    @Override
    public String visitValue(ValueContext ctx) {
        return simpleChangeTerm(ctx);
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
        if (ctx.getChildCount() == 6) {
            String lowerStr = visit(ctx.getChild(3));
            String higherStr = visit(ctx.getChild(5));
            csharp.append(mainStr);
            csharp.append(" < ");
            csharp.append(lowerStr);
            csharp.append(" && ");
            csharp.append(mainStr);
            csharp.append(" > ");
            csharp.append(higherStr);
        } else {
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
        String str = "";
        if (ctx.getChildCount() == 3) {
            str = ctx.getChild(2).getText();
        } else {
            csharp.append("!");
            str = ctx.getChild(3).getText();
        }
        str = csharpStr(str);
        csharp.append("new").append(" ").append("Regex(").append(regex(str)).append(")");
        csharp.append(".IsMatch(").append(anonName).append(".").append(colStr).append(")");
        return csharp.toString();
    }

    private String regex(String str){
        StringBuilder strReg = new StringBuilder();
        Set<Character> hSet = Set.of('.','*','+','?','^','$','[',']','{','}','(',')','|','\\');
        for (char c: str.toCharArray()){
            if (c == '%'){
                strReg.append(".*");
            }else if (c == '_'){
                strReg.append(".");
            }else if (hSet.contains(c)){
                strReg.append('\\').append(c);
            }else{
                strReg.append(c);
            }
        }
        return strReg.toString();
    }

    @Override
    public String visitLogic_null_cmp(SQLParser.Logic_null_cmpContext ctx) {
        StringBuilder csharp = new StringBuilder();
        String colStr = visit(ctx.getChild(0));
        csharp.append(anonName).append(".").append(colStr);
        if (ctx.getChildCount() == 3) {
            csharp.append(" == ");
        } else {
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
        return simpleChangeTerm(ctx);
    }

    public String simpleChangeTerm(ParserRuleContext ctx) {
        StringBuilder csharp = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode termChild) {
                int termType = termChild.getSymbol().getType();
                if (termType == STR) {
                    csharp.append(csharpStr(termChild.getText()));
                }else if (termType == NUM || termType == INT) {
                    csharp.append(termChild.getText());
                } else {
                    csharp.append(SymbolMapper.getSymbolsMap().get(termType));
                }
            } else if (child instanceof ParserRuleContext logicChild) {
                if (logicChild instanceof SQLParser.ColumnContext childColTxt) {
                    csharp.append(anonName + ".");
                }
                csharp.append(visit(logicChild));
            }
        }
        return csharp.toString();
    }

    public String csharpStr(String sqlStr){
        if (sqlStr == null || sqlStr.length() < 2 || sqlStr.charAt(0) != '\'' || sqlStr.charAt(sqlStr.length()-1) != '\'') {
            throw new IllegalArgumentException("Invalid SQL string literal");
        }
        String insideStr = sqlStr.substring(1, sqlStr.length()-1);
        while (insideStr.contains("''")){
            insideStr = insideStr.replace("''", "'");
        }
        return "\"" + insideStr + "\"";
    }
}

