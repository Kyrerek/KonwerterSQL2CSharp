grammar Hello;

@header {
package antlr;
}

from_stm
    : FROM ID
    ;

order_stm
    : ORDER BY order_list
    ;

order_list
    : order_item (',' order_item)*
    ;

order_item
    : ID (ASC | DESC)?
    ;

NUM: [0-9]+ '.' [0-9]+;
INT: [0-9];
STR: '\'' (~['\r\n])* '\'' ;
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;
TRUE: 'true' | 'True';
FALSE: 'false' | 'False';
NULL: 'null' | 'NULL';
SELECT: 'select' | 'SELECT';
FROM: 'from' | 'FROM';
AS: 'as' | 'AS';
JOIN: 'join' | 'JOIN';
INNER: 'inner' | 'INNER';
OUTER: 'outer' | 'OUTER';
LEFT: 'left' | 'LEFT';
RIGHT: 'right' | 'RIGHT';
ON: 'on' | 'ON';
WHERE: 'where' | 'WHERE';
AND: 'and' | 'AND';
OR: 'or' | 'OR';
NOT: 'not' | 'NOT';
LIKE: 'like' | 'LIKE';
IS: 'is' | 'IS';
BETWEEN: 'between' | 'BETWEEN';
ORDER: 'order' | 'ORDER';
BY: 'by' | 'BY';
ASC: 'asc' | 'ASC';
DESC: 'desc' | 'DESC';
GROUP: 'group' | 'GROUP';
MIN: 'min' | 'MIN';
MAX: 'max' | 'MAX';
COUNT: 'count' | 'COUNT';
SUM: 'sum' | 'SUM';
AVG: 'avg' | 'AVG';
HAVING: 'having' | 'HAVING';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '//';
GREATER: '>';
LESS: '<';
EGREATER: '>=';
ELESS: '=<';
EQL: '=';
NEQL: '<>';
LBRACKET: '(';
RBRACKET: ')';
COMMA: ',';
PER: '.';
END: ';';
WS : [ \t\r\n]+ -> skip ;
ERR: . ;