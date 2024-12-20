package visitor;

import syntaxtree.*;
import visitor.Generator;

import java.util.*;

public class Mips extends GJDepthFirst<String, String> {

   boolean debug;
   String functionName;
   int maxStackSize;
   int maxCalleeArgs;
   Generator gen;

   public Mips(boolean debug) {
      this.debug = debug;
      // initialise everything to zero
      functionName = null;
      maxStackSize = 0;
      maxCalleeArgs = 0;
      gen = new Generator(debug);
   }

   /**
    * f0 -> "MAIN"
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    * f13 -> ( Procedure() )*
    * f14 -> <EOF>
    */
   public String visit(Goal n, String argu) {
      String _ret = null;

      /**
       * Setting global variables of the procedure
       */
      functionName = "main";

      int args = Integer.parseInt(n.f2.f0.tokenImage);
      int stackSpilled = Integer.parseInt(n.f5.f0.tokenImage);
      int calleeArgs = Integer.parseInt(n.f8.f0.tokenImage);
      maxCalleeArgs = Math.max(calleeArgs - 4, 2)+1; // keep two arguments for syscalls

      maxStackSize = (Math.max(0, args - 4) + // for spilled arguments
            stackSpilled + // for spilled registers and saved registers
            maxCalleeArgs + // for calling child functions
            2 + // for storing fp and ra
            2 + // for temporary storage (easy to write code)
            4 + // for storing arguments
            10 // for storing v0 (PRINT)
      ) * 4; // assuming sizeof(int) = sizeof(ptr) = 4 bytes

      /**
       * Prologue
       */

      // 0. Print header
      gen.functionHeader(functionName);

      // 1. store the frame pointer
      gen.store("fp", -8, "sp");

      // 2. change the frame pointer to after the arguments (sp)
      gen.move("fp", "sp");

      // 3. store the return address
      gen.store("ra", -4, "fp");

      // 4. allocate space for the stack
      gen.custom("subu $sp, $sp, " + maxStackSize);

      // 5. store the arguments
      int offset = maxCalleeArgs;
      for (int i = 0; i < Math.max(args-4, 0); i++) {

         // move the argument to v1
         gen.load("v1", i * 4, "fp");

         // store the argument to the stack
         gen.store("v1", (offset + i) * 4, "sp");

      }

      // visit statements
      n.f10.accept(this, argu);


      // Epilogue

      // 1. restore the stack pointer
      gen.custom("addu $sp, $sp, " + maxStackSize);

      // 2. restore the return address
      gen.load("ra", -4, "fp");

      // 3. restore the frame pointer
      gen.load("fp", -8, "fp");

      // 4. return
      gen.custom("j $ra");

      // visit procedures
      n.f13.accept(this, argu);

      // generate the code for system calls
      gen.systemCalls();

      // data segment
      gen.dataSegment();

      return _ret;
   }

   /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
   public String visit(StmtList n, String argu) {
      String _ret = null;
      n.f0.accept(this, "label");
      return _ret;
   }

   /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> "["
    * f5 -> IntegerLiteral()
    * f6 -> "]"
    * f7 -> "["
    * f8 -> IntegerLiteral()
    * f9 -> "]"
    * f10 -> StmtList()
    * f11 -> "END"
    * f12 -> ( SpillInfo() )?
    */
   public String visit(Procedure n, String argu) {
      String _ret = null;

      /**
       * Setting global variables of the procedure
       */
      functionName = n.f0.f0.tokenImage;

      int args = Integer.parseInt(n.f2.f0.tokenImage);
      int stackSpilled = Integer.parseInt(n.f5.f0.tokenImage);
      int calleeArgs = Integer.parseInt(n.f8.f0.tokenImage);
      maxCalleeArgs = Math.max(calleeArgs - 4, 2)+1; // keep two arguments for syscalls

      maxStackSize = (Math.max(0, args - 4) + // for spilled arguments
            stackSpilled + // for spilled registers and saved registers
            maxCalleeArgs + // for calling child functions
            2 +// for storing fp and ra
            2 +// for temporary storage (easy to write code)
            4 +// for storing arguments
            10 // for storing v0 (PRINT)
      ) * 4; // assuming sizeof(int) = sizeof(ptr) = 4 bytes

      /**
       * Prologue
       */

      // 0. Print header
      gen.functionHeader(functionName);

      // 1. store the frame pointer
      gen.store("fp", -8, "sp");

      // 2. change the frame pointer to after the arguments (sp)
      gen.move("fp", "sp");

      // 3. store the return address
      gen.store("ra", -4, "fp");

      // 4. allocate space for the stack
      gen.custom("subu $sp, $sp, " + maxStackSize);

      // 5. store the arguments
      int offset = maxCalleeArgs;
      for (int i = 0; i < Math.max(args-4, 0); i++) {

         // move the argument to v1
         gen.load("v1", i * 4, "fp");

         // store the argument to the stack
         gen.store("v1", (offset + i) * 4, "sp");

      }

      // visit the statements
      n.f10.accept(this, argu);

      // Epilogue

      // 1. restore the stack pointer
      gen.custom("addu $sp, $sp, " + maxStackSize);

      // 2. restore the return address
      gen.load("ra", -4, "fp");

      // 3. restore the frame pointer
      gen.load("fp", -8, "fp");

      // 4. return
      gen.custom("j $ra");

      return _ret;
   }

