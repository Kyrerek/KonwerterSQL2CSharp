package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import antlr.SQLParser;
import antlr.SQLLexer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.model.SQLErrorListener;
import org.model.SQLtoCSharpVisitor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SQL2C#");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        RSyntaxTextArea editTxtArea = new RSyntaxTextArea(20, 50);
        editTxtArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        editTxtArea.setCodeFoldingEnabled(true);
        editTxtArea.setTabSize(4);

//        String sqlStr = """
//        SELECT DISTINCT name, age FROM users WHERE (age > 18 AND age is NOT NULL) or name NOT LIKE '%_OO%' and age BETWEEN 10 AND 100;
//        SELECT Dept, AVG(Salary) AS average FROM Emps GROUP BY Dept;
//        SELECT e.DeptId FROM Employees AS e JOIN Departments AS d ON e.DeptId = d.Id;
//        SELECT e.DeptId FROM Employees AS e LEFT JOIN Departments AS d ON e.DeptId = d.Id AND e.City = d.City;
//        SELECT e.DeptId FROM Employees AS e RIGHT JOIN Departments AS d ON e.DeptId = d.Id AND e.City = d.City;
//        SELECT Dept, AVG(Salary) AS average FROM Emps GROUP BY Dept ORDER BY average DESC, Dept;
//        DELETE FROM users WHERE name LIKE '%ki%' and age NOT BETWEEN 20 AND 50;
//        UPDATE users
//        SET age = age * 1.10, name = 'UNKNOWN', country = NULL
//        WHERE name LIKE '%____%';
//        INSERT INTO Users (name,age,country,code)
//        VALUES
//        ('User1', 10, 'Norway'),
//        ('Per Olsen', 20, NULL),
//        ('Finn Egan', 50, 'Poland');
//        SELECT a.Name, b.Price, c.Category
//        FROM TableA AS a
//        JOIN TableB AS b ON a.Id = b.AId
//        JOIN TableC AS c ON b.Id = c.BId
//        JOIN TableD AS d ON d.BId = a.Id;
//        SELECT a.Name, b.Price, c.Category
//        FROM TableA AS a
//        LEFT JOIN TableB AS b ON a.Id = b.AId
//        LEFT JOIN TableC AS c ON b.Id = c.BId
//        LEFT JOIN TableD AS d ON d.BId = a.Id;
//        CREATE TABLE Departments (
//            Id INT PRIMARY KEY
//        );
//        CREATE TABLE Employees (
//            Id INT PRIMARY KEY,
//            Username VARCHAR(50) NOT NULL UNIQUE,
//            Salary DECIMAL DEFAULT 3000.00 UNIQUE,
//            IsActive BOOLEAN DEFAULT TRUE,
//            DeptId INT REFERENCES Departments(Id)
//        );
//        """;

        String sqlStr = """
                CREATE TABLE uzytkownicy (
                    id INT PRIMARY KEY,
                    imie VARCHAR(50),
                    wiek INT
                );
                INSERT INTO uzytkownicy (id, imie, wiek) VALUES (1, 'Anna', 25);
                INSERT INTO uzytkownicy (id, imie, wiek) VALUES (2, 'Jan', 30);
                
                SELECT * FROM uzytkownicy;
                
                UPDATE uzytkownicy SET wiek = 26 WHERE id = 1;
                
                SELECT * FROM uzytkownicy;
                
                DELETE FROM uzytkownicy WHERE id = 2;
                
                SELECT * FROM uzytkownicy;
                """;
        editTxtArea.setText(sqlStr);

        RTextScrollPane editScrollP = new RTextScrollPane(editTxtArea);

        JPanel sqlPanel = new JPanel(new BorderLayout(0, 4));
        sqlPanel.add(new JLabel("Wejście SQL"), BorderLayout.NORTH);
        sqlPanel.add(editScrollP, BorderLayout.CENTER);

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 50);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSHARP);
        textArea.setCodeFoldingEnabled(true);
        textArea.setEditable(false);
        textArea.setTabSize(4);

        RTextScrollPane textScrollP = new RTextScrollPane(textArea);

        JPanel csPanel = new JPanel(new BorderLayout(0, 4));
        csPanel.add(new JLabel("Wynik C#"), BorderLayout.NORTH);
        csPanel.add(textScrollP, BorderLayout.CENTER);

        RSyntaxTextArea errorOrOutArea = new RSyntaxTextArea(5, 50);
        errorOrOutArea.setEditable(false);
        errorOrOutArea.setForeground(Color.RED);
        errorOrOutArea.setTabSize(4);

        RTextScrollPane errorScrollP = new RTextScrollPane(errorOrOutArea);

        JPanel errorPanel = new JPanel(new BorderLayout(0, 4));
        errorPanel.setPreferredSize(new Dimension(0, 150));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        errorPanel.add(new JLabel("Błędy/Output"), BorderLayout.NORTH);
        errorPanel.add(errorScrollP, BorderLayout.CENTER);

        JButton button = new JButton("Generuj C#");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addActionListener(e -> {
            try {
                textArea.setText(sqlToCSharp(editTxtArea.getText(), errorOrOutArea));
            } catch (Exception ex) {
                errorOrOutArea.setForeground(Color.RED);
                errorOrOutArea.setText("Nieoczekiwany błąd: " + ex.getMessage());
            }
        });

        JButton runButton = new JButton("Run C#");
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.addActionListener(e -> {
            try {
                errorOrOutArea.setForeground(Color.BLACK);
                errorOrOutArea.setText(runCreateCSharp(textArea.getText()));
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.add(sqlPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(runButton);
        buttonPanel.add(Box.createVerticalGlue());
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        topPanel.add(csPanel, BorderLayout.EAST);

        sqlPanel.setPreferredSize(new Dimension(0, 0));
        csPanel.setPreferredSize(new Dimension(0, 0));
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0.45;
        topPanel.add(sqlPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.1;
        gbc.insets = new Insets(0, 10, 0, 10);
        topPanel.add(buttonPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.45;
        gbc.insets = new Insets(0, 0, 0, 0);
        topPanel.add(csPanel, gbc);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(errorPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public static String sqlToCSharp(String sql, RSyntaxTextArea errorArea) {
        if (sql == null || sql.isBlank()) {
            errorArea.setText("Nie podano żadnego wyrażenia SQL.");
            return "";
        }
        SQLErrorListener errorListener = new SQLErrorListener();
        SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SQLParser parser = new SQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseTree tree = parser.query();

        SQLtoCSharpVisitor visitor = new SQLtoCSharpVisitor();
        String result = visitor.visit(tree);

        List<String> allErrors = new ArrayList<>(errorListener.getErrors());
        allErrors.addAll(visitor.getErrors());

        if (!allErrors.isEmpty()) {
            errorArea.setText(String.join("\n", allErrors));
        } else {
            errorArea.setText("");
        }

        return result;
    }


    public static String runCreateCSharp(String csStr) throws IOException, InterruptedException {
        //csStr = "Console.WriteLine(\"Hello World!\");";
        String fullCsStr = """
    using System.ComponentModel.DataAnnotations;
    using Microsoft.EntityFrameworkCore;
    using EFCore.BulkExtensions;
    using System.Reflection;
    
    using var db = new Baza();
    db.Database.OpenConnection();
    db.Database.EnsureCreated();
    """;
        fullCsStr += csStr;
        fullCsStr +=  """
    class Baza : DbContext
    {
        protected override void OnConfiguring(DbContextOptionsBuilder o) => o.UseSqlite("Data Source=:memory:");
    
        protected override void OnModelCreating(ModelBuilder mb) =>
            Assembly.GetExecutingAssembly().GetTypes()
                .Where(t => t.IsClass && t.IsPublic && !t.IsAbstract && !typeof(DbContext).IsAssignableFrom(t))
                .ToList()
                .ForEach(t => mb.Entity(t).ToTable(t.Name));
    }
    
    public static class PokazywarkaExtensions
    {
        public static void Show<T>(this IQueryable<T> query) where T: class
        {
            var dane = query.AsNoTracking().ToList();
            Console.WriteLine($"\\n--- TABELA: {typeof(T).Name.ToUpper()} (Rekordów: {dane.Count}) ---");
            
           
            var properties = typeof(T).GetProperties();
    
            foreach (var element in dane)
            {
                var linia = string.Join(", ", properties.Select(p => $"{p.Name}: {p.GetValue(element)}"));
                Console.WriteLine(linia);
            }
        }
    }
    """;
        Path csPath = Paths.get("Template","Program.cs");
        Files.writeString(csPath,fullCsStr);
        ProcessBuilder pb = new ProcessBuilder("dotnet", "run","--project", "Template");
        pb.redirectErrorStream(true);

        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        var outCs = new StringBuilder();
        String curLine = reader.readLine();
        while (curLine != null){
            outCs.append(curLine);
            outCs.append("\n");
            curLine = reader.readLine();
        }
        System.out.println(outCs.toString());
        // Oczekiwanie na zakończenie procesu
        int exitCode = process.waitFor();
        outCs.append("Proces zakończony z kodem: ");
        outCs.append(exitCode);
        outCs.append("\n");
        return outCs.toString();
    }
}