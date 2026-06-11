# KonwerterSQL2CSharp
## Zespół
1. Eryk Łęski - erykleski@student.agh.edu.pl
2. Michał Machalski - mmasalski@student.agh.edu.pl
## Dane kontaktowe
- erykleski@student.agh.edu.pl
## Założenia programu
### Ogólne cele programu
Program ma za zadanie przekonwertowanie zapytań SQL do kodu w języku C#, a dokładnie do LINQ Method Syntax. Będą to zapytania z wykorzystaniem: 
- SELECT, 
- INSERT, 
- DELETE,
- UPDATE.

Także można stworzyć tabelę do wykonywania na niej zapytań.

Dodatkowo do tych zapytań można używać: 
- JOIN, 
- WHERE, 
- funkcje agregujące, 
- HAVING, 
- ORDER BY, 
- GROUP BY, 
- aliasowania,
- VALUES,
- SET. 
### Rodzaj translatora
- Kompilator
### Planowany wynik działania programu
- kompilator zapytań SQL do języka C#
### Język implementacji
- Java
### Generator parsera
- ANTLR4
## Opis tokenów
### Tokeny
|     Kod      | Opis                                                                                                  |               Przykłady                |
|:------------:|-------------------------------------------------------------------------------------------------------|:--------------------------------------:|
|    `NUM`     | Liczba rzeczywista. Regex: `[0-9]+\.[0-9]+`                                                           |            3.14, 0.99, 42.0            |
|    `INT`     | Liczba całkowita. Regex: `[0-9]+`                                                                     |             10000, 3453, 1             |
|    `STR`     | Ciąg znaków jako wartość. Regex: `'([^'\r\n]*)'`                                                      |     'Ciąg znaków', 'Kuba', 'owocE'     |
|     `ID`     | Ciąg znaków jako nazwa kolumny lub tabeli. Regex: `[A-Za-z_][A-Za-z0-9_]*`                            |    persons, name_of_fruits, weight     |
|    `TRUE`    | Wartość logiczna dla prawdy                                                                           |             `true`, `TRUE`             |
|   `FALSE`    | Wartość logiczna dla fałszu                                                                           |            `false`, `FALSE`            |
|    `NULL`    | Nienznana lub brakująca wartość                                                                       |             `null`, `NULL`             |
|   `SELECT`   | Słowo kluczowe "select"                                                                               |           `select`, `SELECT`           |
|  `DISTINCT`  | Słowo kluczowe "distinct"                                                                             |         `distinct`, `DISTINCT`         |
|    `FROM`    | Słowo kluczowe "from"                                                                                 |             `from`, `FROM`             |
|     `AS`     | Słowo kluczowe "as", aliasowanie nazw                                                                 |               `as`, `AS`               |
|    `JOIN`    | Słowo kluczowe "join"                                                                                 |             `join`, `JOIN`             |
|   `INNER`    | Słowo kluczowe "inner"                                                                                |            `inner`, `INNER`            |
|   `OUTER`    | Słowo kluczowe "outer"                                                                                |            `outer`, `OUTER`            |
|    `LEFT`    | Słowo kluczowe "left"                                                                                 |             `left`, `LEFT`             |
|   `RIGHT`    | Słowo kluczowe "right"                                                                                |            `right`, `RIGHT`            |
|     `ON`     | Słowo kluczowe "on"                                                                                   |               `on`, `ON`               |
|   `WHERE`    | Słowo kluczowe "where"                                                                                |            `where`, `WHERE`            |
|    `AND`     | Słowo kluczowe "and", koniunkcja, używane w formułach logicznych                                      |              `and`, `AND`              |
|     `OR`     | Słowo kluczowe "or", alternatywa, używane w formułach logicznych                                      |               `or`, `OR`               |
|    `NOT`     | Słowo kluczowe "not", zaprzeczenie, używane w formułach logicznych                                    |              `not`, `NOT`              |
|    `LIKE`    | Słowo kluczowe "like", używane w formułach logicznych do znajdowania wzorców                          |             `like`, `LIKE`             |
|     `IS`     | Słowo kluczowe "is", używane do sprawdzenie czy wartość jest "null"                                   |               `is`, `IS`               |
|  `BETWEEN`   | Słowo kluczowe "between", sprawdza czy wartość mieści się w danym przedziale zamkniętym               |          `between`, `BETWEEN`          |
|   `ORDER`    | Słowo kluczowe "order"                                                                                |            `order`, `ORDER`            |
|     `BY`     | Słowo kluczowe "by", występuje po "order" i "group"                                                   |               `by`, `BY`               |
|    `ASC`     | Słowo kluczowe "asc", sortowanie rosnące, używane po "order by"                                       |              `asc`, `ASC`              |
|    `DESC`    | Słowo kluczowe "desc", sortowanie malejące, używane po "order by"                                     |             `desc`,`DESC`              |
|   `GROUP`    | Słowo kluczowe "group"                                                                                |            `group`, `GROUP`            |
|    `MIN`     | Słowo kluczowe "min()", najmniejsza wartość                                                           |              `min`, `MIN`              |
|    `MAX`     | Słowo kluczowe "max()", maksymalna wartość                                                            |              `max`, `MAX`              |
|   `COUNT`    | Słowo kluczowe "count()", ilość rekordów                                                              |            `count`, `COUNT`            |
|    `SUM`     | Słowo kluczowe "sum()", suma wartości                                                                 |              `sum`, `SUM`              |
|    `AVG`     | Słowo kluczowe "avg()", średnia z wartości                                                            |              `avg`, `AVG`              |
|   `HAVING`   | Słowo kluczowe "having"                                                                               |           `having`, `HAVING`           |
|   `UPDATE`   | Słowo kluczowe "update"                                                                               |           `update`, `UPDATE`           |
|    `SET`     | Słowo kluczowe "set", używane razem z "update"                                                        |              `set`, `SET`              |
|   `DELETE`   | Słowo kluczowe "delete"                                                                               |           `delete`, `DELETE`           |
|   `INSERT`   | Słowo kluczowe "insert"                                                                               |           `insert`, `INSERT`           |
|    `INTO`    | Słowo kluczowe "into", używane zaraz po "insert"                                                      |             `into`, `INTO`             |
|   `VALUES`   | Słowo kluzowe "values", używane do określania wartości do dodania w "insert into"                     |           `values`, `VALUES`           |
|   `CREATE`   | Słowo kluczowe "create"                                                                               |           `create`, `CREATE`           |
|   `TABLE`    | Słowo kluczowe "table", używane po "create" do określenia tworzenia struktury tabeli                  |            `table`, `TABLE`            |
|  `PRIMARY`   | Słowo kluczowe "primary", występuje przed słowem "key" przy definicji klucza głównego kolumny         |          `primary`, `PRIMARY`          |
|    `KEY`     | Słowo kluczowe "key", występuje po słowie "primary" przy definicji klucza głównego kolumny            |              `key`,`KEY`               |
|   `UNIQUE`   | Słowo kluczowe "unique", ograniczenie wymuszające unikalność wartości w danej kolumnie                |           `unique`, `UNIQUE`           |
|  `DEFAULT`   | Słowo kluczowe "default", służy do określania domyślnej wartości dla kolumny w przypadku braku danych |          `default`, `DEFAULT`          |
| `REFERENCES` | Słowo kluczowe "references", używane do definiowania relacji i kluczy obcych między tabelami          |       `references`, `REFERENCES`       |
|  `INT_TYPE`  | Typ danych dla liczb całkowitych.                                                                     |   `int`, `integer`, `INT`, `INTEGER`   |
|  `VARCHAR`   | Typ danych dla ciągów znaków o zmiennej lub określonej długości                                       |          `varchar`, `VARCHAR`          |
|  `NUMERIC`   | Typ danych dla liczb rzeczywistych                                                                    | `decimal`, `float`, `DECIMAL`, `FLOAT` |
|  `BOOLEAN`   | Typ danych dla wartości logicznych                                                                    |          `boolean`, `BOOLEAN`          |                                                            
|    `PLUS`    | Znak dodawania                                                                                        |                  `+`                   |
|   `MINUS`    | Znak odejmowania                                                                                      |                  `-`                   |
|    `MULT`    | Znak mnożenia                                                                                         |                  `*`                   |
|    `DIV`     | Znak dzielenia                                                                                        |                  `/`                   |
|  `GREATER`   | Znak większości                                                                                       |                  `>`                   |
|    `LESS`    | Znak mniejszości                                                                                      |                  `<`                   |
|  `EGREATER`  | Znak większości bądź równości                                                                         |                  `>=`                  |
|   `ELESS`    | Znak mniejszości bądź równości                                                                        |                  `<=`                  |
|    `EQL`     | Znak równości                                                                                         |                  `=`                   |
|    `NEQL`    | Znak nierówności                                                                                      |                  `<>`                  |
|  `LBRACKET`  | Nawias otwierający                                                                                    |                  `(`                   |
|  `RBRACKET`  | Nawias zamykający                                                                                     |                  `)`                   |
|   `COMMA`    | Przecinek, używany do odzielenia np. nazw kolumn lub tabel                                            |                  `,`                   |
|    `PER`     | Kropka, separator używany do kwalifikowania nazw obiektów                                             |                  `.`                   |
|    `END`     | Średnik, oznaczenie końca kwerendy                                                                    |                  `;`                   |
|     `WS`     | Białe znaki. Regex: `[ \t\r\n]+`                                                                      |         spacje, tabulacje, CF          |
|    `ERR`     | Nieznane znaki                                                                                        |                `@`, `$`                |
### Reguły parsera
|         Kod         | Opis                                                                                     |                                     Przykłady                                     |
|:-------------------:|------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------:|
|       `query`       | Cała kwerenda                                                                            |         `select a, tabela.b, c as d from tabela where a > b order by a;`          |
|    `select_stm`     | Wyrażenie SELECT                                                                         |                     `select a, b, c from tabela where a > b;`                     |
|    `select_list`    | Zawartość listy wybranych do wyświetlenia kolumn/wartości                                |                                  `a, b, min(c)`                                   |
|    `select_item`    | Element z listy wybranych do wyświetlenia kolumn/wartości                                |                             `a`, `tabela.b`, `min(c)`                             |
|      `column`       | Sama nazwa kolumny lub z odpowiadającą dla niej tabelą                                   |                                  `a`, `tabela.b`                                  |
|     `agg_func`      | Funkcje agregujące                                                                       |                             `min(a)`, `sum(tabela.b)`                             |
|     `from_stm`      | Wyrażenie FROM wraz z tabelą                                                             |                                   `from tabela`                                   |
|     `where_stm`     | Wyrażenie WHERE wraz z formułą logiczną                                                  |                   `where a > b`, `where b = min(a) and c <= 7`                    |
|     `join_stm`      | Wyrażenie JOIN z ewentualnym jego typem                                                  | `left join tabela2 on tabela.a=tabela2.b`, `join tabela2 on tabela.a = tabela2.a` |
|     `join_bef`      | Różne typy JOIN'ów                                                                       |                          `left inner`, `right`, `outer`                           |
|    `groupby_stm`    | Wyrażenie GROUP BY wraz z listą kolumn                                                   |                           `group by a, b`, `group by c`                           |
|    `logic_form`     | Formuła logiczna                                                                         |                `a > b and a < b`, `b = min(a) or c <> c`, `c <= 7`                |
|     `logic_or`      | Reguła do ewentualnego dodawania OR                                                      |                                  `a=b or c < 4`                                   |
|     `logic_and`     | Reguła do ewentualnego dodawania AND                                                     |                                `6 <> a AND 7 < 10`                                |
|     `logic_not`     | Reguła do ewentualnego dodawania NOT                                                     |                                   `NOT a < 45`                                    |
|    `logic_atom`     | Pojedyncza część formuły logicznej, gdzie można dodać nawiasowanie                       |                             `a=b`, `(tabela.c < 200)`                             |
|     `logic_cmp`     | Reguła do wyboru rodzaju porównywania                                                    |                    `a between 10 and 200`, `b is null`, `y<>x`                    |
| `logic_simple_cmp`  | Regyła do wyboru rodzaju prostego porównywania wartości                                  |                                  `a<>b`, `c<=10`                                  |
| `logic_between_cmp` | Wyrażenie BETWEEN                                                                        |       `a not BETWEEN tabela2.alfa and tabela3.beta`, `a between 10 and 200`       |
|  `logic_like_cmp`   | Wyrażenie LIKE                                                                           |                        `a not like 'a%'`, `b like 'beta_'`                        |
|  `logic_null_cmp`   | Reguła do porównywania wartości z nullem                                                 |                           `a is null`, `b is not null`                            |
|     `item_form`     | Reguła do ewentualnego dodawania operacji matematycznych                                 |                                 `a+10`, `8*10+b`                                  |
|  `item_plus_minus`  | Reguła do poprawnego dodawania i odejmowania                                             |                                  `a+b`, `4*c-5`                                   |
|  `item_multi_div`   | Reguła do poprawnego mnożenia i dzielenia                                                |                                 `a*10`, `65/c+9`                                  |
|     `item_atom`     | Ewentualna negacja wartości lub dodanie nawiasowania dla operacji matematycznych         |                                    `-a*(10+b)`                                    |
|       `item`        | Wartość lub kolumna do porównywania                                                      |                          `tabela.a`, `199`, `'apostol'`                           |
|     `order_stm`     | Wyrażenie ORDER BY wraz z listą kolumn i wyborem sortowania                              |                        `order by a, b`, `order by a desc`                         |
|    `order_list`     | Jedna lub więcej kolumn według, których odbędzie się sortowanie wraz z trybem sortowania |                                 `a, b DESC`, `a`                                  |
|    `order_item`     | Kolumna i ewentualnie jej tryb sortowania                                                |                                   `a`, `b desc`                                   |
|    `having_stm`     | Wyrażenie HAVING wraz z formułą logiczną                                                 |                         `having a=b and count(a)=max(b)`                          |


