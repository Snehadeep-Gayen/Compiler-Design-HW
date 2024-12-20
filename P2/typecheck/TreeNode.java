package typecheck;
import syntaxtree.*;
import java.util.*;

public class TreeNode{
    Node par;
    ArrayList<TreeNode> children;

    public TreeNode(Node p){
        par = p;
        children = new ArrayList<TreeNode>();
    }

    public void addChild(TreeNode n){
        children.add(n);
    }

    public TreeNode getChild(int index){
        if(index>=children.size())
            return null;
        return children.get(index);
    }

    public int getSize(){
        return children.size();
    }

    public Node getNode(){
        return par;
    }
}
