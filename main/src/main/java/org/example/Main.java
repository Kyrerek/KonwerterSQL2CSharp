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
        String fullTest = """
    CREATE TABLE Categories (
        Id INT PRIMARY KEY,
        Name VARCHAR(50) NOT NULL,
        Description VARCHAR(200)
    );
 
    CREATE TABLE Products (
        Id INT PRIMARY KEY,
        Name VARCHAR(100) NOT NULL,
        Price DECIMAL NOT NULL,
        Stock INT NOT NULL DEFAULT 0,
        CategoryId INT REFERENCES Categories(Id)
    );
 
    CREATE TABLE Customers (
        Id INT PRIMARY KEY,
        FirstName VARCHAR(50) NOT NULL,
        LastName VARCHAR(50) NOT NULL,
        Email VARCHAR(100) NOT NULL UNIQUE,
        Age INT
    );
 
    CREATE TABLE Orders (
        Id INT PRIMARY KEY,
        Quantity INT NOT NULL,
        TotalPrice DECIMAL NOT NULL,
        CustomerId INT REFERENCES Customers(Id),
        ProductId INT REFERENCES Products(Id)
    );
 
    INSERT INTO Categories (Id, Name, Description) VALUES (1, 'Elektronika', 'Sprzet elektroniczny');
    INSERT INTO Categories (Id, Name, Description) VALUES (2, 'Ksiazki', 'Literatura i podreczniki');
    INSERT INTO Categories (Id, Name, Description) VALUES (3, 'Odziez', 'Ubrania i akcesoria');
 
    INSERT INTO Products (Id, Name, Price, Stock, CategoryId) VALUES (1, 'Laptop', 3500, 10, 1);
    INSERT INTO Products (Id, Name, Price, Stock, CategoryId) VALUES (2, 'Telefon', 1500, 25, 1);
    INSERT INTO Products (Id, Name, Price, Stock, CategoryId) VALUES (3, 'Java Book', 89, 100, 2);
    INSERT INTO Products (Id, Name, Price, Stock, CategoryId) VALUES (4, 'T-Shirt', 49, 200, 3);
    INSERT INTO Products (Id, Name, Price, Stock, CategoryId) VALUES (5, 'Headphones', 299, 50, 1);
 
    INSERT INTO Customers (Id, FirstName, LastName, Email, Age) VALUES (1, 'Anna', 'Kowalska', 'anna@mail.com', 25);
    INSERT INTO Customers (Id, FirstName, LastName, Email, Age) VALUES (2, 'Jan', 'Nowak', 'jan@mail.com', 30);
    INSERT INTO Customers (Id, FirstName, LastName, Email, Age) VALUES (3, 'Maria', 'Wisniewska', 'maria@mail.com', 22);
    INSERT INTO Customers (Id, FirstName, LastName, Email, Age) VALUES (4, 'Piotr', 'Zielinski', 'piotr@mail.com', 35);
 
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (1, 1, 3500, 1, 1);
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (2, 2, 3000, 2, 2);
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (3, 1, 89, 1, 3);
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (4, 3, 147, 3, 4);
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (5, 1, 299, 4, 5);
    INSERT INTO Orders (Id, Quantity, TotalPrice, CustomerId, ProductId) VALUES (6, 2, 178, 2, 3);
 
    SELECT * FROM Customers WHERE Age > 25;
 
    SELECT c.FirstName, c.LastName, c.Email FROM Customers AS c WHERE c.Age > 24;
 
    SELECT * FROM Products ORDER BY Price ASC;
 
    SELECT * FROM Products ORDER BY Price DESC;
 
    SELECT * FROM Products ORDER BY CategoryId ASC, Price DESC;
 
    SELECT c.FirstName, o.TotalPrice FROM Customers AS c JOIN Orders AS o ON c.Id = o.CustomerId;
 
    SELECT c.FirstName, p.Name, o.Quantity FROM Customers AS c JOIN Orders AS o ON c.Id = o.CustomerId JOIN Products AS p ON o.ProductId = p.Id;
 
    SELECT c.FirstName, o.TotalPrice FROM Customers AS c LEFT JOIN Orders AS o ON c.Id = o.CustomerId;
 
    SELECT o.TotalPrice, p.Name FROM Orders AS o RIGHT JOIN Products AS p ON o.ProductId = p.Id;
 
    SELECT c.FirstName, o.TotalPrice FROM Customers AS c JOIN Orders AS o ON c.Id = o.CustomerId ORDER BY o.TotalPrice DESC;
 
    SELECT o.CustomerId, COUNT(o.Id) FROM Orders AS o GROUP BY o.CustomerId;
 
    SELECT o.CustomerId, SUM(o.TotalPrice) FROM Orders AS o GROUP BY o.CustomerId;
 
    SELECT o.ProductId, AVG(o.TotalPrice) FROM Orders AS o GROUP BY o.ProductId;
 
    SELECT o.ProductId, MIN(o.TotalPrice), MAX(o.TotalPrice) FROM Orders AS o GROUP BY o.ProductId;
 
    SELECT o.CustomerId, SUM(o.TotalPrice) FROM Orders AS o GROUP BY o.CustomerId HAVING SUM(o.TotalPrice) > 500;
 
    SELECT c.FirstName AS Imie, c.LastName AS Nazwisko FROM Customers AS c;
 
    SELECT DISTINCT CustomerId FROM Orders;
 
    UPDATE Products SET Stock = 5 WHERE Id = 1;
 
    UPDATE Customers SET Age = 26 WHERE Id = 1;
 
    DELETE FROM Orders WHERE CustomerId = 3;
 
    SELECT * FROM Orders;
 
    DELETE FROM Customers WHERE Id = 3;
 
    SELECT * FROM Customers;
                """;
        String errorCreateTest = """
            CREATE TABLE Users (
                Id INT PRIMARY KEY,
                Name VARCHAR(50) NOT NULL,
                Age INT NOT NULL
            );

            CREATE TABLE Users (
                Id INT PRIMARY KEY,
                Email VARCHAR(100)
            );

            CREATE TABLE Orders (
                Id INT PRIMARY KEY,
                Id VARCHAR(50) NOT NULL,
                Amount DECIMAL PRIMARY KEY,
                UserId INT REFERENCES Payments(Id),
                Stock INT DEFAULT 'hello'
            );
            """;

        String errorSelectTest = """
            CREATE TABLE Customers (
                Id INT PRIMARY KEY,
                FirstName VARCHAR(50) NOT NULL,
                LastName VARCHAR(50) NOT NULL,
                Age INT
            );

            CREATE TABLE Orders (
                Id INT PRIMARY KEY,
                TotalPrice DECIMAL NOT NULL,
                CustomerId INT REFERENCES Customers(Id)
            );

            SELECT c.NonExistent FROM Customers AS c;

            SELECT c.FirstName AS Imie, c.LastName AS Imie FROM Customers AS c;

            SELECT c.FirstName, c.LastName, SUM(o.TotalPrice)
            FROM Customers AS c
            JOIN Orders AS o ON c.Id = o.CustomerId
            GROUP BY c.FirstName;

            SELECT * FROM NonExistentTable;
            """;

        String errorMixedTest = """
            CREATE TABLE Categories (
                Id INT PRIMARY KEY,
                Name VARCHAR(50) NOT NULL
            );

            CREATE TABLE Products (
                Id INT PRIMARY KEY,
                Name VARCHAR(100) NOT NULL,
                CategoryId INT REFERENCES Categories(NonExistentColumn)
            );

            INSERT INTO Categories (Id, Name) VALUES (1, 'Elektronika');
            INSERT INTO Products (Id, Name, CategoryId) VALUES (1, 'Laptop', 1);

            SELECT p.Name, c.Name
            FROM Products AS p
            JOIN Categories AS c ON p.CategoryId = c.Id
            JOIN Categories AS c ON p.CategoryId = c.Id;
            """;

