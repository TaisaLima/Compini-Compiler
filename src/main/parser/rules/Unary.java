package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class Unary extends Op{
    public Expr expr;

    public Unary(Token tok, Expr x) {
        super(tok, null);
        expr= x;
        tipo = Type.max(Type.Int, expr.tipo);
        if(tipo==null) error ("type error");
    }
    public Expr gen(){
        return new Unary(op,expr.reduce());
    }

    @Override
    public String toString() {
        return op.toString()+" "+expr.toString();
    }
}