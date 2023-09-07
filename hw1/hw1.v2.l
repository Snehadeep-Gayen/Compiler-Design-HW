%{
// version 0: prints the input program without Type Declarations in input
#include <stdio.h>
#include <stdlib.h>
#define YYSTYPE struct token*
#include "hw1.v2.tab.h"
#define TOKENDEBUG 0
#define token_type int

struct token* giveToken(char*, token_type);

#define COPY_YYTEXT(TYPE) \
    if(TOKENDEBUG) fprintf(stderr, "got token %s\n", yytext); \
    yylval = giveToken(yytext, TYPE); \
    return TYPE;

int syntaxError = 0;


%}

%%

 /*------COMMENTS---------*/
"/*"([^\*]|(\*[^\/]+))*"*/"|"/***/" { /* Ignore comment */ }
"//"[^\n]*\n { /* Ignore single line comment */ }

 
 /*-------BRACKETS-------*/
"(" { COPY_YYTEXT(BL1); }
")" { COPY_YYTEXT(BR1); }
"[" { COPY_YYTEXT(BL2); }
"]" { COPY_YYTEXT(BR2); }
"{" { COPY_YYTEXT(BL3); }
"}" { COPY_YYTEXT(BR3); }

 /*-------KEYWORDS-------*/
"boolean"           { COPY_YYTEXT(KW_BO); }
"if"                { COPY_YYTEXT(KW_IF); }
"else"              { COPY_YYTEXT(KW_EL); }
"do"                { COPY_YYTEXT(KW_DO); }
"while"             { COPY_YYTEXT(KW_WH); }
"for"               { COPY_YYTEXT(KW_FR); }
"class"             { COPY_YYTEXT(KW_CL); }
"extends"           { COPY_YYTEXT(KW_EX); }
"public"            { COPY_YYTEXT(KW_PB); }
"static"            { COPY_YYTEXT(KW_ST); }
"void"              { COPY_YYTEXT(KW_VD); }
"main"              { COPY_YYTEXT(KW_MN); }
"String"            { COPY_YYTEXT(KW_SR); }
"System.out.println" { COPY_YYTEXT(KW_PR); }
"return"            { COPY_YYTEXT(KW_RT); }
"true"              { COPY_YYTEXT(KW_TR); }
"false"             { COPY_YYTEXT(KW_FL); }
"new"               { COPY_YYTEXT(KW_NW); }
"int"               { COPY_YYTEXT(KW_IN); }
"#define"           { COPY_YYTEXT(KW_HD); }
"this"              { COPY_YYTEXT(KW_TH); }
"length"           { COPY_YYTEXT(KW_LN); }

 /*------OPERATORS/SYMBOLS-------*/
"<=" { COPY_YYTEXT(OP_LE); }
">=" { COPY_YYTEXT(OP_GE); }
"==" { COPY_YYTEXT(OP_EE); }
"!=" { COPY_YYTEXT(OP_NE); }
"||" { COPY_YYTEXT(OP_PP); }
"&&" { COPY_YYTEXT(OP_AA); }
"&" { COPY_YYTEXT(OP_AND); }
"|" { COPY_YYTEXT(OP_PIP); }
"+" { COPY_YYTEXT(OP_ADD); }
"-" { COPY_YYTEXT(OP_SUB); }
"*" { COPY_YYTEXT(OP_MUL); }
"/" { COPY_YYTEXT(OP_DIV); }
"=" { COPY_YYTEXT(OP_EQL); }
"." { COPY_YYTEXT(OP_DOT); }
"!" { COPY_YYTEXT(OP_NEG); }
";" { COPY_YYTEXT(OP_SEM); }
"," { COPY_YYTEXT(OP_COM); }

 /*-------CATCHALL-EXPR--------*/
[_a-zA-Z][_a-zA-Z0-9]*   { COPY_YYTEXT(ID);  }
[0-9]+                   { COPY_YYTEXT(INT); }

 /*------MISC-TOKENS-------*/
[ \t\r\n]+ { }
 /*  <<EOF>> { COPY_YYTEXT(TK_EOF); } */


%%

