# KonwerterSQL2CSharp
## Zespół
1. Eryk Łęski - erykleski@student.agh.edu.pl
2. Michał Machalski - mmasalski@student.agh.edu.pl
## Dane kontaktowe
- erykleski@student.agh.edu.pl
## Założenia programu
### Ogólne cele programu
Program ma za zadanie przekonwertowanie zapytań SQL do kodu w języku C#.
### Rodzaj translatora
- Kompilator
### Planowany wynik działania programu
- kompilator zapytań SQL do języka C#
### Planowany język implementacji
- Java
### Generator parsera
- ANTLR4
## Opis tokenów
| Kod      | Opis                                |Przykłady|
|:----------:|-------------------------------------|:---------:|
|`NUM`|Liczba rzeczywista|3.14, 0.99, 42.0|
|`INT`|Liczba całkowita|10000, 3453, 1|
|`STR`|Ciąg znaków jako wartość|'Ciąg znaków', 'Kuba', 'owocE'|
|`ID`|Ciąg znaków jako nazwa kolumny lub tabeli|persons, name_of_fruits, weight|
|`SELECT`|Słowo kluczowe "select"|-|
|`FROM`|Słowo kluczowe "from"|-|
|`AS`|Słowo kluczowe "as", aliasowanie nazw|-|
|`WHERE`|Słowo kluczowe "where"|-|
|`AND`|Słowo kluczowe "and", koniunkcja, używane w formułach logicznych|-|
|`OR`|Słowo kluczowe "or", alternatywa, używane w formułach logicznych|-|
|`NOT`|Słowo kluczowe "not", zaprzeczenie, używane w formułach logicznych|-|
|`LIKE`|Słowo kluczowe "like", używane w formułach logicznych do znajdowania wzorców|-|
|`ORDER`|Słowo kluczowe "order by"|-|
|`ASC`|Słowo kluczowe "asc", sortowanie rosnące, używane po "order by"|-|
|`DESC`|Słowo kluczowe "desc", sortowanie malejące, używane po "order by"|-|
|`GROUP`|Słowo kluczowe "group by"|-|
|`MIN`|Słowo kluczowe "min()", najmniejsza wartość|-|
|`MAX`|Słowo kluczowe "max()", maksymalna wartość|-|
|`COUNT`|Słowo kluczowe "count()", ilość rekordów|-|
|`SUM`|Słowo kluczowe "sum()", suma wartości|-|
|`AVG`|Słowo kluczowe "avg()", średnia z wartości|-|
|`PLUS`|Znak dodawania |`+`|
|`MINUS`|Znak odejmowania|`-`|
|`MULT`|Znak mnożenia|`*`|
|`DIV`|Znak dzielenia|`/`|
|`GREATER`|Znak większości|`>`|
|`LESS`|Znak mniejszości|`<`|
|`EGREATER`|Znak większości bądź równości|`>=`|
|`ELESS`|Znak mniejszości bądź równości|`<=`|
|`EQL`|Znak równości|`=`|
|`NEQL`|Znak nierówności|`<>`|
|`LBRACKET`|Nawias otwierający|`(`|
|`RBRACKET`|Nawias zamykający|`)`|
|`COMMA`|Przecinek, używany do odzielenia np. nazw kolumn lub tabel|`,`|
|`PER`|Kropka, ...|`.`|
|`END`|Średnik, oznaczenie końca kwerendy|`;`|
|`SELECT_STM`|Całe wyrażenie SELECT|`select a, b, c from tabela where a > b;`|
|`SELECT_CONT`|Zawartość listy wybranych do wyświetlenia kolumn/wartości|`a, b, min(c)`|
|`SELECT_ITEM`|Element z listy wybranych do wyświetlenia kolumn/wartości|`a`, `tabela.b`, `min(c)`|
|`WHERE_STM`|Wyrażenie WHERE wraz z formułą logiczną|`where a > b`, `where b = min(a) and c <= 7`|
|`WHERE_CONT`|Formuły logiczne rozdzielone AND lub OR|`a > b`, `b = min(a) and c <= 7`|
|`LOGIC_FORM`|Formuła logiczna|`a > b`, `b = min(a)`, `c <= 7`|
|`ORDER_STM`|Wyrażenie ORDER BY wraz z listą kolumn i wyborem sortowania|`order by a, b`, `order by a desc`|
|`ORDER_LIST`|Jeden lub więcej kolumn według, których odbędzie się sortowanie|`a, b`, `a`|

## Gramatyka formatu
