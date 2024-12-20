package visitor;

import java.util.*;
import syntaxtree.*;

class ProcedureVisitor extends GJDepthFirst<ArrayList<Integer>, ArrayList<Integer>> {
    boolean debug;
    ArrayList<Instruction> insts;
    HashMap<String, Integer> labelMap;
    String procedureName;
    int noInst;
    int noArgs;
    int maxArgs;
    // For Register Allocation
    HashMap<Integer, Integer> startMap = new HashMap<>();
    HashMap<Integer, Integer> endMap = new HashMap<>();
    /**
     * {@code Stores the current statement number}
     */
    int curStatNo;

    // TODO: how to get the passed args when number of args is > 4 ?

    /**
     * Constructor
     * 
     * @param debug {@code boolean}
     */
    public ProcedureVisitor(boolean debug, HashMap<String, Integer> labelMap, Integer noInst) {
        this.debug = debug;
        insts = new ArrayList<Instruction>();
        this.labelMap = labelMap;
        procedureName = "";
        noArgs = 0;
        maxArgs = 0;
        this.noInst = noInst;
    }

    public Integer getMaxArgs() {
        return maxArgs;
    }

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public ArrayList<Integer> visit(Goal n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // treat stuff differently if it is main
        procedureName = "MAIN";
        noArgs = 0;

        // call the Label Handler visitor
        // Label_Handler lh = new Label_Handler(debug, noArgs);
        // lh.visit(n);
        // labelMap = lh.getLabelMap();
        // int noInst = lh.getNoInst();

        // add instructions to store the arguments
        for (int i = 0; i < noArgs; i++) {
            Instruction inst = new Instruction("", i);
            inst.def.add(i);
            insts.add(inst);
        }

        // add dummy instruction for rest
        for (int i = noArgs; i < noInst; i++) {
            insts.add(new Instruction("", i));
        }

        // add dummy return statement
        insts.add(new Instruction("RETURN", noInst));

        curStatNo = 0;

        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */
    public ArrayList<Integer> visit(Procedure n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // set number of arguments
        noArgs = Integer.parseInt(n.f2.f0.toString());

        // call the Label Handler visitor
        // Label_Handler lh = new Label_Handler(debug, noArgs);
        // lh.visit(n);
        // labelMap = lh.getLabelMap();
        // int noInst = lh.getNoInst();

        // set procedure Name
        procedureName = n.f0.f0.toString();

        // add instructions to store the arguments
        for (int i = 0; i < noArgs; i++) {
            Instruction inst = new Instruction("", i);
            inst.def.add(i);
            inst.succ.add(i + 1);
            insts.add(inst);
            if (i > 0)
                insts.get(i).pred.add(i - 1);
        }

        // add dummy instruction for rest
        for (int i = noArgs; i < noInst; i++) {
            insts.add(new Instruction("", i));
        }
        
        // add String final return statement
        insts.add(new Instruction("RETURN", noInst));

        // TODO: Chck if this is needed
        if (noArgs > 0)
            insts.get(noArgs).pred.add(noArgs - 1);

        // update the current statement number
        curStatNo = noArgs;

        // debug print
        if (debug) {
            System.out.println("Procedure: " + procedureName);
            System.out.println("Number of arguments: " + noArgs);
            System.out.println("Number of instructions: " + noInst);
            System.out.println("Current statement number: " + curStatNo);
        }

        // process statements
        n.f4.accept(this, argu);

        if (debug) {
            System.out.println("After populating def, use, pred, succ");
            print();
        }
        return _ret;
    }

    /**
     * f0 -> "BEGIN"
     * f1 -> StmtList()
     * f2 -> "RETURN"
     * f3 -> SimpleExp()
     * f4 -> "END"
     */
    public ArrayList<Integer> visit(StmtExp n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // process statements
        n.f1.accept(this, argu);

        // process return statement
        ArrayList<Integer> use = n.f3.accept(this, argu);
        insts.get(curStatNo).use = use;

        return _ret;
    }

    ////////////////// PROCESSING STATEMENTS /////////////////////////

    /**
     * f0 -> "NOOP"
     */
    public ArrayList<Integer> visit(NoOpStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // name the instruction
        insts.get(curStatNo).inst = "NOOP";

        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);
        insts.get(curStatNo).addSucc(curStatNo + 1);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "ERROR"
     */
    public ArrayList<Integer> visit(ErrorStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // name the instruction
        insts.get(curStatNo).inst = "ERROR";

        // TODO: Check this once
        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);
        insts.get(curStatNo).addSucc(curStatNo + 1);
        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public ArrayList<Integer> visit(CJumpStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // name the instruction
        insts.get(curStatNo).inst = "CJUMP";

        // get the temp number defined
        ArrayList<Integer> use1 = n.f1.accept(this, argu);

        // add the temp number to def of current statement
        insts.get(curStatNo).use = (use1);

        // get the instruction number corresponding to the label
        int labelNo = labelMap.get(n.f2.f0.toString());

        // add current statement to pred of the labelNo
        insts.get(labelNo).addPred(curStatNo);

        // add the labelNo to succ of current statement
        insts.get(curStatNo).addSucc(labelNo);

        // add the next the statement
        insts.get(curStatNo + 1).addPred(curStatNo);
        insts.get(curStatNo).addSucc(curStatNo + 1);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public ArrayList<Integer> visit(JumpStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;

        // name the instruction
        insts.get(curStatNo).inst = "JUMP";

        // get the instruction number corresponding to the label
        int labelNo = labelMap.get(n.f1.f0.toString());

        // add current statement to pred of the labelNo
        insts.get(labelNo).addPred(curStatNo);

        // add the labelNo to succ of current statement
        insts.get(curStatNo).addSucc(labelNo);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public ArrayList<Integer> visit(HStoreStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;
        ArrayList<Integer> use1 = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        ArrayList<Integer> use2 = n.f3.accept(this, argu);

        ArrayList<Integer> use = new ArrayList<Integer>();
        use.addAll(use1);
        use.addAll(use2);

        // name the instruction
        insts.get(curStatNo).inst = "HSTORE";

        // add def and use to current statement
        insts.get(curStatNo).def = null;
        insts.get(curStatNo).use = (use);

        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public ArrayList<Integer> visit(HLoadStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;
        n.f0.accept(this, argu);
        ArrayList<Integer> def = n.f1.accept(this, argu);
        ArrayList<Integer> use = n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        // name the instruction
        insts.get(curStatNo).inst = "HLOAD";

        // add def and use to current statement
        insts.get(curStatNo).def = (def);
        insts.get(curStatNo).use = (use);

        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public ArrayList<Integer> visit(MoveStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;
        n.f0.accept(this, argu);
        ArrayList<Integer> def = n.f1.accept(this, argu);
        ArrayList<Integer> use = n.f2.accept(this, argu);

        // name the instruction
        insts.get(curStatNo).inst = "MOVE";

        // add def and use to current statement
        insts.get(curStatNo).def = (def);
        insts.get(curStatNo).use = (use);

        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);

        curStatNo++;

        return _ret;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public ArrayList<Integer> visit(PrintStmt n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;
        n.f0.accept(this, argu);
        ArrayList<Integer> use = n.f1.accept(this, argu);

        // name the instruction
        insts.get(curStatNo).inst = "PRINT";

        // add use to current statement
        insts.get(curStatNo).use = (use);

        // add current statement to pred of next statement
        insts.get(curStatNo + 1).addPred(curStatNo);

        curStatNo++;

        return _ret;
    }

    //////////////////////////////////////////////////////////////////

    /////////////////////// SUBPARTS OF STMTS ////////////////////////

    /**
     * f0 -> Call()
     * | HAllocate()
     * | BinOp()
     * | SimpleExp()
     */
    public ArrayList<Integer> visit(Exp n, ArrayList<Integer> argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "CALL"
     * f1 -> SimpleExp()
     * f2 -> "("
     * f3 -> ( Temp() )*
     * f4 -> ")"
     */
    public ArrayList<Integer> visit(Call n, ArrayList<Integer> argu) {
        ArrayList<Integer> subUse = n.f1.accept(this, argu);
        if (subUse == null)
            subUse = new ArrayList<Integer>();
        n.f3.accept(this, subUse);
        // First argument is "this"
        maxArgs = Math.max(maxArgs, subUse.size());
        return subUse;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public ArrayList<Integer> visit(HAllocate n, ArrayList<Integer> argu) {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public ArrayList<Integer> visit(BinOp n, ArrayList<Integer> argu) {

        ArrayList<Integer> use1 = n.f1.accept(this, argu);
        ArrayList<Integer> use2 = n.f2.accept(this, argu);

        // combine use1 and use2 to subUse
        ArrayList<Integer> subUse = new ArrayList<Integer>();
        if (use1 != null)
            subUse.addAll(use1);
        if (use2 != null)
            subUse.addAll(use2);

        return subUse;
    }

    /**
     * f0 -> Temp()
     * | IntegerLiteral()
     * | Label()
     */
    public ArrayList<Integer> visit(SimpleExp n, ArrayList<Integer> argu) {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public ArrayList<Integer> visit(Temp n, ArrayList<Integer> argu) {
        ArrayList<Integer> _ret = null;
        Integer tempNo = Integer.parseInt(n.f1.f0.tokenImage);
        _ret = new ArrayList<Integer>();
        _ret.add(tempNo);

        if (argu != null)
            argu.add(tempNo);

        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public ArrayList<Integer> visit(IntegerLiteral n, ArrayList<Integer> argu) {
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public ArrayList<Integer> visit(Label n, ArrayList<Integer> argu) {
        return null;
    }

    public void print() {
        if (!debug)
            return;
        System.out.println("Procedure " + procedureName + ":");
        System.out.println("Definition of each statement:");
        for (int i = 0; i < insts.size(); i++) {
            System.out.println("Statement " + i + ":");

            System.out.println("    inst: " + insts.get(i).inst);

            System.out.print("    def: ");
            if (insts.get(i).def == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).def.size(); j++)
                    System.out.print(" " + insts.get(i).def.get(j));
            System.out.println("");

            System.out.print("    use: ");
            if (insts.get(i).use == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).use.size(); j++)
                    System.out.print(" " + insts.get(i).use.get(j));
            System.out.println("");

            System.out.print("    succ: ");
            if (insts.get(i).succ == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).succ.size(); j++)
                    System.out.print(" " + insts.get(i).succ.get(j));
            System.out.println("");

            System.out.print("    pred: ");
            if (insts.get(i).pred == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).pred.size(); j++)
                    System.out.print(" " + insts.get(i).pred.get(j));
            System.out.println("");

            // print liveIn and liveOut
            System.out.print("    liveIn: ");
            if (insts.get(i).liveIn == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).liveIn.size(); j++)
                    System.out.print(" " + insts.get(i).liveIn.get(j));
            System.out.println("");

            System.out.print("    liveOut: ");
            if (insts.get(i).liveOut == null)
                System.out.print(" null");
            else
                for (int j = 0; j < insts.get(i).liveOut.size(); j++)
                    System.out.print(" " + insts.get(i).liveOut.get(j));
            System.out.println("");

        }
    }

    //////////////////////////////////////////////////////////////////

    /////////////////////////// LIVENESS ////////////////////////////

    private ArrayList<Integer> setMinus(ArrayList<Integer> a, ArrayList<Integer> b) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < a.size(); i++) {
            if (!b.contains(a.get(i)))
                ret.add(a.get(i));
        }
        return ret;
    }

    private ArrayList<Integer> setUnion(ArrayList<Integer> a, ArrayList<Integer> b) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < a.size(); i++) {
            if (!ret.contains(a.get(i)))
                ret.add(a.get(i));
        }
        for (int i = 0; i < b.size(); i++) {
            if (!ret.contains(b.get(i)))
                ret.add(b.get(i));
        }
        return ret;
    }

    private boolean livenessChanged() {

        for (int i = 0; i < insts.size(); i++) {
            Collections.sort(insts.get(i).liveIn);
            Collections.sort(insts.get(i).liveOut);
            Collections.sort(insts.get(i).liveInOld);
            Collections.sort(insts.get(i).liveOutOld);

            // compare liveIn and liveInOld
            if (!insts.get(i).liveIn.equals(insts.get(i).liveInOld)) {
                return true;
            }

            // compare liveOut and liveOutOld
            if (!insts.get(i).liveOut.equals(insts.get(i).liveOutOld)) {
                return true;
            }
        }
        return false;
    }

    private void populateSucc() {
        for (int i = 0; i < insts.size(); i++) {
            ArrayList<Integer> pred = insts.get(i).pred;
            for (int j = 0; j < pred.size(); j++) {
                int predNo = pred.get(j);
                if (!insts.get(predNo).succ.contains(i))
                    insts.get(predNo).succ.add(i);
            }
        }
    }

    public void findLiveness() {

        // initialise all null lists to empty lists
        for (int i = 0; i < insts.size(); i++) {
            Instruction curInst = insts.get(i);
            if (curInst.def == null)
                curInst.def = new ArrayList<Integer>();
            if (curInst.use == null)
                curInst.use = new ArrayList<Integer>();
            if (curInst.succ == null)
                curInst.succ = new ArrayList<Integer>();
            if (curInst.pred == null)
                curInst.pred = new ArrayList<Integer>();
        }

        // populate successors
        populateSucc();

        int noIterations = 0;

        do {
            noIterations++;
            for (int i = 0; i < insts.size(); i++) {
                Instruction curInst = insts.get(i);

                // replace old
                curInst.liveInOld = curInst.liveIn;
                curInst.liveOutOld = curInst.liveOut;

                // compute new
                curInst.liveIn = setUnion(curInst.use, setMinus(curInst.liveOut, curInst.def));
                curInst.liveOut = new ArrayList<Integer>();
                for (int j = 0; j < curInst.succ.size(); j++) {
                    curInst.liveOut = setUnion(curInst.liveOut, insts.get(curInst.succ.get(j)).liveIn);
                }
            }
        } while (livenessChanged());

        if (debug) {
            System.out.println("No of iterations: " + noIterations);
            for (int i = 0; i < insts.size(); i++) {
                // print liveout
                System.out.print("liveOut: ");
                for (int j = 0; j < insts.get(i).liveOut.size(); j++) {
                    System.out.print(insts.get(i).liveOut.get(j) + " ");
                }
                System.out.println("");
            }
        }

    }

    public ArrayList<Instruction> getInsts() {
        return insts;
    }

    //////////////////////////////////////////////////////////////////

    /////////////////////// REGISTER ALLOCATION //////////////////////

    HashMap<Integer, String> regMap;
    Integer maxSpillage;

    public HashMap<Integer, String> getRegMap() {
        return regMap;
    }

    public Integer getMaxSpillage() {
        return maxSpillage;
    }

    public HashMap<Integer, Integer> getStartMap() {
        return startMap;
    }

    public HashMap<Integer, Integer> getEndMap() {
        return endMap;
    }

    public class Interval {
        Integer start;
        Integer end;
        Integer tempNo;
    }

    public void allocateRegisters() {

        // initialise startMap and endMap
        findRange();

        // TODO: Change this
        if (debug) {
            // print the startMap and endMap
            System.out.println("Liveness Range:");
            for (Integer i : startMap.keySet()) {
                System.out.println(i + " " + startMap.get(i) + " " + endMap.get(i));
            }
        }

        // initialise regMap and maxSpillage
        maxSpillage = 0;

        int spilledArgs = 18 + noArgs;

        regMap = new HashMap<>();
        /**
         * Linear Register ALlocation
         */
        String[] registers = { "s0", "s1", "s2", "s3", "s4", "s5", "s6",
                "s7", "t0", "t1", "t2", "t3", "t4", "t5",
                "t6", "t7", "t8", "t9" };
        // int registers_used = 0;
        // for(Integer i : startMap.keySet()){
        // if(registers_used == 18){
        // regMap.put(i, "SPILLEDARG " + spilledArgs);
        // spilledArgs++;
        // }
        // else{
        // regMap.put(i, registers[registers_used]);
        // registers_used++;
        // }
        // }

        /**
         * Spill all
         */
        // for (Integer i : startMap.keySet()) {
        // regMap.put(i, "SPILLEDARG " + spilledArgs);
        // spilledArgs++;
        // }

        /**
         * Spill only if necessary
         */

        // create intervals
        ArrayList<Interval> intervals = new ArrayList<>();
        for (Integer i : startMap.keySet()) {
            Interval curInterval = new Interval();
            curInterval.start = startMap.get(i);
            curInterval.end = endMap.get(i);
            curInterval.tempNo = i;
            intervals.add(curInterval);
        }

        // sort intervals by start
        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval i1, Interval i2) {
                return i1.start.compareTo(i2.start);
            }
        });