//        editTxtArea.setText(fullTest);
//        editTxtArea.setText(errorCreateTest);
//        editTxtArea.setText(errorSelectTest);
//        editTxtArea.setText(errorMixedTest);

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
        errorOrOutArea.setFont(errorOrOutArea.getFont().deriveFont(13f));

        RTextScrollPane errorScrollP = new RTextScrollPane(errorOrOutArea);

        JPanel errorPanel = new JPanel(new BorderLayout(0, 4));
        errorPanel.setPreferredSize(new Dimension(0, 220));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        errorPanel.add(new JLabel("Błędy/Output"), BorderLayout.NORTH);
        errorPanel.add(errorScrollP, BorderLayout.CENTER);

        JButton button = new JButton("Generuj C#");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.addActionListener(e -> {
            try {
                errorOrOutArea.setForeground(Color.RED);
                String csStr = sqlToCSharp(editTxtArea.getText(), errorOrOutArea);
                textArea.setText(addExtraCSharp(csStr));
            } catch (Exception ex) {
                errorOrOutArea.setText("Nieoczekiwany błąd: " + ex.getMessage());
            }
        });

        JButton runButton = new JButton("Run C#");
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.addActionListener(e -> {
            try {
                errorOrOutArea.setForeground(Color.BLACK);
                errorOrOutArea.setText(runCreateCSharp(textArea.getText()));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0.45;
        topPanel.add(sqlPanel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(runButton);
        buttonPanel.add(Box.createVerticalGlue());

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
        //csStr = "Console.WriteLine(\"Hello World!\");"
        Path csPath = Paths.get("Template","Program.cs");
        Files.writeString(csPath,csStr);
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

    public static String addExtraCSharp(String csStr){
        String fullCsStr = """
    using System.ComponentModel.DataAnnotations;
    using Microsoft.EntityFrameworkCore;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Reflection;
    using System.Globalization;
    
    using var db = new Baza();
    db.Database.OpenConnection();
    db.Database.EnsureCreated();
    \n""";
        fullCsStr += csStr;
        fullCsStr +=  """
    class Baza : DbContext
    {
        private static Microsoft.Data.Sqlite.SqliteConnection? _connection;
    
        protected override void OnConfiguring(DbContextOptionsBuilder o)
        {
            if (_connection == null)
            {
                _connection = new Microsoft.Data.Sqlite.SqliteConnection("Data Source=:memory:");
                _connection.Open();
            }
            o.UseSqlite(_connection);
        }
    
        protected override void OnModelCreating(ModelBuilder mb)
        {
            Assembly.GetExecutingAssembly().GetTypes()
                .Where(t => t.IsClass && t.IsPublic && !t.IsAbstract && !typeof(DbContext).IsAssignableFrom(t))
                .ToList()
                .ForEach(t => mb.Entity(t).ToTable(t.Name));
    
            foreach (var entityType in mb.Model.GetEntityTypes())
            {
                foreach (var property in entityType.GetProperties())
                {
                    if (property.ClrType == typeof(decimal) || property.ClrType == typeof(decimal?))
                    {
                        property.SetValueConverter(new Microsoft.EntityFrameworkCore.Storage.ValueConversion.ValueConverter<decimal, double>(
                            v => (double)v,
                            v => (decimal)v
                        ));
                    }
                }
            }
        }
    }
    
    
    public static class ShowExtensions
    {
        public static void Show<T>(this IQueryable<T> query)
        {
            List<T> dane;
            if (typeof(T).IsClass && typeof(T) != typeof(string))
            {
                dane = EntityFrameworkQueryableExtensions.AsNoTracking((IQueryable<object>)query).Cast<T>().ToList();
            }
            else
            {
                dane = query.ToList();
            }
    
            Console.WriteLine($"\\n--- WYNIK ZAPYTANIA (Rekordów: {dane.Count}) ---");
    
            var mainType = typeof(T);
    
            if (mainType.IsPrimitive || mainType == typeof(string) || mainType == typeof(decimal) ||\s
                (Nullable.GetUnderlyingType(mainType)?.IsPrimitive == true))
            {
                foreach (var element in dane)
                {
                    if (element == null) continue;
    
                    var formattedVal = element is IFormattable formattable
                        ? formattable.ToString(null, CultureInfo.InvariantCulture)
                        : element.ToString();
    
                    Console.WriteLine(formattedVal);
                }
            }
            else
            {
                var properties = mainType.GetProperties();
    
                foreach (var element in dane)
                {
                    var kolumny = new List<string>();
    
                    foreach (var p in properties)
                    {
                        var val = p.GetValue(element);
                        if (val == null) continue;
    
                        var type = val.GetType();
    
                        if (type.IsClass && type != typeof(string))
                        {
                            var subProps = type.GetProperties()
                                .Where(sp => sp.PropertyType.IsPrimitive || sp.PropertyType == typeof(string) || sp.PropertyType == typeof(decimal));
    
                            foreach (var sp in subProps)
                            {
                                var subVal = sp.GetValue(val);
                                var formattedSubVal = subVal is IFormattable formattableSub
                                    ? formattableSub.ToString(null, CultureInfo.InvariantCulture)
                                    : subVal?.ToString();
    
                                kolumny.Add($"{sp.Name}: {formattedSubVal}");
                            }
                        }
                        else
                        {
                            var formattedVal = val is IFormattable formattable
                                ? formattable.ToString(null, CultureInfo.InvariantCulture)
                                : val.ToString();
    
                            kolumny.Add($"{p.Name}: {formattedVal}");
                        }
                    }
                    Console.WriteLine(string.Join(", ", kolumny));
                }
            }
        }
    }
    """;
        return fullCsStr;
    }
}