package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import antlr.HelloLexer;
import antlr.HelloParser;


public class Main {
    public static void main(String[] args) {
        String testStr = "hello world";
        HelloLexer lexer = new HelloLexer(CharStreams.fromString(testStr));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HelloParser parser = new HelloParser(tokens);

        ParseTree tree = parser.r();   // start rule
        System.out.println(tree.toStringTree(parser));
    }
}