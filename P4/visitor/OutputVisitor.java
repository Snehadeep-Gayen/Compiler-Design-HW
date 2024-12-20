package visitor;

import java.util.*;
import syntaxtree.*;

public class OutputVisitor extends GJDepthFirst<String, String> {
    HashMap<Integer, String> regMap;
    HashMap<Integer, Integer> startMap;
    HashMap<Integer, Integer> endMap;
    ArrayList<Instruction> insts;
    Integer maxSpilled;
    Integer biggestFunctionCall;
    boolean debug;
    Generator gen;
    Integer arguments;
    Integer arg_number;
    Integer currentLine;
    String procedureName;

    public OutputVisitor(HashMap<Integer, String> regMap,
            Integer maxSpilled,
            Integer biggestFunctionCall,
            HashMap<Integer, Integer> startMap,
            HashMap<Integer, Integer> endMap,
            ArrayList<Instruction> insts,
            boolean debug) {
        this.startMap = startMap;
        this.endMap = endMap;
        this.regMap = regMap;
        this.maxSpilled = maxSpilled;
        this.debug = debug;
        this.biggestFunctionCall = biggestFunctionCall;
        this.gen = new Generator(debug);
        this.insts = insts;
        arguments = 0;
        arg_number = 0;
        currentLine = 0;
    }

    boolean isGenRegister(String location) {
        if (location.charAt(0) == 't' || location.charAt(0) == 's') {
            return true;
        }
        return false;
    }

    Integer getSpilledNo(String sp) {
        // TODO: Assuming "SPILLEDARG NO" format
        return Integer.parseInt(sp.substring(10));
    }

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public String visit(Goal n, String argu) {
        String _ret = null;

        procedureName = "MAIN";

        System.out.println("MAIN [0] [" +
                Integer.toString(maxSpilled + 18) +
                "] [" + biggestFunctionCall.toString() + "]");

        n.f1.accept(this, argu);

        arguments = 0;

        gen.end();
        if (maxSpilled > 0) {
            System.out.println("// SPILLED");
        } else {
            System.out.println("// NOTSPILLED");
        }
        return _ret;
    }

