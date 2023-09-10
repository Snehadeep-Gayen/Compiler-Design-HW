%{
// version 2: uses lists instead of char*


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
int syntaxError = 0;
int macroError = 0;
int overloadingError = 0;

#define token_type int
#define YYSTYPE struct token*
#define MAX_NUMBER_OF_MACROS 10000
#define MAX_NUMBER_OF_MACRO_ARGS 100
#define MAX_ARG_INDEX 5 
#define MAX_NUMBER_OF_METHODS 100

////////   DEBUG MACROS  /////////
// #define DEBUG 0
#define FORMAT_OUTPUT 1
#ifdef DEBUG
#define CONCATDEBUG 0
#define SHOW_MACRO 1
#define COPYLISTDEBUG 0
#define MACRODEBUG 1
#define FLUSH_OUTPUT 1
#define COPYLISTLISTDEBUG 1
#else
#define CONCATDEBUG 0
#define SHOW_MACRO 0
#define COPYLISTDEBUG 0
#define MACRODEBUG 0
#define FLUSH_OUTPUT 0
#define COPYLISTLISTDEBUG 0
#endif

struct token{
    char* str;
    token_type type;
    struct token* nextToken;
};

struct token* giveToken(char* str, token_type type);

int yylex(void);
int yyerror(const char*);

struct token* concat(struct token* tokenArr[]);

void printList(struct token* tokenArr);

struct macro{
    char* name;
    int noArguments;
    char** arguments;
    struct token* body;
    struct token** lists;
    int isStatement;
};

struct macro macroArr[MAX_NUMBER_OF_MACROS];
int nMacros = 0;

char** extractArguments(struct token* lst, int* noArgs);
void createMacro(char* name, struct token* args, struct token* body, int isStatement);
struct token* applyMacros(struct token* macroCall, int semicolon);
void removeToken(struct token*);

struct token* finalAns = NULL;

void errorHandler(void){
    if(syntaxError!=0 || macroError!=0 || overloadingError!=0){
        printf("// Failed to parse macrojava code.");
        exit(0);
    }
    else 
        printList(finalAns);
}

char* methodName[MAX_NUMBER_OF_METHODS];
int methodNumber = 0;

%}

// %define parse.error detailed
%token BL1 BR1 BL2 BR2 BL3 BR3
%token KW_IF KW_EL KW_DO KW_WH KW_FR KW_CL KW_EX KW_PB KW_ST KW_VD KW_MN KW_SR KW_PR KW_RT KW_TR KW_FL KW_NW KW_IN KW_HD KW_TH KW_LN KW_BO
%token CSTA CEND CLIN
%token OP_LE OP_GE OP_EE OP_NE OP_PP OP_AA OP_AND OP_PIP OP_ADD OP_SUB OP_MUL OP_DIV OP_EQL OP_DOT OP_NEG OP_SEM OP_COM
%token ID INT EOL
 /* tokens made for convenience */
%token EMPTY_TOKEN MACRO_ARG

%start Goal

%%



Goal: MacroDefBlock MainClass TypeDeclarationBlock { struct token* arr[4] = {$1,$2,$3,NULL};
                                                     $$ = concat(arr);
                                                     finalAns = $$;
                                                   }
    ;

