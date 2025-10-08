package interm;
import lexer.*;
import symbol.*;

public class Logical extends Expr{
    public Expr ex1,ex2;
    Logical(Token tok, Expr ex1, Expr ex2) {
        super(tok, null);
        ex1=ex1;
        ex2=ex2;
        tipo=check(ex1.tipo,ex2.tipo);
        if(tipo==null) error("type error");
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