        if (debug) {
            System.out.println("Printing sorted intervals");
            // print sorted Intervals
            for (int i = 0; i < intervals.size(); i++) {
                Interval cur = intervals.get(i);
                System.out.println(cur.tempNo + ": start:" + cur.start + ", end:" + cur.end);
            }
        }

        ArrayList<Interval> active = new ArrayList<>();
        int spillCount = 0;

        for (int i = 0; i < intervals.size(); i++) {
            Interval curInterval = intervals.get(i);

            // expire old intervals
            ArrayList<Interval> newActive = new ArrayList<>();
            for (int j = 0; j < active.size(); j++) {
                if (active.get(j).end >= curInterval.start) {
                    newActive.add(active.get(j));
                }
            }
            active = newActive;

            // spill if necessary
            if (active.size() == 18) {
                spillCount++;
                regMap.put(curInterval.tempNo, "SPILLEDARG " + spilledArgs);
                spilledArgs++;
                active.add(curInterval);
                // now remove the interval with the largest end
                int maxEnd = 0;
                int maxEndIndex = 0;
                for (int j = 0; j < active.size(); j++) {
                    if (active.get(j).end > maxEnd) {
                        maxEnd = active.get(j).end;
                        maxEndIndex = j;
                    }
                }
                // interchange map entry with the spilled one
                String spilledArg = regMap.get(active.get(active.size() - 1).tempNo);
                regMap.put(active.get(active.size() - 1).tempNo, regMap.get(active.get(maxEndIndex).tempNo));
                regMap.put(active.get(maxEndIndex).tempNo, spilledArg);
                active.remove(maxEndIndex);
            } else {
                // check which register is free
                boolean[] free = new boolean[18];
                for (int j = 0; j < 18; j++) {
                    free[j] = true;
                }
                for (int j = 0; j < active.size(); j++) {
                    String reg = regMap.get(active.get(j).tempNo);
                    for (int k = 0; k < 18; k++) {
                        if (reg.equals(registers[k])) {
                            free[k] = false;
                            break;
                        }
                    }
                }
                // assign the first free register
                for (int j = 0; j < 18; j++) {
                    if (free[j]) {
                        regMap.put(curInterval.tempNo, registers[j]);
                        if (debug && curInterval.tempNo == 253)
                            System.out.println("LOL:" + registers[j]);
                        break;
                    } else if (j == 17) {
                        System.out.println("/* Error: No free registers */ ");
                    }
                }
                active.add(curInterval);
            }
        }