MacroDefBlock: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
             | MacroDefBlock MacroDefStatement { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr);  }
             | MacroDefBlock MacroDefExpression { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
             ;

MainClass: KW_CL ID BL3
                KW_PB KW_ST KW_VD KW_MN  BL1 KW_SR BL2 BR2 ID BR1  BL3
                    KW_PR BL1 Expression BR1 OP_SEM 
                BR3
           BR3
           {
               struct token* arr[22] = {$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,
                                $11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,NULL};
               $$ = concat(arr);
           }
		;

TypeDeclarationBlock: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
                    | TypeDeclarationBlock KW_CL ID BL3
                        VarDeclarationBlock MethodDeclarationBlock
                      BR3 
                       {
							methodNumber = 0; // LEAK 
							struct token* arr[8] = {$1,$2,$3,$4,$5,$6,$7,NULL}; 
							$$ = concat(arr); 
					   }
                    | TypeDeclarationBlock KW_CL ID KW_EX ID BL3
                        VarDeclarationBlock MethodDeclarationBlock
                      BR3
                     {
						methodNumber = 0; // LEAK
                        struct token* arr[10] = {$1,$2,$3,$4,$5,$6,$7,$8,$9,NULL};
                        $$ = concat(arr);
                     } 
                    ;

VarDeclarationBlock: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
                   | VarDeclarationBlock Type ID OP_SEM  { struct token* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
                   ;

Type: KW_IN BL2 BR2  { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
    | KW_IN  { $$ = $1; }
    | KW_BO  { $$ = $1; }
    | ID  { $$ = $1; }
	;

MethodDeclarationBlock: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
                      | MethodDeclarationBlock KW_PB Type ID BL1 TypeArgumentList BR1 BL3
                            VarDeclarationBlock StatementBlock KW_RT Expression OP_SEM
                        BR3
                        {
						    methodName[methodNumber] = strdup($4->str);
							methodNumber++;
							int i=0;
							for(i=0; i<methodNumber-1; i++)
								if(strcmp(methodName[i], $4->str)==0){
									overloadingError = 1;
									errorHandler();
								}	
                            struct token* arr[15] = {$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,NULL}; 
                            $$ = concat(arr);
                        } 
                      ;

TypeArgumentList: /* empty */  { $$ = giveToken("", EMPTY_TOKEN); }
                | NonEmptyTypeArgumentList  { $$ = $1; }
                ;
NonEmptyTypeArgumentList: Type ID  { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
                        | NonEmptyTypeArgumentList OP_COM Type ID   
                            { struct token* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
                        ;

StatementBlock: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
              | Statement StatementBlock  { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
              ;

Statement: BL3 StatementBlock BR3  { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
         | KW_PR BL1 Expression BR1 OP_SEM { struct token* arr[6] = {$1,$2,$3,$4,$5,NULL}; $$ = concat(arr); }
         | ID OP_EQL Expression OP_SEM  { struct token* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
         | ID BL2 Expression BR2 OP_EQL Expression OP_SEM  
            { struct token* arr[8] = {$1,$2,$3,$4,$5,$6,$7,NULL}; $$ = concat(arr); }
         | KW_IF BL1 Expression BR1 Statement  { struct token* arr[6] = {$1,$2,$3,$4,$5,NULL}; $$ = concat(arr); }
         | KW_IF BL1 Expression BR1 Statement KW_EL Statement  
            { struct token* arr[8] = {$1,$2,$3,$4,$5,$6,$7,NULL}; $$ = concat(arr); }
         | KW_DO Statement KW_WH BL1 Expression BR1 OP_SEM  
            { struct token* arr[8] = {$1,$2,$3,$4,$5,$6,$7,NULL}; $$ = concat(arr); }
         | KW_WH BL1 Expression BR1 Statement  { struct token* arr[6] = {$1,$2,$3,$4,$5,NULL}; $$ = concat(arr); }
         | ID BL1 ArgumentList BR1 OP_SEM { 
             if(MACRODEBUG) printf("Macro %s called\n", $1->str);
             struct token* arr[6] = {$1,$2,$3,$4,$5,NULL}; 
             $$ = applyMacros(concat(arr), 1); 
             }
         ;

Expression:   PrimaryExpression OP_LE PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_NE PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_PP PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_AA PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_ADD PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_SUB PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_MUL PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DIV PrimaryExpression { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DOT KW_LN            { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
            | PrimaryExpression { $$ = $1; }
            | PrimaryExpression BL2 PrimaryExpression BR2 { struct token* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
            | PrimaryExpression OP_DOT ID BL1 ArgumentList BR1 { 
                    struct token* arr[7] = {$1,$2,$3,$4,$5,$6,NULL};
                    $$ = concat(arr);
                }
            | ID BL1 ArgumentList BR1 {
                if(MACRODEBUG) printf("Macro %s called\n", $1->str);
                struct token* arr[5] = {$1,$2,$3,$4,NULL}; 
                $$ = applyMacros(concat(arr), 0); }
            ;

ArgumentList: /* empty */ { $$ = giveToken("", EMPTY_TOKEN); }
            | NonEmptyArgumentList  { $$ = $1; }
            ;
NonEmptyArgumentList: Expression  { $$ = $1; }
                    | NonEmptyArgumentList OP_COM Expression 
                        { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
					;

PrimaryExpression: Integer { $$ = $1; }
                 | KW_TR { $$ = $1; }
                 | KW_FL { $$ = $1; }
                 | ID { $$ = $1; }
                 | KW_TH { $$ = $1; }
                 | KW_NW KW_IN BL2 Expression BR2 { struct token* arr[6] = {$1,$2,$3,$4,$5,NULL}; $$ = concat(arr); }
                 | KW_NW ID BL1 BR1 { struct token* arr[5] = {$1,$2,$3,$4,NULL}; $$ = concat(arr); }
                 | OP_NEG Expression { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr); }
                 | BL1 Expression BR1 { struct token* arr[4] = {$1,$2,$3,NULL}; $$ = concat(arr); }
                 ;

MacroDefExpression: KW_HD ID BL1 ArgumentList BR1 BL1 Expression BR1{ 
                           // struct token* arr[9] = {$1,$2,$3,$4,$5,$6,$7,$8,NULL}; 
                           // $$ = concat(arr);
                           struct token* macroBody[4] = {$6,$7,$8,NULL};
                           createMacro($2->str, $4, concat(macroBody), 0);
                           $$ = giveToken(strdup(""), EMPTY_TOKEN);
                        }
                  ;
MacroDefStatement: KW_HD ID BL1 ArgumentList BR1 BL3 StatementBlock BR3 { 
                           // struct token* arr[9] = {$1,$2,$3,$4,$5,$6,$7,$8,NULL};
                           // $$ = concat(arr);
                           struct token* macroBody[4] = {$6,$7,$8,NULL};
                           createMacro($2->str, $4, concat(macroBody), 1);
                           $$ = giveToken(strdup(""), EMPTY_TOKEN);
                        }
                  ;

Integer: INT { $$ = $1; }
	   | OP_SUB INT { struct token* arr[3] = {$1,$2,NULL}; $$ = concat(arr); } 

%%


int yywrap() { return 1; }

int main(){
	yyparse();
    if(SHOW_MACRO){
	    int i=0;
	    for(i=0; i<nMacros; i++){
	        fprintf(stdout, "Macro no. %d: %s\n", i, macroArr[i].name);
            int j=0;
            while((macroArr[i].arguments)[j]!=NULL){
                fprintf(stdout, "Arg no. %d: %s\n", j, (macroArr[i].arguments)[j]);
                j++;
            }
            printList(macroArr[i].body);
            fprintf(stdout, "\n");
	    }
    }
    errorHandler();
    return 0;
}

int yyerror(const char* errorMsg){
	// fprintf(stderr, "Found Error %s\n", errorMsg);
    syntaxError = 1;
    errorHandler();
	return 0;
}

struct token* giveToken(char* str, token_type type){
    struct token* t = (struct token*) malloc(sizeof(struct token));
    t->str = strdup(str);
    t->type = type;
    t->nextToken = NULL;
    return t;
}

void removeToken(struct token* t){
    free(t->str);
    free(t);
}

/////////// LIST FUNCTIONS ////////////////

// assume l1 and l2 are not NULL
struct token* __concat(struct token* l1, struct token* l2){
    assert(l1!=NULL);
    struct token* ptr1 = l1;
    while(ptr1->nextToken!=NULL) ptr1 = ptr1->nextToken;
    ptr1->nextToken = l2;
    return l1;
}

struct token* concat(struct token* tokenArr[]){
    struct token* head = giveToken("HEAD", 0);
    int i=0;
    if(CONCATDEBUG) fprintf(stdout,"Concatenating: ");
    while(tokenArr[i]!=NULL){ 
        head = __concat(head, tokenArr[i]);
        if(CONCATDEBUG) fprintf(stdout,"%s ", tokenArr[i]->str);
        i++;
    }
    if(CONCATDEBUG) fprintf(stdout, "\n");
    struct token* start = head->nextToken;
    free(head);
    return start;
}


struct token* copyList(struct token* lst){
    if(lst==NULL)   return NULL;
    struct token* hd = giveToken(strdup(lst->str), lst->type);
    struct token* tl = copyList(lst->nextToken);
    hd->nextToken = tl;
    if(COPYLISTDEBUG){
        fprintf(stdout, "CopyList of \n");
        printList(lst);
        fprintf(stdout, "\n is \n");
        printList(hd);
    }
    return hd;
}

struct token** copyListList(struct token** lstlst){
    int num = 0;
    if(COPYLISTLISTDEBUG) { printf("REACHED\n"); fflush(stdout); }
    if(lstlst==NULL) return NULL;
    while(lstlst[num]!=NULL){
        if(COPYLISTLISTDEBUG) {
            printf("num: %d\n", num); fflush(stdout);
        }
        num++;
    }
    struct token** newList = (struct token**) malloc(sizeof(struct token*)*(num+1));
    int i=0;
    if(COPYLISTLISTDEBUG) { printf("REACHED\n"); fflush(stdout); } 
    while(i<num){ newList[i] = copyList(lstlst[i]); i++;}
    if(COPYLISTLISTDEBUG) { printf("REACHED\n"); fflush(stdout); } 
    newList[num] = NULL;
    if(COPYLISTLISTDEBUG) { 
        int i;
        for(i=0; i<num; i++){
            printf("%dth list is: ", i);
            printList(newList[i]);
            printf("\n");
        }
    }
    return newList;
}

// returns position of the string in list 
// and -1 if not present
int isPresent(char** strList, char* str){
    int i=0;
    while(strList[i]!=NULL)
        if(strcmp(strList[i++], str)==0)
            return i-1;
    return -1;
}

/////////////////// MACRO FUNCTIONS //////////////////////

char** extractArguments(struct token* lst, int* noArgs){
    char** args = (char**) malloc(sizeof(char*)*MAX_NUMBER_OF_MACRO_ARGS);
    int noargs = 0;
    struct token* hd = lst;
    while(hd!=NULL){
        if(hd->type==ID){
            args[noargs++] = strdup(hd->str);
            if(MACRODEBUG) { printf("%s is an argument\n", args[noargs-1]);}
        }
        hd = hd->nextToken;
    }
    args[noargs] = NULL;
    if(MACRODEBUG) { printf("has %d arguments\n", noargs);}
    *noArgs = noargs;
    return args;
}

void createList(int macroNumber){
    struct token* listCopy = copyList(macroArr[macroNumber].body);
    if(macroArr[macroNumber].isStatement){
        struct token* hd = listCopy;
        while(hd->nextToken->nextToken!=NULL) hd = hd->nextToken;
        hd->nextToken = NULL; // LEAK
        listCopy = listCopy->nextToken; // LEAK
    }
    int num = 0;
    struct token* cur = listCopy;
    while(cur!=NULL){
        if(cur->type==ID){
            int loc = isPresent(macroArr[macroNumber].arguments, cur->str);
            if(loc!=-1) num++;
        }
        cur = cur->nextToken;
    }
    struct token** lists = (struct token**) malloc(sizeof(struct token*)*(2*num+3));
    cur = listCopy;
    struct token* prev = NULL;
    struct token* hd = NULL;
    int listNo = 0;
    while(cur!=NULL){
        if(cur->type==ID){
            int loc = isPresent(macroArr[macroNumber].arguments, cur->str);
            if(loc!=-1){
                // break the chain and add the left to lists and right to hd
                if(hd!=NULL){
                    prev->nextToken = NULL;
                    lists[listNo++] = hd;
                }
                prev = cur;
                cur = cur->nextToken;
                prev->nextToken = NULL;
                prev->type = MACRO_ARG;
                prev->str = (char*) malloc(sizeof(char)*MAX_ARG_INDEX);
                sprintf(prev->str, "%d", loc);
                if(MACRODEBUG) { printf("Sprintfing %d as %s\n", loc, prev->str); }
                lists[listNo++] = prev;
                hd = prev = NULL;
                continue;
            }
        }
        prev = cur;
        cur = cur->nextToken;
        if(MACRODEBUG){
            if(lists[1]!=NULL)
                printf("Second list is %s\n", lists[1]->str);
        }
        if(hd==NULL) hd = prev;
    }
    if(MACRODEBUG) {
        struct token* temp = (lists[1]);
        // struct token* temp = ((macroArr[macroNumber].lists)[1]);
        if(temp!=NULL)
        printf("Second List in struct token** lists is %s\n", 
                temp->str);
    }
    if(hd!=NULL){
        if(MACRODEBUG){
            printf("head is %s, tail is %s\n", hd->str, prev->str);
        }
        lists[listNo++] = hd;
        prev->nextToken = NULL;
    }
    if(MACRODEBUG) { printf("List Body has %d lists\n", listNo);}
    lists[listNo] = NULL;
    assert(macroArr[macroNumber].noArguments!=0 || listNo==1);
    macroArr[macroNumber].lists = lists;
    if(MACRODEBUG) {
        struct token* temp = (lists[1]);
        // struct token* temp = ((macroArr[macroNumber].lists)[1]);
        if(temp!=NULL)
        printf("Second List in struct token** lists is %s\n", 
                temp->str);
    }
}

void createMacro(char* name, struct token* args, struct token* body, int isStatement){
    // ensure that the macro is not present in list already
    int i=0;
    for(i=0; i<nMacros; i++)
        if(strcmp(macroArr[i].name, name)==0){
            macroError = 1;
            errorHandler();
            return;
        }

    macroArr[nMacros].name = name;
    macroArr[nMacros].arguments = 
            extractArguments(args, &macroArr[nMacros].noArguments);
    macroArr[nMacros].body = copyList(body);
    macroArr[nMacros].isStatement = isStatement;
    createList(nMacros);
    nMacros++;
}

// macroCall is of the form "ID BL1 ArgumentList BR1 (OP_SEM)"
struct token* applyMacros(struct token* macroCall, int semicolon){
   int noCallArgs = 0;
   int numTokens = 0;
   char* macroName = macroCall->str;
   struct token* cur = macroCall;
   while(cur!=NULL){
       if(cur->type==OP_COM)
           noCallArgs++;
       if(MACRODEBUG) printf("%s is a token of type %d\n", cur->str, cur->type);
       if(cur->type!=EMPTY_TOKEN)
        numTokens++;
       cur = cur->nextToken;
    }
    noCallArgs++;
    if(numTokens==3+semicolon)
        noCallArgs=0;

    if(MACRODEBUG) { printf("Reached\n"); fflush(stdout); }
    if(MACRODEBUG) { printf("Macro %s called on %d arguments\n", macroName, noCallArgs); fflush(stdout); }

    // now find the macro in the array
    int i=0;
    for(i=0; i<nMacros; i++)
        if(strcmp(macroArr[i].name, macroName)==0)
            if(macroArr[i].noArguments==noCallArgs)
               if(macroArr[i].isStatement==semicolon)
                    break;
    if(i==nMacros){
        macroError = 1;
        errorHandler();
    }
    int macroNumber = i;

    if(MACRODEBUG) { printf("Reached\n"); fflush(stdout); }


    // map the arguments to callArguments
    struct token** callArgs = (struct token**) malloc(sizeof(struct token*)*noCallArgs);
    struct token* dupMacro = copyList(macroCall);
    cur = dupMacro->nextToken->nextToken;
    i=0;
    struct token* hd = NULL;
    struct token* prev = NULL;
    while((semicolon==0 && cur->nextToken!=NULL) ||
          (semicolon==1 && cur->nextToken->nextToken!=NULL)){
       if(cur->type==EMPTY_TOKEN){
           cur = cur->nextToken;
           continue;
       }
       if(cur->type==OP_COM){
            callArgs[i++] = hd;
            prev->nextToken = NULL;
            hd = prev = NULL;
            cur = cur->nextToken;
       }
       prev = cur;
       cur = cur->nextToken;
       if(hd==NULL) hd = prev;
    }
    if(hd!=NULL){
        callArgs[i++] = hd;
        prev->nextToken = NULL;
    }
    callArgs[i]=NULL;
    if(MACRODEBUG){
        printf("Call arguments (%d in number) are\n", i);
        int j=0;
        for(j=0; j<noCallArgs; j++){
            printList(callArgs[j]);
            printf("\n**************\n");
        }
        printf("**************\n");
        fflush(stdout);
    }
    if(i!=noCallArgs){
        macroError = 1;
        errorHandler();
    }

    if(MACRODEBUG) { printf("Reached\n"); fflush(stdout); }

    // replace the IDs with struct token* lists
    int listNo = 0;
    struct token** editedBody = copyListList(macroArr[macroNumber].lists);
    while(editedBody[listNo]!=NULL){
        struct token* t = editedBody[listNo];
        if(t->type==MACRO_ARG){
            if(MACRODEBUG) { printf("MacroArg's Loc string: '%s\n", t->str); }
            int loc = atoi(t->str);
            if(MACRODEBUG) { printf("loc: %d, noCallArgs: %d, macroName: %s\n", loc, noCallArgs, macroName); fflush(stdout);}
            assert(loc>=0 && loc<noCallArgs);
            editedBody[listNo] = copyList(callArgs[loc]); //MEMORY LEAKS HERE
        }
        listNo++;
    }

    if(MACRODEBUG) { printf("Reached\n"); fflush(stdout); }

   // return macroCall;
   return concat(editedBody);
}

/////////// LIST FUNCTIONS ////////////////

void printList(struct token* tokenArr){
    struct token* head = tokenArr;
    int scope = 0;
    while(head!=NULL){
        if(FORMAT_OUTPUT){
            if(strcmp(head->str, "}")==0) scope--;
            if(strcmp(head->str, "}")==0 ||
               strcmp(head->str, "#define")==0){
                printf("\n");
                int i=0;
                for(i=0; i<scope; i++) 
                    printf("\t");
            }
        }
        printf("%s", head->str);
        if(FORMAT_OUTPUT){
            if(strcmp(head->str, "(")==0 ||
               strcmp(head->str, ")")==0 ||
               strcmp(head->str, ".")==0 ||
               strcmp(head->str, "")==0)
               ; // do nothing
            else 
                printf(" ");
        }
        else
            printf(" ");
        if(FORMAT_OUTPUT){
            if(strcmp(head->str,"{")==0) scope++;
            int i;
            if(strcmp(head->str, "}")==0 ||
               strcmp(head->str, "{")==0 ||
               strcmp(head->str, ";")==0){
                printf("\n");
                for(i=0; i<scope; i++) 
                    printf("\t");
            }
        }
        head = head->nextToken;
        if(FLUSH_OUTPUT) fflush(stdout);
    }
}
