package typecheck;
import typecheck.*;
import syntaxtree.*;
import java.util.*;
import visitor.*;

public class MethodBlock{
    HashMap<String, Method> table;
    public MethodBlock prev;
    MakeGraph g;
    boolean debug;

    public MethodBlock(MethodBlock _prev, MakeGraph _g){
        prev = _prev;
        table = new HashMap<String, Method>();
        debug = false;
        g = _g;
    }

    public void put(Method m){
        contains(m);   // ERROR: Handle overriding here
        table.put(m.name, m);
    }

    public Method get(String name){
        if(table.containsKey(name))
            return table.get(name);
        if(prev==null)
            return null;
        return prev.get(name);
    }

    public void contains(Method m){
        if(table.containsKey(m.name))
            (new ErrorFunc()).throwTypeError(m.name+" MethodBlock contains");
        if(prev!=null)
            prev.containsOverriding(m);
    }

    public void containsOverriding(Method m){
        if(table.containsKey(m.name)){
            // check if signature exactly matches
            Method mParent = table.get(m.name);
            ArrayList<String> l1 = mParent.argType;
            ArrayList<String> l2 = m.argType;
            if(l1.size()!=l2.size())
                (new ErrorFunc()).throwTypeError(m.name+" MethodBlock contains OV1");
            if(m.returnType==mParent.returnType)
                ;
            else if(g.isValidClass(m.returnType) && g.isValidClass(mParent.returnType)
                    && g.isParent(mParent.returnType, m.returnType))
                ;
            else
                (new ErrorFunc()).throwTypeError(m.name+" MethodBlock contains OV1.5");
            for(int i=0; i<l1.size(); i++)
                if(l1.get(i)!=l2.get(i))
                    (new ErrorFunc()).throwTypeError(m.name+" MethodBlock contains OV2");
        }
        if(prev!=null)
            prev.containsOverriding(m);
    }

    public void print(){

        if(!debug)
            return;

        for(int i=0; i<20; i++)
            System.out.print("*");
        System.out.println("");
        
        for (Map.Entry<String, Method> entry : table.entrySet()){
            String key = entry.getKey();
            Method m = entry.getValue();
            System.out.print(key + " => ");
            m.print();
            System.out.println(""); 
        }

        for(int i=0; i<20; i++)
            System.out.print("*");
        System.out.println("");

        if(prev!=null)
            prev.print();
    }
}
