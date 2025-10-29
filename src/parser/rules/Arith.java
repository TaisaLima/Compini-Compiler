package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class Arith extends Op{
    public Expr expr1,expr2;
    public Arith(Token tok, Expr x1, Expr x2) {
        super(tok, null);
        expr1 = x1; expr2 = x2;
        tipo = Type.max(expr1.tipo, expr2.tipo);
        if (tipo == null ){
            error("type error");
        }
    }
    public Expr gen() {
        return new Arith(op, expr1.reduce(), expr2.reduce());
    }

    @Override
    public String toString() {
        return expr1.toString()+" "+op.toString()+" "+expr2.toString();
    }
}
