
package visitor;

import syntaxtree.*;

public class ProgramVisitor extends DepthFirstVisitor{
    
    boolean debug;

    public ProgramVisitor(boolean debug){
        this.debug = debug;
    }

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public void visit(Goal n) {
        Label_Handler lh = new Label_Handler(debug, 0);
        n.accept(lh);
        ProcedureVisitor mainVisitor = new ProcedureVisitor(
                                                    debug, 
                                                    lh.getLabelMap(), 
                                                    lh.getNoInst());
        n.accept(mainVisitor, null);
        mainVisitor.findLiveness();
        if(debug)
            mainVisitor.print();
        mainVisitor.allocateRegisters();

        OutputVisitor out = new OutputVisitor(
                                mainVisitor.getRegMap(), 
                                mainVisitor.getMaxSpillage(), 
                                mainVisitor.getMaxArgs(),
                                mainVisitor.getStartMap(),
                                mainVisitor.getEndMap(),
                                mainVisitor.getInsts(),
                                debug);
        n.accept(out, null);
        n.f3.accept(this);
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */
    public void visit(Procedure n) {
        int noArgs = Integer.parseInt(n.f2.f0.toString());
        Label_Handler lh = new Label_Handler(debug, noArgs);
        n.accept(lh);
        ProcedureVisitor procVisitor = new ProcedureVisitor(
                                                    debug,
                                                    lh.getLabelMap(),
                                                    lh.getNoInst());
        n.accept(procVisitor, null);
        procVisitor.findLiveness();
        if(debug)
            procVisitor.print();
        procVisitor.allocateRegisters();

        OutputVisitor out = new OutputVisitor(
                                procVisitor.getRegMap(), 
                                procVisitor.getMaxSpillage(), 
                                procVisitor.getMaxArgs(),
                                procVisitor.getStartMap(),
                                procVisitor.getEndMap(),
                                procVisitor.getInsts(),
                                debug);
        n.accept(out, null);
    }

}