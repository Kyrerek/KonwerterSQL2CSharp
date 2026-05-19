package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr.SQLParser;
import antlr.SQLLexer;
import org.model.SQLtoCSharpVisitor;


public class Main {
    public static void main(String[] args) {
//        String testStr = """
//        SELECT *
//        FROM klienci
//        JOIN zamowienia ON klienci.id = zamowienia.id_klienta
//        WHERE zamowienia.ilosc > 2 AND klientci.id BETWEEN 10 and 100
//        ORDER BY klienci.nazwa DESC;
//        """.trim();
        String sqlStr = """
                SELECT DISTINCT name, age FROM users WHERE (age > 18 AND age is NOT NULL) or name NOT LIKE '%_OO%' and age BETWEEN 10 AND 100; 
                SELECT Dept, AVG(Salary) AS average FROM Emps GROUP BY Dept;
                SELECT e.DeptId FROM Employees AS e JOIN Departments AS d ON e.DeptId = d.Id;
                SELECT e.DeptId FROM Employees AS e LEFT JOIN Departments AS d ON e.DeptId = d.Id AND e.City = d.City;
                SELECT e.DeptId FROM Employees AS e RIGHT JOIN Departments AS d ON e.DeptId = d.Id AND e.City = d.City;
                SELECT Dept, AVG(Salary) AS average FROM Emps GROUP BY Dept ORDER BY average DESC, Dept;
                DELETE FROM users WHERE name LIKE '%ki%' and age NOT BETWEEN 20 AND 50;
                UPDATE users
                SET age = age * 1.10, name = 'UNKNOWN', country = NULL
                WHERE name LIKE '%____%';
                INSERT INTO Users (name,age,country,code)
                VALUES
                ('User1', 10, 'Norway'),
                ('Per Olsen', 20, NULL),
                ('Finn Egan', 50, 'Poland');
                SELECT a.Name, b.Price, c.Category
                FROM TableA AS a
                JOIN TableB AS b ON a.Id = b.AId
                JOIN TableC AS c ON b.Id = c.BId
                JOIN TableD AS d ON d.BId = a.Id;
                SELECT a.Name, b.Price, c.Category
                FROM TableA AS a
                LEFT JOIN TableB AS b ON a.Id = b.AId
                LEFT JOIN TableC AS c ON b.Id = c.BId
                LEFT JOIN TableD AS d ON d.BId = a.Id;
                """;
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(sqlStr));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        ParseTree tree = parser.query();
        System.out.println(tree.toStringTree(parser));

        SQLtoCSharpVisitor visitor = new SQLtoCSharpVisitor();
        String csharp = visitor.visit(tree);
        System.out.println(csharp);
    }
}