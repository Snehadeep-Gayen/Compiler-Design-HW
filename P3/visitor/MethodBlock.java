package visitor;
import java.util.*;


class MethodBlock {
    String ofClassName;
    ArrayList<String> methodNames;
    ArrayList<String> classNames;
    ArrayList<String> retTypes;

    public MethodBlock(String className){
        methodNames = new ArrayList<>();
        classNames = new ArrayList<>();
        retTypes = new ArrayList<>();
        ofClassName = className;
    }

    public MethodBlock(MethodBlock mb){
        methodNames = new ArrayList<>();
        classNames = new ArrayList<>();
        retTypes = new ArrayList<>();
        for(String s : mb.methodNames){
            methodNames.add(s);
        }
        for(String s : mb.classNames){
            classNames.add(s);
        }
        for(String s : mb.retTypes){
            retTypes.add(s);
        }
        ofClassName = mb.ofClassName;
    }

    public void addMethod(String method, String curclass, String retType){
        // check if its already in the list
        for(int i = 0; i < methodNames.size(); i++){
            if(methodNames.get(i).equals(method)){
                classNames.set(i, curclass);
                retTypes.set(i, retType);
                return;
            }
        }
        methodNames.add(method);
        classNames.add(curclass);
        retTypes.add(retType);
    }

    public int getPosition(String method){
        for(int i = 0; i < methodNames.size(); i++){
            if(methodNames.get(i).equals(method)){
                return i;
            }
        }
        return -1;
    }

    public void merge(MethodBlock mb, String parentClass){
        MethodBlock newBlock = new MethodBlock(mb);
        // for(String s : methodNames){
        //     newBlock.addMethod(s, ofClassName);
        // }
        for(int i=0; i<methodNames.size(); i++){
            newBlock.addMethod(methodNames.get(i), classNames.get(i), retTypes.get(i));
        }
        methodNames = newBlock.methodNames;
        classNames = newBlock.classNames;
        retTypes = newBlock.retTypes;
    }

    public String getReturnType(String method){
        for(int i = methodNames.size()-1; i >=0 ; i--){
            if(methodNames.get(i).equals(method)){
                return retTypes.get(i);
            }
        }
        return "unknown";
    }

    public ArrayList<String> getLabelList(){
        ArrayList<String> labelList = new ArrayList<>();
        for(int i = 0; i < methodNames.size(); i++){
            labelList.add(classNames.get(i)+"__"+methodNames.get(i));
        }
        return labelList;
    }
    
    public void print(){
        for(int i = 0; i < methodNames.size(); i++){
            System.out.println("Method: " + methodNames.get(i) + " in class: " + 
                                classNames.get(i)+ " returns: " + retTypes.get(i));
        }
    }
}
