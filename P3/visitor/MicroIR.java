package visitor;
import java.util.*;

public class MicroIR {
    ArrayList<String> inst;
    boolean debug;
    int scope = 0;

    public MicroIR(boolean debug){
        inst = new ArrayList<>();
        this.debug = debug;
    }

    public void gen(String s){
        inst.add(s);
        this.genNOOP();
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genNoNOOP(String s){
        inst.add(s);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genComment(String s){
        inst.add("/* "+s+" */");
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genSingleLineComment(String s){
        inst.add("// "+s);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genMoveTempTemp(int tempNo1, int tempNo2){
        inst.add("MOVE TEMP " + tempNo1 + " TEMP " + tempNo2);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genMoveTempConst(int tempNo, int constNo){
        inst.add("MOVE TEMP " + tempNo + " " + constNo);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genMoveTempString(int tempNo, String str){
        inst.add("MOVE TEMP " + tempNo + " " + str);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genHStore(int temp1, int offset, int temp2){
        inst.add("HSTORE TEMP " + temp1 + " " + offset + " TEMP " + temp2);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genHLoad(int temp1, int temp2, int offset){
        inst.add("HLOAD TEMP " + temp1 + " TEMP " + temp2 + " " + offset);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genBinOp(int temp1, String operator, int temp2, String SimpleExp){
        inst.add("MOVE TEMP " + temp1 + " " + operator + " TEMP " + temp2 + " "+SimpleExp);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genStartMethod(String className, String methodName, Integer parameters){
        inst.add(className + "__" + methodName + " [" + parameters + "]");
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
        inst.add("BEGIN");
    }

    public void genEndMethod(){
        inst.add("END");
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genPrint(int tempNo){
        inst.add("PRINT TEMP " + tempNo);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genCJump(int tempNo, String label){
        inst.add("CJUMP TEMP " + tempNo + " " + label);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genJump(String label){
        inst.add("JUMP " + label);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genNOOP(){
        inst.add("NOOP");
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void genCall(int tempNo, int methodReg, ArrayList<Integer> args){
        String s = "MOVE TEMP " + tempNo + " CALL TEMP " + methodReg + " (";
        for(int i = 0; i < args.size(); i++){
            s += "TEMP " + args.get(i) + " ";
        }
        s += ")";
        inst.add(s);
        if(debug){
            System.out.println(inst.get(inst.size()-1));
        }
    }

    public void printIR(){
        for(String s : inst){
            System.out.println(s);
        }
    }
}
