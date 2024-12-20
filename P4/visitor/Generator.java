package visitor;
import java.util.*;

public class Generator {
    boolean debug;

    public Generator(boolean debug){
        this.debug = debug;
    }

    public void comment(String comm){
        // System.out.println("/* "+comm+" */");
    }

    public void hstore(String reg1, Integer offset, String reg2){
        System.out.println("HSTORE " + reg1 + " " + offset.toString() + " " + reg2);
    }

    public void hload(String reg1, String reg2, Integer offset){
        System.out.println("HLOAD " + reg1 + " " + reg2 + " " + offset.toString());
    }

    public void astore_number(Integer spilledArgNo, String register){
        System.out.println("ASTORE SPILLEDARG " + 
                            spilledArgNo.toString() + 
                            " " + register);
    }

    public void astore(String spilledArg, String register) {
        System.out.println("ASTORE "+ spilledArg + " " + register);
    }

    public void aload(String register, String spilledArg){
        System.out.println("ALOAD " + register + " " + spilledArg);
    }

    public void aload_number(String register, Integer spilledArgNo){
        System.out.println("ALOAD " + register + 
                            " SPILLEDARG " + 
                            spilledArgNo.toString());
    }
 
    public void move(String register, String arg){
        System.out.println("MOVE " + register + " " + arg);
    }

    public void cjump(String register, String label){
        System.out.println("CJUMP " + register + " " + label);
    }

    public void jump(String label){
        System.out.println("JUMP "+label);
    }

    public void custom(String custom){
        System.out.println(custom);
    }

    public void passarg(Integer argNo, String reg){
        System.out.println("PASSARG "+argNo.toString()+" "+reg);
    }

    public void end(){
        System.out.println("END");
    }

    public void noop(){
        System.out.println("NOOP");
    }

    public void error(){
        System.out.println("ERROR");
    }

    public void print(String reg){
        System.out.println("PRINT "+reg);
    }
}
