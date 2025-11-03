package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class If extends Stmt{
    Expr expr;
    Stmt stmt;
    public If(Expr x, Stmt y){
        expr=x;
        stmt = y;
        if(expr.tipo != Type.Bool){
            expr.error("boolean required in if");
        }
    }
    public void gen(int a, int b){
        int label=newlabel();
        expr.jumping(0,b);
        emitlabel(label);
        stmt.gen(label,b);
    }
}
