v0: 
	bison -d hw1.v0.y
	flex hw1.v0.l
	gcc -g3 -lfl -o v0.out hw1.v0.tab.c lex.yy.c

v1: 
	bison -d hw1.v1.y
	flex hw1.v1.l
	gcc -g3 -lfl -o v1.out hw1.v1.tab.c lex.yy.c

clean: 
	rm *tab* *.c *.out *output*