package visitor;

import java.util.*;

public class Generator {
    boolean debug;

    public Generator(boolean debug) {
        this.debug = debug;
    }

    /**
     * sw $regSrc, stack_location($regPtr)
     */
    public void store(String regSrc, int stack_location, String regPtr) {
        System.out.printf("sw $%s, %d($%s)\n", regSrc, stack_location, regPtr);
    }

    /**
     * move $regSrc, $regDest
     */
    public void move(String regSrc, String regDest) {
        System.out.printf("move $%s, $%s\n", regSrc, regDest);
    }

    /**
     * li $regSrc, value
     */
    public void loadi(String regSrc, int value) {
        System.out.printf("li $%s, %d\n", regSrc, value);
    }

    /**
     * lw $regDest, stack_location($regPtr)
     */
    public void load(String regDest, int stack_location, String regPtr) {
        System.out.printf("lw $%s, %d($%s)\n", regDest, stack_location, regPtr);
    }

    /**
     * custom
     */
    public void custom(String code) {
        System.out.println(code);
    }

    /**
     * comment
     */
    public void comment(String comment) {
        if (debug)
            System.out.printf("# %s\n", comment);
    }

    public void label(String label) {
        System.out.printf("%s:", label);
    }

    /**
     * .text
     * .globl function_name
     * function_name:
     */
    public void functionHeader(String function_name) {
        System.out.println("\n.text");
        System.out.printf(".globl %s\n", function_name);
        System.out.printf("%s:\n", function_name);
    }

    /**
     * generate code for system call
     */
    public void systemCalls() {

        // for halloc
        functionHeader("__halloc");
        loadi("v0", 9);
        custom("syscall");
        custom("j $ra");

        // for print
        functionHeader("__print");
        loadi("v0", 1);
        custom("syscall");
        custom("la $a0, newl");
        loadi("v0", 4);
        custom("syscall");
        custom("j $ra");
    }

    /**
     * data segment
     */
    public void dataSegment() {

        // for newl
        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("newl: .asciiz \"\\n\"");

        // for str_er
        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("str_err: .asciiz \" ERROR: abnormal termination\\n\"");
    }

    /**
     * error
     */
    public void error() {

        System.out.println("la $a0, str_err");
        loadi("v0", 4);
        custom("syscall");
        loadi("a0", 1);
        loadi("v0", 10);
        custom("syscall");
    }

    /**
     * store v1 into (-12)fp
     * store (-8)fp into v1
     */
    public void exchangeV1() {
        store("v1", -16, "fp");
        load("v1", -12, "fp");
    }

    /**
     * store v1 into (-8)fp
     * store (-12)fp into v1
     */
    public void restoreV1() {
        store("v1", -12, "fp");
        load("v1", -16, "fp");
    }

    /**
     * store arguments into (-20)fp, (-24)fp, (-28)fp, (-32)fp
     */
    public void storeArguments() {
        store("a0", -20, "fp");
        store("a1", -24, "fp");
        store("a2", -28, "fp");
        store("a3", -32, "fp");
    }

    /**
     * restore arguments from (-20)fp, (-24)fp, (-28)fp, (-32)fp
     */
    public void restoreArguments() {
        load("a0", -20, "fp");
        load("a1", -24, "fp");
        load("a2", -28, "fp");
        load("a3", -32, "fp");
    }

    /**
     * store v0 into (-36)fp
     */
    public void storeV0() {
        store("v0", -36, "fp");
    }

    /**
     * restore v0 from (-36)fp
     */
    public void restoreV0() {
        load("v0", -36, "fp");
    }
}
