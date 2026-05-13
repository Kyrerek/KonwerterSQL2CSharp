package org.model;


import java.util.Map;

import static antlr.SQLParser.*;

public class SymbolMapper {
    private static final Map<Integer, String> symbolsMap;

    static {
        symbolsMap = Map.ofEntries(
                Map.entry(LBRACKET,"("),
                Map.entry(RBRACKET,")"),
                Map.entry(OR,"||"),
                Map.entry(AND," && "),
                Map.entry(NOT,"!"),
                Map.entry(EGREATER,"=>"),
                Map.entry(ELESS,"<="),
                Map.entry(NEQL,"!="),
                Map.entry(GREATER, ">"),
                Map.entry(LESS, "<"),
                Map.entry(EQL,"=="),
                Map.entry(LIKE,"=="),
                Map.entry(IS,"=="),
                Map.entry(NULL,"null"),
                Map.entry(PLUS,"+"),
                Map.entry(MINUS,"-"),
                Map.entry(MULT,"*"),
                Map.entry(DIV,"/"),
                Map.entry(TRUE,"true"),
                Map.entry(FALSE,"false")
        );
    }

    private SymbolMapper(){}

    public static Map<Integer, String> getSymbolsMap(){
        return symbolsMap;
    }

}
