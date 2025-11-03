package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class While extends Stmt {
    Expr expr; Stmt stmt;
    public While() { expr = null; stmt = null; }
    public void init(Expr x, Stmt s) {
        expr = x; stmt = s;
        if( expr.tipo != Type.Bool ) expr.error("boolean required in while");
    }
    public void gen(int a, int b) {
        after = b;
        expr.jumping(0, b);
        int label = newlabel();
        emitlabel(label); stmt.gen(label, a);
        emit("goto L" + a);
    }
}