    /**
     * f0 -> ( ( Label() )? Stmt() )*
     */
    public String visit(StmtList n, String argu) {
        String _ret = null;
        n.f0.accept(this, "label");
        return _ret;
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */
    public String visit(Procedure n, String argu) {
        String _ret = null;

        procedureName = n.f0.f0.tokenImage;

        arguments = Integer.parseInt(n.f2.f0.toString());

        currentLine = arguments;

        Integer maxStackSize = arguments;
        maxStackSize += 18;
        maxStackSize += maxSpilled;
        maxStackSize += biggestFunctionCall;

        System.out.println(n.f0.f0.toString() +
                " [" + arguments + "]" +
                " [" + maxStackSize.toString() + "] " +
                " [" + biggestFunctionCall.toString() + "] ");

        // save the callee saved registers
        for (Integer i = 0; i < 8; i++) {
            // save ith register into SPILLEDARG i+arguments-4
            gen.astore_number(Math.max(arguments - 4, 0) + i, "s" + Integer.toString(i));
        }

        // copy arguments into respective positions
        // for first 4 arguments
        for (int i = 0; i < 4 && i < arguments; i++) {
            if (regMap.containsKey(i)) {
                // Check which register it is
                String location = regMap.get(i);
                if (isGenRegister(location)) {
                    gen.move(location, "a" + Integer.toString(i));
                } else {
                    gen.astore(location, "a" + Integer.toString(i));
                }
            }
        }

        // for next arguments
        for (int i = 4; i < arguments; i++) {

            // move v0 to the correct location
            if (regMap.containsKey(i)) {
                // move SPILLEDARG i to register v0
                gen.aload_number("v0", i - 4);

                // Check which register it is
                String location = regMap.get(i);
                if (isGenRegister(location)) {
                    gen.move(location, "v0");
                } else {
                    gen.astore(location, "v0");
                }
            }
        }

        n.f4.accept(this, argu);

        // restore the callee saved registers
        for (Integer i = 0; i < 8; i++) {
            // restore ith register from SPILLEDARG i+arguments-4
            gen.aload_number("s" + Integer.toString(i), Math.max(arguments - 4, 0) + i);
        }

        gen.end();
        if (maxSpilled > 0) {
            System.out.println("// SPILLED");
        } else {
            System.out.println("// NOTSPILLED");
        }
        return _ret;
    }

    /**
     * f0 -> NoOpStmt()
     * | ErrorStmt()
     * | CJumpStmt()
     * | JumpStmt()
     * | HStoreStmt()
     * | HLoadStmt()
     * | MoveStmt()
     * | PrintStmt()
     */
    public String visit(Stmt n, String argu) {
        String _ret = null;
        argu = null;
        n.f0.accept(this, argu);
        currentLine++;
        return _ret;
    }

    /**
     * f0 -> "NOOP"
     */
    public String visit(NoOpStmt n, String argu) {
        String _ret = null;
        gen.noop();
        return _ret;
    }

    /**
     * f0 -> "ERROR"
     */
    public String visit(ErrorStmt n, String argu) {
        String _ret = null;
        gen.error();
        return _ret;
    }

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public String visit(CJumpStmt n, String argu) {
        String _ret = null;

        Integer tempNo = Integer.parseInt(n.f1.f1.f0.toString());
        if (regMap.containsKey(tempNo)) {
            String location = regMap.get(tempNo);

            // check if the temp is live
            if (insts.get(currentLine).liveIn.contains(tempNo)) {
                // means that the temp is needed
                if (isGenRegister(location)) {
                    gen.cjump(location, n.f2.f0.toString()+"___"+procedureName);
                } else {
                    gen.aload("v0", location);
                    gen.cjump("v0", n.f2.f0.toString()+"___"+procedureName);
                }
            } else {
                if (debug) {
                    System.out.println("noop " + Integer.toString(currentLine));
                }
                gen.noop();
            }
        } else {
            gen.noop();
        }

        return _ret;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public String visit(JumpStmt n, String argu) {
        String _ret = null;
        gen.jump(n.f1.f0.toString()+"___"+procedureName);
        return _ret;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public String visit(HStoreStmt n, String argu) {
        String _ret = null;
        Integer temp1 = Integer.parseInt(n.f1.f1.f0.toString());
        Integer temp2 = Integer.parseInt(n.f3.f1.f0.toString());

        if (regMap.containsKey(temp1) && regMap.containsKey(temp2)) {
            String location1 = regMap.get(temp1);
            String location2 = regMap.get(temp2);

            if (!insts.get(currentLine).liveIn.contains(temp1)) {
                if (debug) {
                    System.out.println("noop: " + currentLine + " ");
                }
                gen.noop();
                return _ret;
            }

            if (!insts.get(currentLine).liveIn.contains(temp2)) {
                if (debug) {
                    System.out.println("noop: " + currentLine);
                }
                gen.noop();
                return _ret;
            }

            int index = Integer.parseInt(n.f2.f0.toString());

            if (isGenRegister(location1) && isGenRegister(location2)) {
                gen.hstore(location1, index, location2);
            } else if (isGenRegister(location1) && !isGenRegister(location2)) {
                gen.aload("v1", location2);
                gen.hstore(location1, index, "v1");
            } else if (!isGenRegister(location1) && isGenRegister(location2)) {
                gen.aload("v0", location1);
                gen.hstore("v0", index, location2);
            } else {
                gen.aload("v0", location1);
                gen.aload("v1", location2);
                gen.hstore("v0", index, "v1");
            }
        } else {
            gen.noop();
        }
        return _ret;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public String visit(HLoadStmt n, String argu) {
        String _ret = null;

        Integer temp1 = Integer.parseInt(n.f1.f1.f0.toString());
        Integer temp2 = Integer.parseInt(n.f2.f1.f0.toString());

        if (regMap.containsKey(temp1) && regMap.containsKey(temp2)) {

            String location1 = regMap.get(temp1);
            String location2 = regMap.get(temp2);

            if (!insts.get(currentLine).liveOut.contains(temp1)) {
                if (debug) {
                    System.out.println("noop: " + currentLine );
                }
                gen.noop();
                return _ret;
            }

            if (!insts.get(currentLine).liveIn.contains(temp2)) {
                if (debug) {
                    System.out.println("noop: " + currentLine);
                }
                gen.noop();
                return _ret;
            }

            int index = Integer.parseInt(n.f3.f0.toString());

            if (isGenRegister(location1) && isGenRegister(location2)) {
                gen.hload(location1, location2, index);
            } else if (isGenRegister(location1)) {
                gen.aload("v1", location2);
                gen.hload(location1, "v1", index);
            } else if (isGenRegister(location2)) {
                gen.hload("v1", location2, index);
                gen.astore(location1, "v1");
            } else {
                gen.aload("v1", location2);
                gen.hload("v1", "v1", index);
                gen.astore(location1, "v1");
            }
        } else {
            gen.noop();
        }
        return _ret;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public String visit(MoveStmt n, String argu) {
        String _ret = null;

        Integer tempNo = Integer.parseInt(n.f1.f1.f0.toString());
        
        boolean called = false;

        if(n.f2.f0.which == 0){
            n.f2.accept(this, "move");
            called = true;
        }

        if (regMap.containsKey(tempNo)) {

            if (!insts.get(currentLine).liveOut.contains(tempNo)) {
                if (debug) {
                    System.out.println("noop: " + currentLine);
                }
                gen.noop();
                return _ret;
            }

            gen.comment("Starting Move");

            // its expected that the result will be in v1 register after this
            // but if some register is not live then it non null value will be returned
            if(!called)
                n.f2.accept(this, "move");

            String location = regMap.get(tempNo);

            if (isGenRegister(location)) {
                gen.move(location, "v1");
            } else {
                gen.astore(location, "v1");
            }

            gen.comment("Ending Move");
        } else {
            // means that the temp is not needed
            // so put a NOOP instruction instead
            gen.noop();
        }
        return _ret;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public String visit(PrintStmt n, String argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        // it is expected that the answer will be in v1
        if (n.f1.f0.which == 0) {
            // means that it is a temp
            Integer tempNo = Integer.parseInt(((Temp) n.f1.f0.choice).f1.f0.tokenImage);
            if (regMap.containsKey(tempNo)) {
                String location = regMap.get(tempNo);

                if (!insts.get(currentLine).liveIn.contains(tempNo)) {
                    if (debug) {
                        System.out.println("noop: " + currentLine);
                    }
                    gen.noop();
                    return _ret;
                }

                if (isGenRegister(location)) {
                    gen.print(location);
                } else {
                    gen.aload("v1", location);
                    gen.print("v1");
                }
            } else {
                gen.noop();
            }
        } else {
            n.f1.accept(this, argu);
            gen.print("v1");
        }
        return _ret;
    }

    /**
     * f0 -> Call()
     * | HAllocate()
     * | BinOp()
     * | SimpleExp()
     */
    public String visit(Exp n, String argu) {
        // its expected that the answer will be in v1 if its not returned
        n.f0.accept(this, argu);
        return null;
    }

    /**
     * f0 -> "BEGIN"
     * f1 -> StmtList()
     * f2 -> "RETURN"
     * f3 -> SimpleExp()
     * f4 -> "END"
     */
    public String visit(StmtExp n, String argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        // it is expected that answer will be stored in "v1"
        n.f3.accept(this, argu);
        gen.move("v0", "v1");
        return _ret;
    }

    /**
     * f0 -> "CALL"
     * f1 -> SimpleExp()
     * f2 -> "("
     * f3 -> ( Temp() )*
     * f4 -> ")"
     */
    public String visit(Call n, String argu) {
        String _ret = null;

        // save caller saved registers
        for (int i = 0; i < 10; i++) {
            // TODO: check this once
            int spilledLocation = Math.max(arguments - 4, 0) + 8 + i;
            gen.astore("SPILLEDARG " + spilledLocation, "t" + i);
        }

        arg_number = 0;

        n.f3.accept(this, "call");
        n.f1.accept(this, argu);
        gen.custom("CALL v1");
        gen.move("v1", "v0");

        // restore caller saved registers
        for (int i = 0; i < 10; i++) {
            // TODO: check this once
            int spilledLocation = Math.max(arguments - 4, 0) + 8 + i;
            gen.aload("t" + i, "SPILLEDARG " + spilledLocation);
        }
        return _ret;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public String visit(HAllocate n, String argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        gen.move("v1", "HALLOCATE " + "v1");
        return _ret;
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public String visit(BinOp n, String argu) {
        String _ret = null;
        String operation = null;
        switch (n.f0.f0.which) {
            case 0:
                operation = "LE";
                break;
            case 1:
                operation = "NE";
                break;
            case 2:
                operation = "PLUS";
                break;
            case 3:
                operation = "MINUS";
                break;
            case 4:
                operation = "TIMES";
                break;
            case 5:
                operation = "DIV";
                break;
        }

        Integer tempNo = Integer.parseInt(n.f1.f1.f0.toString());

        n.f2.accept(this, argu);

        if (regMap.containsKey(tempNo)) {

            String location = regMap.get(tempNo);

            if (isGenRegister(location)) {
                gen.move("v1", operation + " " + location + " " + "v1");
            } else {
                gen.aload("v0", location);
                gen.move("v1", operation + " v0 " + "v1");
            }
        } else {
            // means that the temp is not needed
            // so put a NOOP instruction instead
            gen.noop();
        }
        return _ret;
    }

    /**
     * f0 -> "LE"
     * | "NE"
     * | "PLUS"
     * | "MINUS"
     * | "TIMES"
     * | "DIV"
     */
    public String visit(Operator n, String argu) {
        String _ret = null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Temp()
     * | IntegerLiteral()
     * | Label()
     */
    public String visit(SimpleExp n, String argu) {
        String _ret = null;
        if (n.f0.which == 0) {

            Integer tempNo = Integer.parseInt(((Temp) (n.f0.choice)).f1.f0.toString());

            if (regMap.containsKey(tempNo)) {
                String location = regMap.get(tempNo);

                if (isGenRegister(location)) {
                    gen.move("v1", location);
                } else {
                    gen.aload("v1", location);
                }
            } else {
                gen.noop();
            }
        } else {
            n.f0.accept(this, argu);
        }
        return _ret;
    }

    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public String visit(Temp n, String argu) {
        String _ret = null;
        if (argu == "call") {
            Integer tempNo = Integer.parseInt(n.f1.f0.toString());
            if (regMap.containsKey(tempNo)) {
                String location = regMap.get(tempNo);

                if (isGenRegister(location)) {
                    arg_number++;
                    if (arg_number > 4)
                        gen.passarg(arg_number - 4, location);
                    else
                        gen.move("a" + (arg_number - 1), location);
                } else {
                    gen.aload("v1", location);
                    arg_number++;
                    if (arg_number > 4)
                        gen.passarg(arg_number - 4, "v1");
                    else
                        gen.move("a" + (arg_number - 1), "v1");
                }
            } else {
                gen.noop();
            }
        } else {
            Integer tempNo = Integer.parseInt(n.f1.f0.toString());
            if (regMap.containsKey(tempNo)) {
                String location = regMap.get(tempNo);

                if (isGenRegister(location)) {
                    gen.move("v1", location);
                } else {
                    gen.aload("v1", location);
                    // gen.print("v1");
                }
            } else {
                gen.noop();
            }
        }
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) {
        n.f0.accept(this, argu);
        gen.move("v1", n.f0.tokenImage);
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Label n, String argu) {
        n.f0.accept(this, argu);
        if (argu == "label") {
            gen.custom(n.f0.tokenImage+"___"+procedureName);
        }
        else{
            gen.move("v1", n.f0.tokenImage);
        }
        return null;
    }

}
