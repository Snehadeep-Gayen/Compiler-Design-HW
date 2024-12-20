package visitor;
import java.util.*;

class ClassData {
    String className;
    String parentClass;
    VarBlock varBlock;
    MethodBlock methodBlock;

    public ClassData(String className, String parentClass){
        this.className = className;
        this.parentClass = parentClass;
        varBlock = new VarBlock(className);
        methodBlock = new MethodBlock(className);
    }

    public void addVar(String var, String curclass, String type){
        varBlock.addVar(var, curclass, type);
    }

    public void addMethod(String method, String curclass, String retType){
        methodBlock.addMethod(method, curclass, retType);
    }

    public int getVarPosition(String var){
        return varBlock.getPosition(var);
    }

    public String getVarType(String var){
        return varBlock.getType(var);
    }

    public int getMethodPosition(String method){
        return methodBlock.getPosition(method);
    }

    public  String getParent(){
        return parentClass;
    }

    public void merge(ClassData cd){
        varBlock.merge(cd.varBlock, parentClass);
        methodBlock.merge(cd.methodBlock, parentClass);
    }

    public ArrayList<String> getMethodLabelList(){
        return methodBlock.getLabelList();
    }

    public int getNumberVars(){
        return varBlock.getNumberVars();
    }

    public String getReturnType(String method){
        return methodBlock.getReturnType(method);
    }

    public void print(){
        System.out.println("Class: " + className);
        System.out.println("Parent: " + parentClass);
        System.out.println("Vars: ");
        varBlock.print();
        System.out.println("Methods: ");
        methodBlock.print();
    }
}