   /**
    * f0 -> NoOpStmt()
    * | ErrorStmt()
    * | CJumpStmt()
    * | JumpStmt()
    * | HStoreStmt()
    * | HLoadStmt()
    * | MoveStmt()
    * | PrintStmt()
    * | ALoadStmt()
    * | AStoreStmt()
    * | PassArgStmt()
    * | CallStmt()
    */
   public String visit(Stmt n, String argu) {
      String _ret = null;

      // clearing argument
      argu = null;

      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "NOOP"
    */
   public String visit(NoOpStmt n, String argu) {
      String _ret = null;
      gen.custom("nop");
      return _ret;
   }

   /**
    * f0 -> "ERROR"
    */
   public String visit(ErrorStmt n, String argu) {
      String _ret = null;
      gen.error();
      return _ret;
   }

   /**
    * f0 -> "CJUMP"
    * f1 -> Reg()
    * f2 -> Label()
    */
   public String visit(CJumpStmt n, String argu) {
      String _ret = null;

      String reg = n.f1.accept(this, argu);
      String label = n.f2.accept(this, "");

      gen.custom("beqz $" + reg + ", " + label);

      return _ret;
   }

   /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public String visit(JumpStmt n, String argu) {
      String _ret = null;

      String label = n.f1.accept(this, "");
      gen.custom("b " + label);

      return _ret;
   }

   /**
    * f0 -> "HSTORE"
    * f1 -> Reg()
    * f2 -> IntegerLiteral()
    * f3 -> Reg()
    */
   public String visit(HStoreStmt n, String argu) {
      String _ret = null;

      String regPtr = n.f1.accept(this, argu);
      int location = Integer.parseInt(n.f2.f0.tokenImage);
      String regDest = n.f3.accept(this, argu);

      gen.store(regDest, location, regPtr);

      return _ret;
   }

   /**
    * f0 -> "HLOAD"
    * f1 -> Reg()
    * f2 -> Reg()
    * f3 -> IntegerLiteral()
    */
   public String visit(HLoadStmt n, String argu) {
      String _ret = null;

      String reg1 = n.f1.accept(this, argu);
      String reg2 = n.f2.accept(this, argu);
      int location = Integer.parseInt(n.f3.f0.tokenImage);

      gen.load(reg1, location, reg2);

      return _ret;
   }

   /**
    * f0 -> "MOVE"
    * f1 -> Reg()
    * f2 -> Exp()
    */
   public String visit(MoveStmt n, String argu) {
      String _ret = null;

      String reg = n.f1.accept(this, argu);

      // assuming ans is stored in (-12)fp
      n.f2.accept(this, argu);

      gen.comment("Starting move");
      gen.load(reg, -12, "fp");
      gen.comment("Ending move");

      return _ret;
   }

   /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
   public String visit(PrintStmt n, String argu) {
      String _ret = null;

      // assuming ans is stored in v1
      n.f1.accept(this, argu);

      gen.comment("Starting print");
      gen.storeV0();
      // v1 is free now
      gen.storeArguments();
      // a0 to a4 are free now
      gen.load("a0", -12, "fp");
      gen.custom("jal __print");
      // restore everything
      gen.restoreArguments();
      gen.restoreV0();
      gen.comment("Ending print");

      return _ret;
   }

   /**
    * f0 -> "ALOAD"
    * f1 -> Reg()
    * f2 -> SpilledArg()
    */
   public String visit(ALoadStmt n, String argu) {
      String _ret = null;

      String reg = n.f1.accept(this, argu);
      int offset = maxCalleeArgs;
      int spilledNo = Integer.parseInt(n.f2.f1.f0.tokenImage);

      gen.load(reg, (offset + spilledNo) * 4, "sp");

      return _ret;
   }

   /**
    * f0 -> "ASTORE"
    * f1 -> SpilledArg()
    * f2 -> Reg()
    */
   public String visit(AStoreStmt n, String argu) {
      String _ret = null;

      int offset = maxCalleeArgs;
      int spilledNo = Integer.parseInt(n.f1.f1.f0.tokenImage);

      String reg = n.f2.accept(this, argu);

      gen.comment("Astore start");
      gen.store(reg, (offset + spilledNo) * 4, "sp");
      gen.comment("Astore end");

      return _ret;
   }

   /**
    * f0 -> "PASSARG"
    * f1 -> IntegerLiteral()
    * f2 -> Reg()
    */
   public String visit(PassArgStmt n, String argu) {
      String _ret = null;
      n.f0.accept(this, argu);
      
      int argNo = Integer.parseInt(n.f1.f0.tokenImage);
      String reg = n.f2.accept(this, argu);

      gen.store(reg, (argNo-1) * 4, "sp"); // convert to 0 indexing

      return _ret;
   }

   /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    */
   public String visit(CallStmt n, String argu) {
      String _ret = null;
      // expected answer is in v1
      n.f1.accept(this, argu);
      
      gen.comment("Starting call");
      gen.exchangeV1();
      // v1 is free now
      gen.custom("jalr $v1");
      // restore v1
      gen.restoreV1();    
      gen.comment("Ending call");

      return _ret;
   }

   /**
    * f0 -> HAllocate()
    * | BinOp()
    * | SimpleExp()
    */
   public String visit(Exp n, String argu) {
      String _ret = null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
   public String visit(HAllocate n, String argu) {
      String _ret = null;

      n.f1.accept(this, argu);

      gen.comment("Starting hallocate");
      gen.storeV0();
      gen.storeArguments();
      gen.load("a0", -12, "fp");
      gen.custom("jal __halloc");
      gen.store("v0", -12, "fp");
      // restore everything
      gen.restoreArguments();
      gen.restoreV0();
      gen.comment("Ending hallocate");

      return _ret;
   }

   /**
    * f0 -> Operator()
    * f1 -> Reg()
    * f2 -> SimpleExp()
    */
   public String visit(BinOp n, String argu) {
      String _ret = null;

      String op = n.f0.accept(this, argu);
      String reg = n.f1.accept(this, argu);

      // assuming ans is stored in v0
      n.f2.accept(this, argu);

      if(reg == "v1"){
         gen.comment("Starting binop");
         gen.exchangeV1();
         // v1 is free now
         gen.storeV0();
         // v0 is free now
         // get value of v1 from stack to v0
         gen.load("v0", -16, "fp");
         gen.custom(op + " $v1, $v0, $v1");
         gen.restoreV0();
         gen.restoreV1();
         gen.comment("Ending binop");
      }
      else{ 
         gen.comment("Starting binop");
         // store v1
         gen.exchangeV1();
         // v1 is free now
         gen.custom(op + " $v1, $"+ reg + ", $v1");
         gen.restoreV1();
         gen.comment("Ending binop");
      }

      return _ret;
   }

   /**
    * f0 -> "LE"
    * | "NE"
    * | "PLUS"
    * | "MINUS"
    * | "TIMES"
    * | "DIV"
    */
   public String visit(Operator n, String argu) {
      String _ret = null;
      if(n.f0.which==0) return "sle";
      else if(n.f0.which==1) return "sne";
      else if(n.f0.which==2) return "add";
      else if(n.f0.which==3) return "sub";
      else if(n.f0.which==4) return "mul";
      else if(n.f0.which==5) return "div";
      return _ret;
   }

   /**
    * f0 -> "SPILLEDARG"
    * f1 -> IntegerLiteral()
    */
   public String visit(SpilledArg n, String argu) {
      String _ret = null;
      return _ret;
   }

   /**
    * f0 -> Reg()
    * | IntegerLiteral()
    * | Label()
    */
   public String visit(SimpleExp n, String argu) {
      String _ret = null;
      String reg = n.f0.accept(this, "SimpleExp");
      if(n.f0.which == 0){
         // means its a register
         gen.store(reg, -12, "fp");
      }
      return _ret;
   }

   /**
    * f0 -> "a0"
    * | "a1"
    * | "a2"
    * | "a3"
    * | "t0"
    * | "t1"
    * | "t2"
    * | "t3"
    * | "t4"
    * | "t5"
    * | "t6"
    * | "t7"
    * | "s0"
    * | "s1"
    * | "s2"
    * | "s3"
    * | "s4"
    * | "s5"
    * | "s6"
    * | "s7"
    * | "t8"
    * | "t9"
    * | "v0"
    * | "v1"
    */
   public String visit(Reg n, String argu) {
      return ((NodeToken)(n.f0.choice)).tokenImage;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, String argu) {
      String _ret = null;
      if(argu=="SimpleExp"){
         gen.exchangeV1();
         // v1 is free now
         gen.loadi("v1", Integer.parseInt(n.f0.tokenImage));
         gen.restoreV1();
      }

      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Label n, String argu) {

      if (argu == "label") {
         gen.label(n.f0.tokenImage);
      }
      else if(argu == "SimpleExp"){
         gen.exchangeV1();
         // v1 is free now
         gen.custom("la $v1, " + n.f0.tokenImage);
         gen.restoreV1();
      }

      return n.f0.tokenImage;
   }
}
