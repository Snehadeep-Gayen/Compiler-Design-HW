import syntaxtree.*;
import visitor.*;
import typecheck.*;
import java.util.*;

public class P2 {
   static boolean debug = false;
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         MakeGraph g = new MakeGraph();
         root.accept(g); 
         Node mainClass = g.getMainClass();
         if(debug)
            g.showArray();
         Forest f = new Forest(g.createGraph(), mainClass);
         if(debug)
            f.printForest();
         if(f.countNodes()!=g.getNumberOfClasses())
             (new ErrorFunc()).throwCyclicError();
         ClassVisitor cv = new ClassVisitor(f, g);
         f.apply(cv);
         System.out.println("Program type checked successfully"); 
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 


