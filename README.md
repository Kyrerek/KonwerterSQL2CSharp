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
|`END`|Średnik, oznaczenie końca kwerendy|`;`|


## Gramatyka formatu
