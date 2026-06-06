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
|   `CREATE`   | Słowo kluczowe "create", używane do inicjalizacji tworzenia nowych obiektów bazy danych               |           `create`, `CREATE`           |
|   `TABLE`    | Słowo kluczowe "table", używane po "create" do określenia tworzenia struktury tabeli                  |            `table`, `TABLE`            |
|  `PRIMARY`   | Słowo kluczowe "primary", występuje przed słowem "key" przy definicji klucza głównego kolumny         |          `primary`, `PRIMARY`          |
|    `KEY`     | Słowo kluczowe "key", występuje po słowie "primary" przy definicji klucza głównego kolumny            |              `key`,`KEY`               |
|   `UNIQUE`   | Słowo kluczowe "unique", ograniczenie (constraint) wymuszające unikalność wartości w danej kolumnie   |           `unique`, `UNIQUE`           |
|  `DEFAULT`   | Słowo kluczowe "default", służy do określania domyślnej wartości dla kolumny w przypadku braku danych |          `default`, `DEFAULT`          |
| `REFERENCES` | Słowo kluczowe "references", używane do definiowania relacji i kluczy obcych między tabelami          |       `references`, `REFERENCES`       |
|  `INT_TYPE`  | Typ danych dla liczb całkowitych.                                                                     |      `int`, `INTEGER`, `integer`       |
|  `VARCHAR`   | Typ danych dla ciągów znaków o zmiennej lub określonej długości                                       |          `varchar`, `VARCHAR`          |
|  `NUMERIC`   | Typ danych dla liczb rzeczywistych                                                                    | `decimal`, `float`, `DECIMAL`, `FLOAT` |
|  `BOOLEAN`   | Typ danych dla wartości logicznych                                                                    |           `boolean`, `BOOL`            |                                                            
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
