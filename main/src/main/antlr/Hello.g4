grammar Hello;

@header {
package antlr;
}

query : select_stm END;

select_stm
    : SELECT select_list
    from_stm
    (join_stm)*
    (where_stm)?
    (groupby_stm)?
    (order_stm)?
    ;

select_list
    : MULT |
    select_item (COMMA select_item)
    ;

select_item
    : ((ID PER)? ID | agg_func) (AS ID)?
    ;

agg_func
    : (MIN | MAX | COUNT | SUM | AVG) LBRACKET (MULT | (ID PER)? ID) RBRACKET
    ;


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
INT: [0-9]+;
STR: '\'' (~['\r\n])* '\'' ;
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;
TRUE: [tT][rR][uU][eE];
FALSE:[fF][aA][lL][sS][eE];
NULL: [nN][uU][lL][lL];
SELECT: [sS][eE][lL][eE][cC][tT];
FROM: [fF][rR][oO][mM];
AS: [aA][sS];
JOIN: [jJ][oO][iI][nN];
INNER: [iI][nN][nN][eE][rR];
OUTER: [oO][uU][tT][eE][rR];
LEFT: [lL][eE][fF][tT];
RIGHT: [rR][iI][gG][hH][tT];
ON: [oO][nN];
WHERE: [wW][hH][eE][rR][eE];
AND: [aA][nN][dD];
OR: [oO][rR];
NOT: [nN][oO][tT];
LIKE: [lL][iI][kK][eE];
IS: [iI][sS];
BETWEEN: [bB][eE][tT][wW][eE][eE][nN];
ORDER: [oO][rR][dD][eE][rR];
BY: [bB][yY];
ASC: [aA][sS][cC];
DESC: [dD][eE][sS][cC];
GROUP: [gG][rR][oO][uU][pP];
MIN: [mM][iI][nN];
MAX: [mM][aA][xX];
COUNT: [cC][oO][uU][nN][tT];
SUM: [sS][uU][mM];
AVG: [aA][vV][gG];
HAVING: [hH][aA][vV][iI][nN][gG];
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '//';
GREATER: '>';
LESS: '<';
EGREATER: '>=';
ELESS: '<=';
EQL: '=';
NEQL: '<>';
LBRACKET: '(';
RBRACKET: ')';
COMMA: ',';
PER: '.';
END: ';';
WS : [ \t\r\n]+ -> skip ;
ERR: . ;