package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr.SQLParser;
import antlr.SQLLexer;


public class Main {
    public static void main(String[] args) {
        String testStr = """
        SELECT *
        FROM klienci
        JOIN zamowienia ON klienci.id = zamowienia.id_klienta
        WHERE zamowienia.ilosc > 2 AND klientci.id BETWEEN 10 and 100
        ORDER BY klienci.nazwa DESC;
        """.trim();
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(testStr));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        ParseTree tree = parser.query();
        System.out.println(tree.toStringTree(parser));
    }
}