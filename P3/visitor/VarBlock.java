package visitor;
import java.util.*;

public class VarBlock {
    private ArrayList<String> vars;
    private ArrayList<String> className;
    private ArrayList<String> type;
    private String ofClassName;

    VarBlock(String className){
        vars = new ArrayList<>();
        this.className = new ArrayList<>();
        this.type = new ArrayList<>();
        ofClassName = className;
    }

    VarBlock(VarBlock vb){
        vars = new ArrayList<>();
        className = new ArrayList<>();
        type = new ArrayList<>();
        for(String s : vb.vars){
            vars.add(s);
        }
        for(String s : vb.className){
            className.add(s);
        }
        for(String s : vb.type){
            type.add(s);
        }
        ofClassName = vb.ofClassName;
    }

    void addVar(String var, String curclass, String type){
        // check if its already in the list
        // for(int i = 0; i < vars.size(); i++){
        //     if(vars.get(i).equals(var) && className.get(i).equals(curclass)){
        //         return;
        //     }
        // }
        vars.add(var);
        className.add(curclass);
        this.type.add(type);
    }

    int getPosition(String var){
        for(int i = vars.size()-1; i >= 0; i--){
            if(vars.get(i).equals(var)){
                return i;
            }
        }
        return -1;
    }

    void merge(VarBlock vb, String parentClass){
        VarBlock newBlock = new VarBlock(vb);
        for(int i = 0; i < vars.size(); i++){
            newBlock.addVar(vars.get(i), className.get(i), type.get(i));
        }
        vars = newBlock.vars;
        className = newBlock.className;
        type = newBlock.type;
    }

    int getNumberVars(){
        return vars.size();
    }

    String getType(String var){
        for(int i = vars.size()-1; i >= 0; i--){
            if(vars.get(i).equals(var)){
                return type.get(i);
            }
        }
        return "unknownVal";
    }

    void print(){
        for(int i = 0; i < vars.size(); i++){
            System.out.println("Var: " + vars.get(i) + " Class: " + className.get(i) + " Type: " + type.get(i));
        }
    }
}