package parser.rules;


import lexer.*;
import intermediate.*;
import symbol.*;

public class Do extends Stmt{
    Expr expr; Stmt stmt;
    public Do() { expr = null; stmt = null; }
    public void init(Stmt s, Expr x) {
        expr = x; stmt = s;
        if( expr.tipo != Type.Bool ) expr.error("boolean required in do");
    }
    public void gen(int a, int b) {
        after = b;
        int label = newlabel();
        stmt.gen(a,label);
        emitlabel(label);
        expr.jumping(a,0);
    }
}
