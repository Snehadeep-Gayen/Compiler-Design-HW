package typecheck;
import typecheck.*;
import syntaxtree.*;
import java.util.*;
import visitor.*;

public class Method{
    public String className;
    public String name;
    public String returnType;
    public ArrayList<String> argName;
    public ArrayList<String> argType;

    public Method(String _cName, String _n, String _rType){
        className = _cName;
        name = _n;
        returnType = _rType;
        argName = new ArrayList<String>();
        argType = new ArrayList<String>();
    }

    public void addArgument(String _argN, String _argT){
        // no need to check for unique arguments, VarBlock will take care of that
        argName.add(_argN);
        argType.add(_argT);
    }

    public void print(){
        System.out.println("ClassName\t\tMethodName\t\tReturns");
        System.out.println(className+"\t\t"+name+"\t\t"+returnType);
        for(int i=0; i<argName.size(); i++)
            System.out.println(argName.get(i)+" of type "+argType.get(i));
    }

}
