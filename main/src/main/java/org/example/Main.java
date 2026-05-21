package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr.SQLParser;
import antlr.SQLLexer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.model.SQLtoCSharpVisitor;

import javax.swing.*;
import java.awt.*;


public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SQL2C#");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        RSyntaxTextArea editTxtArea = new RSyntaxTextArea(20,50);
        editTxtArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        editTxtArea.setCodeFoldingEnabled(true);
        editTxtArea.setTabSize(4);

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
        editTxtArea.setText(sqlStr);


        RTextScrollPane editScrollP = new RTextScrollPane(editTxtArea);

        JButton button = new JButton("Generate C#");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        RSyntaxTextArea textArea = new RSyntaxTextArea(20,50);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
        textArea.setCodeFoldingEnabled(true);
        textArea.setEditable(false);
        textArea.setTabSize(4);

        RTextScrollPane textScrollP = new RTextScrollPane(textArea);

        button.addActionListener(e ->
                textArea.setText(sqlToCSharp(editTxtArea.getText())));

        panel.add(editScrollP);
        panel.add(Box.createVerticalStrut(20));
        panel.add(button);
        panel.add(Box.createVerticalStrut(20));
        panel.add(textScrollP);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static String sqlToCSharp(String sql) {
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        ParseTree tree = parser.query();

        SQLtoCSharpVisitor visitor = new SQLtoCSharpVisitor();
        return visitor.visit(tree);
    }
}