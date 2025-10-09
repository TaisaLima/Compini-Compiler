package intermediate;
import lexer.*;
import symbol.*;

public class Logical extends Expr{
    public Expr ex1,ex2;
    

    Logical(Token tok, Expr e1, Expr e2) {
        
        super(tok, null); 
        
        
        this.ex1 = e1; 
        this.ex2 = e2; 
        

        this.tipo = check(this.ex1.tipo, this.ex2.tipo);
        if(this.tipo == null) error("type error");
    }

    public Type check(Type p1, Type p2) {
        if ( p1 == Type.Bool && p2 == Type.Bool ) return Type.Bool;
        else return null;
    }
    
    
    public Expr gen() {
        int f = newlabel(); int a = newlabel();
        Temp temp = new Temp(tipo);
        this.jumping(0,f);
        emit(temp.toString() + " = true");
        emit("goto L" + a);
        emitlabel(f); emit(temp.toString() + " = false");
        emitlabel(a);
        return temp;
    }
    
    public String toString() {
        return ex1.toString()+" "+op.toString()+" "+ex2.toString();
    }
}