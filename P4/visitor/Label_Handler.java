package visitor;

import syntaxtree.*;
import java.util.*;

public class Label_Handler extends DepthFirstVisitor{

    int currentLine;
    HashMap<String, Integer> labelMap;
    ArrayList<String> currentLabelList;
    boolean debug;

    public Label_Handler(boolean debug, int offset){
        this.debug = debug;
        currentLine = offset;
        labelMap = new HashMap<String, Integer>();
        currentLabelList = new ArrayList<String>();
    }

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public void visit(Goal n) {
        n.f1.accept(this);
        // don't visit procedures
    }

    /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public void visit(StmtList n) {
        currentLabelList.clear();
        n.f0.accept(this);
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public void visit(Label n) {
        currentLabelList.add(n.f0.toString());
        n.f0.accept(this);
   }

   /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public void visit(Stmt n) {
        if(currentLabelList.size() > 0){
            for(String label : currentLabelList){
                labelMap.put(label, currentLine);
            }
        }
        currentLabelList.clear();
        currentLine++;
   }
    
    public HashMap<String, Integer> getLabelMap(){
        return labelMap;
    }

    public Integer getNoInst(){
        return currentLine;
    }
}
