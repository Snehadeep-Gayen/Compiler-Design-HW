package typecheck;

public class ErrorFunc{

    boolean submission = true;

    public void throwError(String s){
        if(!submission)
            System.out.println(s);
        else
            System.out.print(s);
        System.out.flush();
        System.exit(0);
    }

    public void throwSymbolError(String s){
        if(submission)
            throwError("Symbol not found");
        else
            throwError("Symbol not found "+s);
    }

    public void throwTypeError(){
        throwError("Type error");
    }

    public void throwTypeError(String s){
        if(!submission)
            throwError("Type error"+s);
        else
            throwTypeError();
    }

    public void throwCyclicError(){
        if(!submission)
            throwError("Cyclic Class Decl");
        else
            throwTypeError(""); // TODO
    }

    public void throwMultiDeclError(String s){
        if(!submission)
            throwError("Multiple Decl of "+s);
        else
            throwTypeError(""); // TODO
    }
}
