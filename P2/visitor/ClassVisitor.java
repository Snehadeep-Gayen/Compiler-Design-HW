package visitor;
import syntaxtree.*;
import java.util.*;
import typecheck.*;

public class ClassVisitor extends GJDepthFirst<String,String> {

   VarBlock vEnv;
   MethodBlock mEnv;
   Forest f;
   MakeGraph g;
   ArrayList<String> methodParamType;
   boolean debug;
   boolean printMethodBlock;

   public ClassVisitor(Forest _f, MakeGraph _g){
       f = _f;
       g = _g;
       vEnv = new VarBlock(null);
       mEnv = new MethodBlock(null, g);
       debug = false;
       printMethodBlock = true;
       methodParamType = null;
   }

   public String visit(NodeList n, String argu) {
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeListOptional n, String argu) {
      if ( n.present() ) {
         String _ret=null;
         int _count=0;
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
         }
         return _ret;
      }
      else
         return null;
   }

   public String visit(NodeOptional n, String argu) {
      if ( n.present() )
         return n.node.accept(this,argu);
      else
         return null;
   }

   public String visit(NodeSequence n, String argu) {
      String _ret=null;
      int _count=0;
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this,argu);
         _count++;
      }
      return _ret;
   }

   public String visit(NodeToken n, String argu) { return null; }

   public void removeCurrentScope(){
        vEnv = vEnv.prev;
        mEnv = mEnv.prev;
   }

   public void removeVarCurrentScope(){
        vEnv = vEnv.prev;
   }

   public void printBlocks(){
        if(debug){
            vEnv.print();
            mEnv.print();
        }
   }

   private boolean isValidType(String typeName){
      if(typeName==null)
         return false;
      if(typeName.equals("int") || typeName.equals("int[]") || typeName.equals("boolean"))
         return true;
      if(g.isValidClass(typeName))
         return true;
      return false;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> PrintStatement()
    * f15 -> "}"
    * f16 -> "}"
    */
   public String visit(MainClass n, String argu) {
      n.f0.accept(this, null);
      String type = n.f1.accept(this, null);
      argu = type;
      n.f2.accept(this, argu);
      vEnv = new VarBlock(vEnv);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      n.f10.accept(this, argu);
      String argsName = n.f11.accept(this, argu);
      // vEnv.put(argsName, "String[]"); // CHECK
      n.f12.accept(this, argu);
      n.f13.accept(this, argu);
      n.f14.accept(this, argu);
      n.f15.accept(this, argu);
      removeVarCurrentScope();
      n.f16.accept(this, argu);
      return null;
   }

/**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public String visit(ClassDeclaration n, String __arg) {
      String _ret=null;
      vEnv = new VarBlock(vEnv);
      mEnv = new MethodBlock(mEnv, g);
      n.f0.accept(this, null);
      n.f1.accept(this, null);
      String argu = n.f1.f0.tokenImage;
      ArrayList<Method> lMethods = g.getMethodList(n.f1.f0.tokenImage);
      for(Method m : lMethods)
          mEnv.put(m);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public String visit(ClassExtendsDeclaration n, String __argu) {
      String _ret=null;
      vEnv = new VarBlock(vEnv);
      mEnv = new MethodBlock(mEnv, g);
      n.f0.accept(this, null);
      n.f1.accept(this, null);
      String argu = n.f1.f0.tokenImage;
      ArrayList<Method> lMethods = g.getMethodList(n.f1.f0.tokenImage);
      for(Method m : lMethods)
          mEnv.put(m);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      // remove currentScope
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, String argu) {
      String _ret=null;
      String type = n.f0.accept(this, argu);
      if(!isValidType(type))
        (new ErrorFunc()).throwSymbolError("Variable type incorrect "+type);
      String val = n.f1.accept(this, argu);
      vEnv.put(val, type);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public String visit(MethodDeclaration n, String argu) {
      // ASSUMPTION: Add function arguments to the stack
      vEnv = new VarBlock(vEnv);
      n.f0.accept(this, argu);
      String retType = n.f1.accept(this, argu);
      if(!isValidType(retType))
        (new ErrorFunc()).throwSymbolError("Return type"+retType+" of function not found");
      String methodName = n.f2.accept(this, argu);
      // check parameters also here
      Method currentMethod = g.getMethod(argu, methodName);
      for(int i=0; i<currentMethod.argType.size(); i++){
        String typeOfArg = currentMethod.argType.get(i);
         if(!isValidType(typeOfArg))
           (new ErrorFunc()).throwSymbolError("Argument type"+typeOfArg+" of function not found");
      }
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      if(debug){
          System.out.println("Starting variable decl block of "+methodName);
          System.out.flush();
      }
      n.f7.accept(this, argu);
      if(debug){
          System.out.println("Starting statement block of "+methodName);
          System.out.flush();
      }
      n.f8.accept(this, argu);
      n.f9.accept(this, argu);
      String type = n.f10.accept(this, argu);
      if(debug){
          System.out.println("Got return expression of type "+type);
          System.out.flush();
      }
      if(type==retType)
          ;
      else if(g.isValidClass(type) && g.isValidClass(retType) && g.isParent(retType, type))
          ;
      else
          (new ErrorFunc()).throwTypeError("Returned "+type+" from function having return type "+retType);
      n.f11.accept(this, argu);
      n.f12.accept(this, argu);
      if(debug && printMethodBlock){
          vEnv.print();
      }
      removeVarCurrentScope();
      return null;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterStringest() )*
    */
   public String visit(FormalParameterList n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n, String argu) {
      String type = n.f0.accept(this, argu);
      String name = n.f1.accept(this, argu);
      if(!isValidType(type))
          (new ErrorFunc()).throwSymbolError("Argument type"+type+" of function not found");
      if(debug)
          System.out.println("Putting argument "+name+" if type "+type);
      vEnv.put(name, type);
      return null;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public String visit(FormalParameterRest n, String argu) {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return null;
   }

   /**
    * f0 -> StringrrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public String visit(Type n, String argu) {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(ArrayType n, String argu) {
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return "int[]"; 
   }

   /**
    * f0 -> "boolean"
    */
   public String visit(BooleanType n, String argu) {
      n.f0.accept(this, argu);
      return "boolean";
   }

   /**
    * f0 -> "int"
    */
   public String visit(IntegerType n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      return "int";
   }

   /**
    * f0 -> Block()
    *       | StringssignmentStatement()
    *       | StringrrayStringssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | DoStatement()
    *       | PrintStatement()
    */
   public String visit(Statement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public String visit(Block n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public String visit(AssignmentStatement n, String argu) {
      String _ret=null;
      String type1 = n.f0.accept(this, argu);
      String type1a = vEnv.get(type1);
      if(type1a==null)
          (new ErrorFunc()).throwSymbolError("Variable not found");
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type2!="int" && type2!="int[]" && type2!="boolean" && !g.isValidClass(type2))
          type2 = vEnv.get(type2); // TODO: this is wrong, ideally this shouldn't execute
      n.f3.accept(this, argu);
      if(type1a==type2)
          ;
      else if(g.isValidClass(type2) && g.isValidClass(type1a) && g.isParent(type1a, type2))
          ;
      else
          (new ErrorFunc()).throwTypeError("Assigning "+type1a+" to "+type2);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n, String argu) {
      String _ret=null;
      String varname = n.f0.accept(this, argu);
      String type = vEnv.get(varname);
	  if(type==null)
          (new ErrorFunc()).throwSymbolError("Identifier not found "+varname);
      if(type!="int[]")
          (new ErrorFunc()).throwTypeError("Exepcted int[] but got "+type);
      n.f1.accept(this, argu);
      String type1 = n.f2.accept(this, argu);
      if(type1!="int")
          (new ErrorFunc()).throwTypeError("Expected int but got "+type1);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      String type2 = n.f5.accept(this, argu);
      if(type2!="int")
          (new ErrorFunc()).throwTypeError("Expected int but got "+type2);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> IfthenElseStatement()
    *       | IfthenStatement()
    */
   public String visit(IfStatement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(IfthenStatement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type = n.f2.accept(this, argu);
      if(type!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean got "+type);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfthenElseStatement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type = n.f2.accept(this, argu);
      if(type!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean got "+type);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public String visit(WhileStatement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type = n.f2.accept(this, argu);
      if(type!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean got "+type);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "do"
    * f1 -> Statement()
    * f2 -> "while"
    * f3 -> "("
    * f4 -> Expression()
    * f5 -> ")"
    * f6 -> ";"
    */
   public String visit(DoStatement n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      String type = n.f4.accept(this, argu);
      if(type!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean got "+type);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n, String argu) {
      String _ret=null;
      // TODO: CHECK THIS
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type = n.f2.accept(this, argu);
      if(type != "int")
         (new ErrorFunc()).throwTypeError("Print statement should be of type int");
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> OrExpression()
    *       | StringndExpression()
    *       | CompareExpression()
    *       | neqExpression()
    *       | StringddExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | DivExpression()
    *       | StringrrayLookup()
    *       | StringrrayLength()
    *       | MessageSend()
    *       | PrimaryExpression()
    */
   public String visit(Expression n, String argu) {
       return n.f0.accept(this, argu);
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
   public String visit(AndExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="boolean"||type2!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean but got "+type1+" "+type2);
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "||"
    * f2 -> PrimaryExpression()
    */
   public String visit(OrExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="boolean"||type2!="boolean")
          (new ErrorFunc()).throwTypeError("Expected boolean got "+type1+" and "+type2);
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<="
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="int"||type2!="int")
          (new ErrorFunc()).throwTypeError("Expected <= int but got "+type1+" "+type2);
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "!="
    * f2 -> PrimaryExpression()
    */
   public String visit(neqExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1==type2)
          return "boolean";
      if(g.isValidClass(type1) && g.isValidClass(type2) &&
          (g.isParent(type1, type2) || g.isParent(type2, type1)))
          return "boolean";

      (new ErrorFunc()).throwTypeError(" in != Expected int but got "+type1+" "+type2);
      return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(AddExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="int"||type2!="int")
          (new ErrorFunc()).throwTypeError(" + Expected int but got "+type1+" "+type2);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="int" || type2!="int")
          (new ErrorFunc()).throwTypeError(" - Expected int but got "+type1+" "+type2);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="int" || type2!="int")
          (new ErrorFunc()).throwTypeError(" * Expected int but got "+type1+" "+type2);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "/"
    * f2 -> PrimaryExpression()
    */
   public String visit(DivExpression n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      if(type1!="int" || type2!="int")
          (new ErrorFunc()).throwTypeError(" / Expected int but got "+type1+" "+type2);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String type2 = n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      if(type1!="int[]" || type2!="int")
          (new ErrorFunc()).throwTypeError("Expected int[] and int "+type1+" "+type2);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n, String argu) {
      String type1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      if(type1!="int[]") 
          (new ErrorFunc()).throwTypeError("Expected int[] or String[] but got "+type1);
      return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public String visit(MessageSend n, String argu) {
      String _ret=null;
      String classType = n.f0.accept(this, argu);
      if(!g.isValidClass(classType))
        (new ErrorFunc()).throwSymbolError("Expected class type but found "+classType);
      n.f1.accept(this, argu);
      String calledMethod = n.f2.accept(this, argu);
      // check if function is present
      // Method actualMethod = mEnv.get(calledMethod); this is wrong
      if(debug)
          System.out.println("Trying to get method "+calledMethod+" from class "+classType);
      Method actualMethod = g.getMethod(classType, calledMethod);
      if(actualMethod==null)
          (new ErrorFunc()).throwSymbolError("Method not found"+calledMethod);
      // TODO: handle multiple here
      ArrayList<String> prevList = methodParamType;
      methodParamType = new ArrayList<String>();
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      if(debug){
            System.out.println("Expected "+actualMethod.argType.size()+" but got "+methodParamType.size()+" arguments");
      }
      if(actualMethod.argType.size()!=methodParamType.size())
        (new ErrorFunc()).throwTypeError("Function not found "+actualMethod.name+" with params "+methodParamType);
      for(int i=0; i<actualMethod.argType.size(); i++){
          if(debug){
              System.out.println("MethodArgs\t\tCalledArgs");
              System.out.println(actualMethod.argType.get(i)+" "+methodParamType.get(i));
          }
          String arg1 = actualMethod.argType.get(i);
          String arg2 = methodParamType.get(i);
          if(arg1==arg2)
              continue;
          if(g.isValidClass(arg1) && g.isValidClass(arg2) && g.isParent(arg1, arg2))
              continue;
          (new ErrorFunc()).throwTypeError("Argument "+i+" type mismatch");
      }
      methodParamType = prevList;
      // TODO: check if return type is in scope
      if(debug){
          System.out.println("Returning "+actualMethod.returnType);
      }
      return actualMethod.returnType; // TODO: CHANGE THIS
   }

   /**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
   public String visit(ExpressionList n, String argu) {
      String _ret=null;
      String type = (n.f0.accept(this, argu));
      if(debug){
          System.out.println("Adding "+type+" to methodParamType arraylist");
      }
      methodParamType.add(type);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public String visit(ExpressionRest n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      methodParamType.add(n.f1.accept(this, argu));
      return _ret;
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
   public String visit(PrimaryExpression n, String argu) {
      String retType = n.f0.accept(this, argu);
      if(debug)
          System.out.println("Returning "+retType+" from primary exp");
      if(n.f0.which!=3)
          return retType;
      String retType2 = vEnv.get(retType);
      if(retType2==null){
        // KUMS DOUBT
      //   if(g.isValidClass(retType2)){
      //       (new ErrorFunc()).throwTypeError("Kums error Mail: Sep 23, 2023, 6:20:37AM");
      //   }
        (new ErrorFunc()).throwSymbolError("Can't find "+retType);
      }
      if(debug)
          System.out.println("Returning "+retType2+" from primary exp");
      return retType2;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public String visit(IntegerLiteral n, String argu) {
      n.f0.accept(this, argu);
      return "int";
   }

   /**
    * f0 -> "true"
    */
   public String visit(TrueLiteral n, String argu) {
      n.f0.accept(this, argu);
      return "boolean";
   }

   /**
    * f0 -> "false"
    */
   public String visit(FalseLiteral n, String argu) {
      n.f0.accept(this, argu);
      return "boolean";
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n, String argu) {
      n.f0.accept(this, argu);
      return n.f0.tokenImage; 
   }

   /**
    * f0 -> "this"
    */
   public String visit(ThisExpression n, String argu) {
      n.f0.accept(this, argu);
      return argu;  // this must be the class name
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      String type = n.f3.accept(this, argu);
      if(type!="int")
          (new ErrorFunc()).throwTypeError("Expected integer in array length but got "+type);
      // ERROR: ensure expression is int
      n.f4.accept(this, argu);
      return "int[]";
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public String visit(AllocationExpression n, String argu) {
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);
      if(!g.isValidClass(type))
          (new ErrorFunc()).throwSymbolError(type+"is not a valid classname");
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      if(debug)
          System.out.println("Returning new object of type " + type);
      return type;
   }

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
   public String visit(NotExpression n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);
      if(type!="boolean")
          (new ErrorFunc()).throwTypeError();
      return "boolean";
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, String argu) {
      String _ret=null;
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return type;
   }
}