## Gramatyka formatu
[GRAMATYKA](https://github.com/Kyrerek/KonwerterSQL2CSharp/blob/main/main/src/main/antlr/SQL.g4)

## Stosowane generatory i pakiety zewnętrzne

### Generator parsera
- **ANTLR4** w wersji 4.13.1 - generator skanerów i parserów
- Strona: https://www.antlr.org/
- Gramatyka `SQL.g4` → automatyczne generowanie `SQLLexer` i `SQLParser` w Javie z obsługą wzorca Visitor

### Pakiety zewnętrzne - Java (kompilator + UI)
| Pakiet | Wersja | Zastosowanie |
|--------|--------|--------------|
| `org.antlr:antlr4` | 4.13.1 | Generator parsera (narzędzie budowania) |
| `org.antlr:antlr4-runtime` | 4.13.1 | Runtime ANTLR4 do uruchamiania parsera |
| `com.fifesoft:rsyntaxtextarea` | 3.3.3 | Edytor kodu z podświetlaniem składni SQL i C# w UI |

### Pakiety zewnętrzne - C# (wygenerowany kod)
| Pakiet | Wersja | Zastosowanie |
|--------|--------|--------------|
| `Microsoft.EntityFrameworkCore.Sqlite` | 10.0.9 | ORM + provider SQLite |
| `EFCore.BulkExtensions` | 10.0.1 | Operacje `BulkInsert` dla INSERT |

---

## Instrukcja obsługi

### Wymagania
- Java 21+
- Gradle 8+
- .NET 10 SDK

### Uruchomienie

1. Sklonuj repozytorium:
```bash
   git clone https://github.com/Kyrerek/KonwerterSQL2CSharp
   cd KonwerterSQL2CSharp/main
```

2. Uruchom aplikację:
```bash
   ./gradlew run
```
   Otworzy się okno graficzne aplikacji.

### Obsługa interfejsu

Aplikacja składa się z trzech sekcji:

- **Wejście SQL** (lewa strona) — edytor z podświetlaniem składni SQL
- **Wynik C#** (prawa strona) — wygenerowany kod C# z podświetlaniem składni; pole tylko do odczytu
- **Błędy/Output** (dół) — lista błędów kompilacji SQL (na czerwono) lub wynik wykonania programu C# (na czarno)

Przyciski:
- **Generuj C#** — konwertuje zawartość edytora SQL do kodu C#
- **Run C#** — zapisuje wygenerowany kod do pliku `Template/Program.cs` i uruchamia go przez `dotnet run`

---

## Przykład użycia
### Prosty przykład
```sql
SELECT * FROM T;
```
### Przykład 1
```sql
CREATE TABLE Users (
    Id INT PRIMARY KEY,
    Name VARCHAR(50) NOT NULL,
    Age INT NOT NULL
);

CREATE TABLE Orders (
    Id INT PRIMARY KEY,
    Product VARCHAR(100) NOT NULL,
    Price DECIMAL NOT NULL,
    UserId INT REFERENCES Users(Id)
);

INSERT INTO Users (Id, Name, Age) VALUES (1, 'Anna', 25);
INSERT INTO Users (Id, Name, Age) VALUES (2, 'Jan', 30);

INSERT INTO Orders (Id, Product, Price, UserId) VALUES (102, 'Telefon', 1500, 2);
INSERT INTO Orders (Id, Product, Price, UserId) VALUES (101, 'Laptop', 3500, 1);

SELECT u.Name, o.Product
FROM Users AS u
JOIN Orders AS o ON u.Id = o.UserId
WHERE u.Age > 20
ORDER BY o.Price DESC;

UPDATE Users SET Age = 26 WHERE Id = 1;

DELETE FROM Orders WHERE UserId = 1;
```
#### Przykładowe wyjście
```csharp
using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations.Schema;
using System.Reflection;
using System.Globalization;

using var db = new Baza();
db.Database.OpenConnection();
db.Database.EnsureCreated();

db.AddRange(new Users[] {
	new Users { Id = 1, Name = "Anna", Age = 25 }
});
db.SaveChanges();

db.AddRange(new Users[] {
	new Users { Id = 2, Name = "Jan", Age = 30 }
});
db.SaveChanges();

db.AddRange(new Orders[] {
	new Orders { Id = 102, Product = "Telefon", Price = 1500, UserId = 2 }
});
db.SaveChanges();

db.AddRange(new Orders[] {
	new Orders { Id = 101, Product = "Laptop", Price = 3500, UserId = 1 }
});
db.SaveChanges();

db.Set<Users>()
	.Join(
		db.Set<Orders>(),
		u => u.Id,
		o => o.UserId,
		(u, o) => new {u, o}
	)
	.Where(temp => temp.u.Age>20)
	.OrderByDescending(temp => temp.o.Price)
	.Select(temp => new {temp.u.Name, temp.o.Product}).Show();

db.Set<Users>()
	.Where(temp => temp.Id==1)
	.ExecuteUpdate(setters => setters
		.SetProperty(temp => temp.Age, 26)
	);

db.Set<Orders>()
	.Where(temp => temp.UserId==1)
	.ExecuteDelete();

public class Users
{
	[Key]
	public int Id {get; set;}

	public required string Name {get; set;}

	public required int Age {get; set;}
}

public class Orders
{
	[Key]
	public int Id {get; set;}

	public required string Product {get; set;}

	public required decimal Price {get; set;}

	public int? UserId {get; set;}

	[ForeignKey(nameof(UserId))]
	public virtual Users? UsersFK {get; set;}

}

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

        Console.WriteLine($"\n--- WYNIK ZAPYTANIA (Rekordów: {dane.Count}) ---");

        var mainType = typeof(T);

        if (mainType.IsPrimitive || mainType == typeof(string) || mainType == typeof(decimal) || 
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

```
### Przykład 2
```sql
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

```
