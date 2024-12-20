package visitor;

import java.util.*;

public class Instruction {
    public String inst;    // doesn't matter
    public ArrayList<Integer> pred, succ, def, use;
    public ArrayList<Integer> liveIn, liveOut, liveInOld, liveOutOld;
    public int lineNumber;

    /**
     * Creates an instruction
     * @param inst
     * @param lineNumber
     */
    public Instruction(String inst, int lineNumber) {
        this.inst = inst;
        this.lineNumber = lineNumber;
        pred = new ArrayList<Integer>();
        succ = new ArrayList<Integer>();
        liveIn = new ArrayList<Integer>();
        liveOut = new ArrayList<Integer>();
        liveInOld = new ArrayList<Integer>();
        liveOutOld = new ArrayList<Integer>();
        def = new ArrayList<Integer>();
        use = new ArrayList<Integer>();
    }    

    public void addPred(int pred){
        this.pred.add(pred);
    }

    public void addSucc(int succ){
        this.succ.add(succ);
    }
}
