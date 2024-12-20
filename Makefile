.PHONY: run clean all

run: P1/P1 P2/P2.class P3/P3.class P4/P4.class P5/P5.class
	./P1/P1 < input.java > P2/input2.minijava
	cd P2 && \
	java P2 < input2.minijava > output2.typecheck && \
	grep -q "successful" output2.typecheck || (echo "Type Check Failed"; exit 1)
	cd ./P3 && \
	java P3 < ../P2/input2.minijava > ../P4/input4.microIR
	cd ./P4 && \
	java P4 < input4.microIR > ../P5/input5.miniRA
	cd ./P5 && \
	java P5 < input5.miniRA > ../output.mips

all: P1 P2 P3 P4 P5

clean:
	rm -f P1/P1 P1/*.tab.c P1/*.tab.h P1/lex.yy.c
	find . -type f -name "*.class" -delete
	rm -f P2/input2.minijava P2/output2.typecheck
	rm -f P4/input4.microIR
	rm -f P5/input5.miniRA
	rm -f output.mips

# Targets for individual components
P1/P1: P1/P1.y P1/P1.l
	cd P1 && \
	bison -d P1.y && \
	flex P1.l && \
	gcc $(CFLAGS) P1.tab.c lex.yy.c -o P1

P2/P2.class: P2/P2.java
	cd P2 && \
	javac $(JFLAGS) P2.java

P3/P3.class: P3/P3.java
	cd P3 && \
	javac $(JFLAGS) P3.java

P4/P4.class: P4/P4.java
	cd P4 && \
	javac $(JFLAGS) P4.java

P5/P5.class: P5/P5.java
	cd P5 && \
	javac $(JFLAGS) P5.java
