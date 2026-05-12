grammar SQL;

@header {
package antlr;
}

query : operation END (operation END)*;

operation
    : select_stm | update_stm | delete_stm | insert_stm
    ;

select_stm
    : SELECT (DISTINCT)? select_list
    from_stm
    (join_stm)*
    (where_stm)?
    (groupby_stm)?
    (having_stm)?
    (order_stm)?
    ;

select_list
    : MULT |
    select_item (COMMA select_item)*
    ;

select_item
    : (column | agg_func) (AS ID)?
    ;

column
    : (ID PER)? ID
    ;

agg_func
    : (MIN | MAX | COUNT | SUM | AVG) LBRACKET (MULT | column) RBRACKET
    ;

where_stm
    : WHERE logic_form
    ;

from_stm
    : FROM ID (AS ID)?
    ;

join_stm
    : join_bef? JOIN ID (AS ID)? ON join_on (AND join_on)*
    ;

join_bef
    : (LEFT | RIGHT | FULL) (OUTER)?
    | INNER
    ;

join_on
    : column EQL column
    ;

groupby_stm
    : GROUP BY column (COMMA column)*
    ;

logic_form
    : logic_or
    ;

logic_or
    : logic_and (OR logic_and)*
    ;

logic_and
    : logic_not (AND logic_not)*
    ;

logic_not
    : NOT logic_not
    | logic_atom
    ;

logic_atom
    : logic_cmp
    | LBRACKET logic_form RBRACKET
    ;

logic_cmp
    : logic_simple_cmp | logic_between_cmp | logic_like_cmp | logic_null_cmp
    ;

logic_simple_cmp
    : item_form (EGREATER | ELESS | NEQL | GREATER | LESS | EQL) item_form
    ;

logic_between_cmp
    : item_form NOT? BETWEEN item_form AND item_form
    ;

logic_like_cmp
    : column NOT? LIKE STR
    ;

logic_null_cmp
    : column IS NOT? NULL
    ;

item_form
    : item_plus_minus
    ;

item_plus_minus
    :   item_multi_div ((PLUS | MINUS) item_multi_div)*
    ;

item_multi_div
    :   item_atom ((MULT | DIV) item_atom)*
    ;

item_atom
    : MINUS? item | LBRACKET item_form RBRACKET
    ;

item
    : (column | NUM | INT | STR | TRUE | FALSE | agg_func)
    ;

order_stm
    : ORDER BY order_list
    ;

order_list
    : order_item (COMMA order_item)*
    ;

order_item
    : column (ASC | DESC)?
    ;

having_stm
    : HAVING logic_form
    ;

update_stm
    : UPDATE ID set_stm (where_stm)?
    ;

set_stm
    : SET set_list
    ;

set_list
    : set_item (COMMA set_item)*
    ;

set_item
    : ID EQL (item_form | NULL)
    ;

delete_stm
    : DELETE from_stm (where_stm)?
    ;

insert_stm
    : INSERT into_stm values_stm
    ;

into_stm
    : INTO ID (LBRACKET into_bracket_list RBRACKET)?
    ;

into_bracket_list
    : ID (COMMA ID)*
    ;

values_stm
    : VALUES values_list
    ;

values_list
    : values_item (COMMA values_item)*
    ;

values_item
    : LBRACKET values_item_list RBRACKET
    ;

values_item_list
    : value (COMMA value)*
    ;

value
    : (MINUS)? (INT | NUM)
    | STR
    | TRUE
    | FALSE
    | NULL
    ;


NUM: [0-9]+ '.' [0-9]+;
INT: [0-9]+;
STR: '\'' (~['\r\n])* '\'' ;
TRUE: [tT][rR][uU][eE];
FALSE:[fF][aA][lL][sS][eE];
NULL: [nN][uU][lL][lL];
SELECT: [sS][eE][lL][eE][cC][tT];
DISTINCT: [dD][iI][sS][tT][iI][nN][cC][tT];
FROM: [fF][rR][oO][mM];
AS: [aA][sS];
JOIN: [jJ][oO][iI][nN];
INNER: [iI][nN][nN][eE][rR];
OUTER: [oO][uU][tT][eE][rR];
LEFT: [lL][eE][fF][tT];
RIGHT: [rR][iI][gG][hH][tT];
FULL: [fF][uU][lL][lL];
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
UPDATE: [uU][pP][dD][aA][tT][eE];
SET: [sS][eE][tT];
DELETE: [dD][eE][lL][eE][tT][eE];
INSERT: [iI][nN][sS][eE][rR][tT];
INTO: [iI][nN][tT][oO];
VALUES: [vV][aA][lL][uU][eE][sS];

PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
EGREATER: '>=';
ELESS: '<=';
NEQL: '<>';
GREATER: '>';
LESS: '<';
EQL: '=';
LBRACKET: '(';
RBRACKET: ')';
COMMA: ',';
PER: '.';
END: ';';

WS : [ \t\r\n]+ -> skip ;
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;
ERR: . ;