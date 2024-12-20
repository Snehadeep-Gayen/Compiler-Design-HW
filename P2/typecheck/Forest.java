package typecheck;
import typecheck.*;
import syntaxtree.*;
import java.util.*;
import visitor.*;

public class Forest{
    ArrayList<TreeNode> forest;
    Node mainClass;
    boolean debug;

    public Forest(ArrayList<TreeNode> f, Node _mainClass){
        mainClass = _mainClass;
        forest = f;
        debug = false;
    }

    private void printTree(TreeNode tn, int depth){
        for(int i=0; i<depth; i++)
            System.out.print("\t");
        Node n = tn.getNode();
        if(n instanceof ClassDeclaration)
            System.out.println(((ClassDeclaration) n).f1.f0.tokenImage);
        if(n instanceof ClassExtendsDeclaration)
            System.out.println(((ClassExtendsDeclaration) n).f1.f0.tokenImage);
        for(int i=0; i<tn.getSize(); i++)
            printTree(tn.getChild(i), depth+1);
    }

    public void printForest(){
        for(int i=0; i<forest.size(); i++)
            printTree(forest.get(i), 0);
    }

    private int countTreeNodes(TreeNode n){
        int sum = 1;
        for(int i=0; i<n.getSize(); i++)
            sum += countTreeNodes(n.getChild(i));
        return sum;
    }

    public int countNodes(){
        int sum = 0;
        for(int i=0; i<forest.size(); i++)
            sum += countTreeNodes(forest.get(i));
        return sum;
    }

    private void applyTree(ClassVisitor cv, TreeNode tn){
        Node n = tn.getNode();
        if(debug){
            if(n instanceof ClassDeclaration)
                System.out.println("Before Class "+((ClassDeclaration) n).f1.f0.tokenImage);
            if(n instanceof ClassExtendsDeclaration)
                System.out.println("Before Class "+((ClassExtendsDeclaration) n).f1.f0.tokenImage);
            cv.printBlocks();
        }
        if(! (n instanceof MainClass))
            n.accept(cv, null);
        for(int i=0; i<tn.getSize(); i++)
            applyTree(cv, tn.getChild(i));
        if(debug){
            if(n instanceof ClassDeclaration)
                System.out.println("After Class "+((ClassDeclaration) n).f1.f0.tokenImage);
            if(n instanceof ClassExtendsDeclaration)
                System.out.println("After Class "+((ClassExtendsDeclaration) n).f1.f0.tokenImage);
            cv.printBlocks();
        }
        if(! (n instanceof MainClass))
            cv.removeCurrentScope();
    }

    public void apply(ClassVisitor cv){
        ((MainClass) mainClass).accept(cv, null);
        for(int i=0; i<forest.size(); i++)
            applyTree(cv, forest.get(i));
    }
}
