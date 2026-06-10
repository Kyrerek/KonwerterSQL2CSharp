package org.model;


import antlr.SQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
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
    private Map<String, List<String>> tablesAndColumns = new LinkedHashMap<>();
    private List<String> warnings;
    private Map<String, String> columnsAliases;
    private Map<String, String> tablesAliases;
    private List<String> stmErrors;
    private Set<String> selectedCols;

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String visitQuery(SQLParser.QueryContext ctx) {
        StringBuilder csharp = new StringBuilder();

        StringBuilder creations = new StringBuilder();
        StringBuilder actions = new StringBuilder();
        for (var query : ctx.operation()) {
            if (query.children.getFirst() instanceof SQLParser.Create_stmContext ) {
                creations.append(visit(query)).append("\n\n");
            }else {
                actions.append(visit(query)).append(";\n\n");
            }
        }
        csharp.append(actions.toString());
        csharp.append(creations.toString());
        return csharp.toString();
    }

    private String mainName;
    private String mainTable;

    @Override
    public String visitSelect_stm(SQLParser.Select_stmContext ctx) {
        stmErrors = new ArrayList<>();
        warnings = new ArrayList<>();
        columnsAliases = new HashMap<>();
        tablesAliases = new HashMap<>();
        isGrouped = false;
        tablesToJoin = 1;
        prevJoins = new ArrayList<>();

        mainName = visit(ctx.from_stm());
        mainTable = wrapInDb(ctx.from_stm().ID().getFirst().getText());
        StringBuilder csharp = new StringBuilder();

        // JOIN
        if (!ctx.join_stm().isEmpty()) {
            if (ctx.join_stm().size() > 1) {
                boolean anyRight = ctx.join_stm().stream()
                        .anyMatch( x -> detectJoinType(x.join_bef()) == JoinType.RIGHT);
                if (anyRight) {
                    errors.add("ERROR: Błąd w linii "+ctx.start.getLine()+": wiele joinów z RIGHT JOIN nie jest wspierane.");
                    return "/* ERROR: Błąd przez brak wsparcia dla wielu joinów z RIGHT JOIN. */";
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

        if (ctx.order_stm() != null && ctx.groupby_stm() == null) {
            csharp.append(visit(ctx.order_stm()));
        }

        selectedCols = new HashSet<>();
        // SELECT
        csharp.append("\n\t.Select(temp => ").append(visit(ctx.select_list())).append(')');
        if (isGrouped){
            for (var col : selectedCols){
                if (!groupedColumns.contains(col)){
                    errors.add("ERROR: Błąd w linii "+ctx.start.getLine()+". Nie każda kolumna została dodana do klauzauli GROUP BY przy agregacji");
                    return "/* ERROR: Błąd w linii "+ctx.start.getLine()+". Nie każda kolumna została dodana do klauzauli GROUP BY przy agregacji */";
                }
            }
        }

        // DISTINCT
        if (ctx.DISTINCT() != null) csharp.append("\n\t.Distinct()");

        if (ctx.order_stm() != null && ctx.groupby_stm() != null) {
            csharp.append(visit(ctx.order_stm()));
        }

        if (!stmErrors.isEmpty()){
            return String.join("\n", stmErrors);
        }

        for (var war : warnings){
            csharp.insert(0, war);
        }
        return csharp.toString() + ".Show()";
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

        if (ctx.AS() != null) {
            String alias = ctx.ID().getText();
            Token aliasToken = ctx.ID().getSymbol();
            if (columnsAliases.containsKey(alias)){
                errors.add("ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+alias+"\" jest już zajęty przez inną kolumnę!");
                stmErrors.add("/* ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+alias+"\" jest już zajęty przez inną kolumnę! */");
            } else{
                columnsAliases.put(alias, ctx.column() != null ? visit(ctx.column()) : visit(ctx.agg_func()));
            }
            csharp.append(alias).append(" = ");
        }

        if (ctx.column() != null) {
            var col = visit(ctx.column());
            selectedCols.add(col);
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
            String table = ctx.ID().getFirst().getText();
            String checkTable = table;
            if (!tablesAliases.isEmpty() && tablesAliases.containsKey(table)){
                checkTable = tablesAliases.get(table);
            }
            String column = ctx.ID().get(1).getText();
            if (!tablesAndColumns.containsKey(checkTable)){
                    errors.add("WARNING: Tabela o nazwie \""+checkTable+"\" nie została określona przed linią "+ctx.start.getLine()+". Zalecane jest pierw jej stworzenie.");
                    warnings.add("/* WARNING: Tabela o nazwie \""+checkTable+"\" nie została jeszcze określona! */\n");
            } else {
                Token columnToken = ctx.ID().get(1).getSymbol();
                if (!tablesAndColumns.get(checkTable).contains(column)){
                    errors.add("ERROR: Błąd w linii "+columnToken.getLine()+":"+columnToken.getCharPositionInLine()+". Kolumna \""+column+"\" nie istnieje w tabeli \""+checkTable+"\"!");
                    stmErrors.add("/* ERROR: Błąd w linii "+columnToken.getLine()+":"+columnToken.getCharPositionInLine()+". Kolumna \""+column+"\" nie istnieje w tabeli \""+checkTable+"\"! */");
                }
            }

            return table + '.' + column;
        }

        String column = ctx.getText();
        return column;
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
        String tableName = ctx.ID().getFirst().getText();
        if (!tablesAndColumns.containsKey(tableName)){
            errors.add("WARNING: Tabela o nazwie \""+tableName+"\" nie została określona przed linią "+ctx.start.getLine()+". Zalecane jest pierw jej stworzenie.");
            warnings.add("/* WARNING: Tabela o nazwie \""+tableName+"\" nie została jeszcze określona! */\n");
        }
        if (ctx.AS() != null){
            String alias = ctx.ID().get(1).getText();
            Token aliasToken = ctx.ID().get(1).getSymbol();
            if (tablesAliases.containsKey(alias)){
                errors.add("ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+alias+"\" jest już zajęty przez inną tabelę!");
                stmErrors.add("/* ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+alias+"\" jest już zajęty przez inną tabelę! */");
            } else {
                tablesAliases.put(alias, tableName);
            }
            return alias;
        }
        return tableName;
    }

    private enum JoinType {INNER, LEFT, RIGHT}

    @Override
    public String visitJoin_stm(SQLParser.Join_stmContext ctx) {
        JoinType joinType = detectJoinType(ctx.join_bef());

        String joinTable = ctx.ID().getFirst().getText();
        if (!tablesAndColumns.containsKey(joinTable)){
            errors.add("WARNING: Tabela o nazwie \""+joinTable+"\" nie została określona przed linią "+ctx.start.getLine()+". Zalecane jest pierw jej stworzenie.");
            warnings.add("/* WARNING: Tabela o nazwie \""+joinTable+"\" nie została jeszcze określona! */\n");
        }
        String joinName = joinTable;
        if (ctx.AS() != null){
            joinName = ctx.ID().get(1).getText();
            Token aliasToken = ctx.ID().get(1).getSymbol();
            if (tablesAliases.containsKey(joinName)){
                errors.add("ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+joinName+"\" jest już zajęty przez inną tabelę!");
                stmErrors.add("/* ERROR: Błąd w linii "+aliasToken.getLine()+":"+aliasToken.getCharPositionInLine()+". Alias \""+joinName+"\" jest już zajęty przez inną tabelę! */");
            } else {
                tablesAliases.put(joinName, joinTable);
            }
        }
        String mainNameJoin;

        List<String> mainSide = new ArrayList<>();
        List<String> joinSide = new ArrayList<>();
        for (var on : ctx.join_on()) {
            String first = visit(on.column(0));
            String second = visit(on.column(1));
            if (first.startsWith(joinName)){
                joinSide.add(first);
                mainSide.add(second);
            } else {
                mainSide.add(first);
                joinSide.add(second);
            }
        }
        joinTable = wrapInDb(joinTable);
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
        StringBuilder csharp = new StringBuilder(wrapInDb(ctx.from_stm().ID().getFirst().getText()));

        if (ctx.getChildCount() == 3) {
            csharp.append("\n\t.Where(temp => ").append(visit(ctx.where_stm())).append(')');
        }

        csharp.append("\n\t.ExecuteDelete()");
        return csharp.toString();
    }

    @Override
    public String visitUpdate_stm(Update_stmContext ctx) {
        StringBuilder csharp = new StringBuilder(wrapInDb(ctx.ID().getText()));

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
        var parStr = new ArrayList<String>();
        var cols = ctx.into_stm().into_bracket_list();
        var warnErr = new ArrayList<String>();

        if (!tablesAndColumns.containsKey(idStr)) {
            errors.add("WARNING: Tabela o nazwie \""+idStr+"\" nie została określona przed linią "+ctx.start.getLine()+". Zalecane jest pierw jej stworzenie.");
            warnErr.add("/* WARNING: Tabela o nazwie \""+idStr+"\" nie została jeszcze określona! */");
        }

        if (cols != null) {
            for (var id: ctx.into_stm().into_bracket_list().ID()){
                String colTest = id.getText();
                parStr.add(colTest);
                Token columnToken = id.getSymbol();
                if (tablesAndColumns.containsKey(idStr) && !tablesAndColumns.get(idStr).contains(colTest)){
                    errors.add("ERROR: Błąd w linii "+columnToken.getLine()+":"+columnToken.getCharPositionInLine()+". Kolumna \""+colTest+"\" nie istnieje w tabeli \""+idStr+"\"!");
                    warnErr.add("/* ERROR: Błąd w linii "+columnToken.getLine()+":"+columnToken.getCharPositionInLine()+". Kolumna \""+colTest+"\" nie istnieje w tabeli \""+idStr+"\"! */");
                }
            }
        }else{
            if (tablesAndColumns.containsKey(idStr)){
                parStr.addAll(tablesAndColumns.get(idStr));
            }
        }

        if (!warnErr.isEmpty()){
            return String.join("\n", warnErr);
        }

        csharp.append("db.BulkInsert(new ").append(idStr).append("[] {");
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

    private List<String> uniqueList;
    private boolean hasPrimary;
    @Override
    public String visitCreate_stm(SQLParser.Create_stmContext ctx){
        hasPrimary = false;
        stmErrors = new ArrayList<>();
        String tableName = ctx.ID().toString();
        Token tableToken = ctx.ID().getSymbol();
        if (tablesAndColumns.containsKey(tableName)){
            errors.add("ERROR: Błąd w linii "+tableToken.getLine()+":"+tableToken.getCharPositionInLine()+". Tabela \""+tableName+"\" już istnieje!");
            return  "/* ERROR: Błąd w linii "+tableToken.getLine()+":"+tableToken.getCharPositionInLine()+". Tabela \""+tableName+"\" już istnieje! */";
        }
        tablesAndColumns.put(ctx.ID().getText(), new ArrayList<>());
        uniqueList =  new ArrayList<>();
        StringBuilder str = new StringBuilder("public class " +
                tableName +
                "\n{" +
                visit(ctx.create_list()) +
                '}');
        if (!stmErrors.isEmpty()){
            return String.join("\n", stmErrors);
        }
        for (var u : uniqueList){
            str.insert(0, "[Index(nameof(" + u + "), IsUnique = true)]\n");
        }
        return str.toString();
    }

    @Override
    public String visitCreate_list(SQLParser.Create_listContext ctx){
        StringBuilder builder = new StringBuilder();
        for (var e : ctx.create_element()){
            builder.append(visit(e));
        }
        return builder.toString();
    }

    private String getDataType(SQLParser.Data_typeContext ctx){
        if (ctx.INT_TYPE() != null) return "int?";
        if (ctx.NUMERIC() != null) return "decimal?";
        if (ctx.VARCHAR() != null) return  "string?";
        return "bool?";
    }

    private enum ContraintType{
        PRIMARY, REF, UNIQUE, DEFAULT, NOTNULL
    }

    private ContraintType detectConstraintType(SQLParser.ContraintContext ctx){
        if (ctx.UNIQUE() != null) return ContraintType.UNIQUE;
        if (ctx.NULL() != null) return ContraintType.NOTNULL;
        if (ctx.PRIMARY() != null) return ContraintType.PRIMARY;
        if (ctx.DEFAULT() != null) return ContraintType.DEFAULT;
        return ContraintType.REF;
    }

    private boolean hasUnique;
    private boolean hasNull;
    @Override
    public String visitCreate_element(SQLParser.Create_elementContext ctx){
        hasUnique = false;
        hasNull = false;

        String lastKey = null;
        Token colToken = ctx.ID().getSymbol();
        for (var key : tablesAndColumns.keySet()){
            lastKey = key;
        }
        if (tablesAndColumns.get(lastKey).contains(ctx.ID().getText())){
            errors.add("ERROR: Błąd w linii "+colToken.getLine()+":"+colToken.getCharPositionInLine()+". Tabela \""+lastKey+"\" ma już kolumne \""+ctx.ID().getText()+"\"!");
            stmErrors.add("/* ERROR: Błąd w linii "+colToken.getLine()+":"+colToken.getCharPositionInLine()+". Tabela \""+lastKey+"\" ma już kolumne \""+ctx.ID().getText()+"\"! */");
        }

        StringBuilder builder = new StringBuilder();
        String type = getDataType(ctx.data_type());
        boolean isNullable = true;
        String defaultVal = null;
        boolean required = false;
        String fk = null;
        for (var c : ctx.contraint()){
            Token cToken = c.start;
            switch (detectConstraintType(c)){
                case ContraintType.UNIQUE ->{
                    if (hasUnique) {
                        errors.add("ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń UNIQUE!");
                        stmErrors.add("/* ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń UNIQUE! */");
                    } else {
                        uniqueList.add(ctx.ID().toString());
                        hasUnique = true;
                    }
                }
                case ContraintType.NOTNULL -> {
                    if (hasNull) {
                        errors.add("ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń NOT NULL!");
                        stmErrors.add("/* ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń NOT NULL! */");
                    } else {
                        isNullable = false;
                        required = true;
                        hasNull = true;
                    }

                }
                case ContraintType.PRIMARY -> {
                    if (hasPrimary) {
                        errors.add("ERROR: Błąd w linii "+colToken.getLine()+":"+colToken.getCharPositionInLine()+". Tabela \""+lastKey+"\" próbuje zdefiniować wiele kluczy głównych!");
                        stmErrors.add("/* ERROR: Błąd w linii "+colToken.getLine()+":"+colToken.getCharPositionInLine()+". Tabela \""+lastKey+"\" próbuje zdefiniować wiele kluczy głównych! */");
                    } else{
                        builder.append("\n\t[Key]");
                        isNullable = false;
                        hasPrimary = true;
                    }
                }
                case ContraintType.DEFAULT -> {
                    if (defaultVal != null) {
                        errors.add("ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń DEFAULT!");
                        stmErrors.add("/* ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele ograniczeń DEFAULT! */");
                    } else {
                        Token valueToken = c.value().start;
                        if (correctType(type, c.value().getText())){
                            defaultVal = visit(c.value());
                        } else {
                            errors.add("ERROR: Błąd w linii "+valueToken.getLine()+":"+valueToken.getCharPositionInLine()+". Wartość \""+c.value().getText()+"\" nie jest typu \""+type+"\"!");
                            stmErrors.add("/* ERROR: Błąd w linii "+valueToken.getLine()+":"+valueToken.getCharPositionInLine()+". Wartość \""+c.value().getText()+"\" nie jest typu \""+type+"\"! */");
                        }
                    }
                }
                case ContraintType.REF -> {
                    if (fk != null) {
                        errors.add("ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele kluczy obcych!");
                        stmErrors.add("/* ERROR: Błąd w linii "+cToken.getLine()+":"+cToken.getCharPositionInLine()+". Kolumna \""+ctx.ID()+"\" próbuje zdefiniować wiele kluczy obcych! */");
                    } else {
                        String refTable = c.ID(0).toString();
                        Token refToken = c.ID(0).getSymbol();
                        String refCol = c.ID(1).getText();
                        Token refColToken = c.ID(1).getSymbol();
                        if (!tablesAndColumns.containsKey(refTable)){
                            errors.add("ERROR: Błąd w linii "+refToken.getLine()+":"+refToken.getCharPositionInLine()+". Tabela \""+refTable+"\" nie istnieje, więc nie można się do niej odwołać!");
                            stmErrors.add("/* ERROR: Błąd w linii "+refToken.getLine()+":"+refToken.getCharPositionInLine()+". Tabela \""+refTable+"\" nie istnieje, więc nie można się do niej odwołać! */");
                        } else if (!tablesAndColumns.get(refTable).contains(refCol)){
                            errors.add("ERROR: Błąd w linii "+refColToken.getLine()+":"+refColToken.getCharPositionInLine()+". Kolumna \""+refCol+"\" nie istnieje w tabeli \""+refTable+"\"!");
                            stmErrors.add("/* ERROR: Błąd w linii "+refColToken.getLine()+":"+refColToken.getCharPositionInLine()+". Kolumna \""+refCol+"\" nie istnieje w tabeli \""+refTable+"\"! */");
                        }

                        fk = """ 
                        
                        \t[ForeignKey(nameof(%s))]
                        \tpublic virtual %s? %sFK {get; set;}
                        """.formatted(ctx.ID(), c.ID(0), c.ID(0));
                    }
                }
            }
        }
        if (!isNullable){
            type = type.substring(0, type.length()-1);
        }
        builder.append("\n\tpublic ");
        if (required){
            builder.append("required ");
        }
        builder.append(type)
                .append(' ')
                .append(ctx.ID())
                .append(" {get; set;}");
        if (defaultVal != null){
            builder.append(" = ").append(defaultVal);
            if (type.contains("decimal")){
                builder.append("m");
            }
            builder.append(";");
        }
        if (fk != null){
            builder.append('\n').append(fk);
        }

        tablesAndColumns.get(lastKey).add(ctx.ID().getText());

        return builder.append('\n').toString();
    }

    private boolean correctType(String type, String value) {
        if (value == null) return false;
        String baseType = type.replace("?", "");

        try {
            return switch (baseType) {
                case "int" -> {
                    Integer.parseInt(value);
                    yield true;
                }
                case "decimal" -> {
                    Double.parseDouble(value.replace(",", "."));
                    yield true;
                }
                case "string" -> value.startsWith("'") && value.endsWith("'");
                case "bool" -> value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")
                        || value.equals("1") || value.equals("0");
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String wrapInDb(String colName){
        return "db.Set<" + colName + ">()";
    }
}