        if (debug) {
            System.out.println("Spill Count: " + spillCount + "");
            System.out.println("Register Map:");
            for (Integer i : regMap.keySet()) {
                System.out.println(i + " " + regMap.get(i));
            }
            System.out.println("Spilled Args: " + spilledArgs + "");
            if (spilledArgs < 18) {
                System.out.println("No Spilled Args");
            } else {
                System.out.println("Spilled Args: " + (spilledArgs - 18) + " to " + (spilledArgs - 1));
            }
        }
        maxSpillage = spilledArgs - 18 - noArgs;
    }

    private void findRange() {
        for (int i = 0; i < insts.size(); i++) {
            Instruction curInst = insts.get(i);
            // check only liveOut for start
            for (int j = 0; j < curInst.liveOut.size(); j++) {
                int regNo = curInst.liveOut.get(j);
                if (!startMap.containsKey(regNo)
                    || startMap.get(regNo) > i) {
                    startMap.put(regNo, i);
                }
                if(!endMap.containsKey(regNo)
                    || endMap.get(regNo) < i){
                    endMap.put(regNo, i);
                }
            }
            // check only liveIn for endMap
            for (int j = 0; j < curInst.liveIn.size(); j++) {
                int regNo = curInst.liveIn.get(j);
                if (!startMap.containsKey(regNo)
                    || startMap.get(regNo) > i) {
                    startMap.put(regNo, i);
                }
                if(!endMap.containsKey(regNo)
                    || endMap.get(regNo) < i){
                    endMap.put(regNo, i);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////
}
