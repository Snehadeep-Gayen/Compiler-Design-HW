import syntaxtree.*;
import visitor.*;
import java.util.*;

public class P3{
   static boolean debug = false;
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         if(debug){
            System.out.println("/* \n Program parsed successfully");
         }
         // First pass
         MakeVTable mv = new MakeVTable(debug);
         root.accept(mv);
         mv.createVTables();
         if(debug){
            System.out.println("VTables created");
         }
         MicroIR mir = new MicroIR(debug);
         GenIR gen = new GenIR(mir, mv.getClassData(), debug);
         root.accept(gen, null);
         if(debug){
            System.out.println("IR generated */\n");
         }
         mir.printIR();
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 


