package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class And extends Logical {
    public And(Token tok, Expr x1, Expr x2) {
        super(tok, x1, x2);
    }
    public void jumping(int t, int f) {
        int label = f != 0 ? f : newlabel();
        ex1.jumping(0, label);
        ex2.jumping(t,f);
        if( f == 0 ) emitlabel(label);
    }
}
