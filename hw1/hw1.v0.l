%{
// version 0: prints the input program without Type Declarations in input
#include <stdio.h>
#include <stdlib.h>
#define YYSTYPE char*
#define TOKENDEBUG 0
#include "hw1.v0.tab.h"

#define COPY_YYTEXT if(TOKENDEBUG) printf("got token %s\n", yytext); yylval = strdup(yytext);

int syntaxError = 0;
%}

%%

 /*------COMMENTS---------*/
"/*"([^\*]|(\*[^\/]))*|\*"*/" { /* Ignore comment */ }
"//"[^\n]*\n { /* Ignore single line comment */ }

 /*-------BRACKETS-------*/
"(" { COPY_YYTEXT; return BL1; }
")" { COPY_YYTEXT; return BR1; }
"[" { COPY_YYTEXT; return BL2; }
"]" { COPY_YYTEXT; return BR2; }
"{" { COPY_YYTEXT; return BL3; }
"}" { COPY_YYTEXT; return BR3; }

 /*-------KEYWORDS-------*/
"if"                { COPY_YYTEXT; return KW_IF; }
"else"              { COPY_YYTEXT; return KW_EL; }
"do"                { COPY_YYTEXT; return KW_DO; }
"while"             { COPY_YYTEXT; return KW_WH; }
"for"               { COPY_YYTEXT; return KW_FR; }
"class"             { COPY_YYTEXT; return KW_CL; }
"extends"           { COPY_YYTEXT; return KW_EX; }
"public"            { COPY_YYTEXT; return KW_PB; }
"static"            { COPY_YYTEXT; return KW_ST; }
"void"              { COPY_YYTEXT; return KW_VD; }
"main"              { COPY_YYTEXT; return KW_MN; }
"String"            { COPY_YYTEXT; return KW_SR; }
"System.out.println" { COPY_YYTEXT; return KW_PR; }
"return"            { COPY_YYTEXT; return KW_RT; }
"true"              { COPY_YYTEXT; return KW_TR; }
"false"             { COPY_YYTEXT; return KW_FL; }
"new"               { COPY_YYTEXT; return KW_NW; }
"int"               { COPY_YYTEXT; return KW_IN; }
"#define"           { COPY_YYTEXT; return KW_HD; }
"this"              { COPY_YYTEXT; return KW_TH; }
"length"           { COPY_YYTEXT; return KW_LN; }


 /*------OPERATORS/SYMBOLS-------*/
"<=" { COPY_YYTEXT; return OP_LE; }
">=" { COPY_YYTEXT; return OP_GE; }
"==" { COPY_YYTEXT; return OP_EE; }
"!=" { COPY_YYTEXT; return OP_NE; }
"||" { COPY_YYTEXT; return OP_PP; }
"&&" { COPY_YYTEXT; return OP_AA; }
"&" { COPY_YYTEXT; return OP_AND; }
"|" { COPY_YYTEXT; return OP_PIP; }
"+" { COPY_YYTEXT; return OP_ADD; }
"-" { COPY_YYTEXT; return OP_SUB; }
"*" { COPY_YYTEXT; return OP_MUL; }
"/" { COPY_YYTEXT; return OP_DIV; }
"=" { COPY_YYTEXT; return OP_EQL; }
"." { COPY_YYTEXT; return OP_DOT; }
"!" { COPY_YYTEXT; return OP_NEG; }
";" { COPY_YYTEXT; return OP_SEM; }
"," { COPY_YYTEXT; return OP_COM; }

 /*-------CATCHALL-EXPR--------*/
[_a-zA-Z][_a-zA-Z0-9]*   { COPY_YYTEXT; return ID;  }
[0-9]+                   { COPY_YYTEXT; return INT; }

 /*------MISC-TOKENS-------*/
[ \t\r\n]+ { }
 /*  <<EOF>> { COPY_YYTEXT; return TK_EOF;} */

. { fprintf(stderr, "Unknown symbol %s\n", yytext); }


%%

