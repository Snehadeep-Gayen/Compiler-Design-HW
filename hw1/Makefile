v0: 
	bison -d hw1.v0.y
	flex hw1.v0.l
	gcc -g3 -lfl -o v0.out hw1.v0.tab.c lex.yy.c

v1: 
	bison -d hw1.v1.y
	flex hw1.v1.l
	gcc -g3 -lfl -o v1.out hw1.v1.tab.c lex.yy.c

v2:
	bison -d hw1.v2.y
	flex hw1.v2.l
	gcc -g3 -Wall -Wextra -lfl -o v2.out hw1.v2.tab.c lex.yy.c

.PHONY: tc
tc: 
	./v2.out < ./tc/BinarySearch.java > ./tc/BinarySearchOutput.java
	javac ./tc/BinarySearchOutput.java
	./v2.out < ./tc/BinaryTree.java > ./tc/BinaryTreeOutput.java
	javac ./tc/BinaryTreeOutput.java
	./v2.out < ./tc/Factorial.java > ./tc/FactorialOutput.java
	javac ./tc/FactorialOutput.java
	./v2.out < ./tc/LinearSearch.java > ./tc/LinearSearchOutput.java
	javac ./tc/LinearSearchOutput.java
	./v2.out < ./tc/LinkedList.java > ./tc/LinkedListOutput.java
	javac ./tc/LinkedListOutput.java
	./v2.out < ./tc/QuickSort.java > ./tc/QuickSortOutput.java
	javac ./tc/QuickSortOutput.java
	./v2.out < ./tc/TreeVisitor.java > ./tc/TreeVisitorOutput.java
	javac ./tc/TreeVisitorOutput.java

clean: 
	rm *tab* *.c *.out ./tc/*Output* ./tc/*.class *.output