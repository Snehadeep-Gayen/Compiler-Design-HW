import syntaxtree.*;
import visitor.*;
import java.util.*;


public class P5 {
    static boolean debug = false;
    public static void main(String args[]){
        try {
            Node root = new MiniRAParser(System.in).Goal();

            if(debug)
                System.out.println("# Program parsed successfully");            
            
            root.accept(new Mips(debug), "");
            
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }
}