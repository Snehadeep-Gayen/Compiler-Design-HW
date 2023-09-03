%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define YYSTYPE char*
#define CONCATDEBUG 0

int yylex(void);
int yyerror(const char*);

// version 0: prints the input program without Type Declarations in input

char* concat(char* strArr[]){
    int n = 0;
    int len = 0;
    while(strArr[n]!=NULL){
        if(CONCATDEBUG) printf("%s", strArr[n]);
        len += strlen(strArr[n++]);
    }
    char *final = (char*) malloc(sizeof(char)*(len+n));
    final[0]=0;
    // copy all the strings and add spaces in between
    int i=0;
    if(CONCATDEBUG) printf("Concating: ");
    for(i=0; i<n; i++){
        if(CONCATDEBUG) printf("%s ", strArr[i]);
        strcat(final, strArr[i]);
        if(i!=n-1) strcat(final, " ");
    }
    if(CONCATDEBUG) printf("\n");
    // free the strings
    // for(i=0; i<n; i++) free(strArr[i]);
    return final;
}
%}

%define parse.error detailed
%token BL1 BR1 BL2 BR2 BL3 BR3
%token KW_IF KW_EL KW_DO KW_WH KW_FR KW_CL KW_EX KW_PB KW_ST KW_VD KW_MN KW_SR KW_PR KW_RT KW_TR KW_FL KW_NW KW_IN KW_HD KW_TH KW_LN
%token CSTA CEND CLIN
%token OP_LE OP_GE OP_EE OP_NE OP_PP OP_AA OP_AND OP_PIP OP_ADD OP_SUB OP_MUL OP_DIV OP_EQL OP_DOT OP_NEG OP_SEM OP_COM
%token ID INT EOL

%start Goal

%%



Goal: MacroDefBlock MainClass TypeDeclarationBlock { printf("%s%s\n%s", $1,$2,$3); }
    ;

MacroDefBlock: /* empty */ { $$ = strdup(""); }
             | MacroDefExpression MacroDefBlock { char* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
             ;

MainClass: KW_CL ID BL3
                KW_PB KW_ST KW_VD KW_MN  BL1 KW_SR BL2 BR2 ID BR1  BL3
                    KW_PR BL1 Expression BR1 OP_SEM 
                BR3
           BR3
           {
               char* arr[22] = {$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,
                                $11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,NULL};
               $$ = concat(arr);
           }

TypeDeclarationBlock: /* empty */ { $$ = strdup(""); }
                    ;

Expression:   PrimaryExpression OP_LE PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_NE PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_PP PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_AA PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_ADD PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_SUB PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_MUL PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DIV PrimaryExpression { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DOT KW_LN            { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression { $$ = $1; }
            | PrimaryExpression BL2 PrimaryExpression BR2 { char* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DOT ID BL1 ArgumentList BR1 { 
                    char* arr[7] = {$1,$2,$3,$4,$5,$6,NULL};
                    $$ = concat(arr);
                }
            | ID BL1 ArgumentList BR1 { char* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
            ;

ArgumentList: /* empty */ { $$ = strdup(""); }
            | Expression OP_COM ArgumentList { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | Expression { $$ = $1; }
            ;

PrimaryExpression: INT { $$ = $1; }
                 | KW_TR { $$ = $1; }
                 | KW_FL { $$ = $1; }
                 | ID { $$ = $1; }
                 | KW_TH { $$ = $1; }
                 | KW_NW KW_IN BL2 Expression BR2 { char* arr[6] = {$1,$2,$3,$4,$5,NULL}; $$ = concat(arr); }
                 | KW_NW ID BL1 BR1 { char* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
                 | OP_NEG Expression { char* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
                 | BL1 Expression BR1 { char* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
                 ;

MacroDefExpression: KW_HD ID BL1 ArgumentList BR1 BL1 Expression BR1{ 
                           char* arr[10] = {$1,$2,$3,$4,$5,$6,$7,$8,"\n",NULL}; $$ = concat(arr);
                           // $$ = strdup("Macro");
                        }
                  ;

%%


int yywrap() { return 1; }

int main(){
	yyparse();
}

int yyerror(const char* errorMsg){
	fprintf(stderr, "Found Error %s\n", errorMsg);
	return 0;
}

