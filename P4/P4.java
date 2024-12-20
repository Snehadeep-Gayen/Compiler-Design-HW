import java.util.*;
import syntaxtree.*;
import visitor.*;

public class P4{
    static boolean debug = false;
    public static void main(String [] args){
        try{
            Node root = new microIRParser(System.in).Goal();
            ProgramVisitor programVisitor = new ProgramVisitor(debug);
            root.accept(programVisitor);
        }
        catch(ParseException e){
            System.out.println(e.toString());
        }
    }
}
