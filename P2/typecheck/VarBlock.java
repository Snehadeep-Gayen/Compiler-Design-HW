package typecheck;
import typecheck.*;
import syntaxtree.*;
import java.util.*;
import visitor.*;

public class VarBlock{
    // stores name, symboltype
    HashMap<String, String> table;
    public VarBlock prev;
    boolean debug;

    public VarBlock(VarBlock parent){
        prev = parent;
        table = new HashMap<String,String>();
        debug = false;
    }

    public void put(String name, String symbol){
        if(debug)
            print();
        contains(name); // program exits if clash found
        table.put(name, symbol);
    }

    public String get(String name){
        if(table.containsKey(name))
            return table.get(name);
        if(prev==null)
            return null;
        return prev.get(name);
    }

    public void contains(String name){
        if(table.containsKey(name))
            (new ErrorFunc()).throwTypeError(name+"VarBlock contains"+name);
        return ; // TODO: Check this
        //if(prev!=null)
          //  prev.contains(name);
    }

    public void print(){
        if(!debug)
            return;

        for(int i=0; i<20; i++)
            System.out.print("*");
        System.out.println("");
        
        for (Map.Entry<String, String> entry : table.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + " => " + value);
        }

        for(int i=0; i<20; i++)
            System.out.print("*");
        System.out.println("");

        if(prev!=null)
            prev.print();
    }
}
