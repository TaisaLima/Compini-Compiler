package parser.rules;


import lexer.*;
import intermediate.*;
import symbol.*;

public class Or extends Logical {
    public Or(Token tok, Expr x1, Expr x2) {
        super(tok, x1, x2);
    }
    public void jumping(int t, int f) {
        int label = t != 0 ? t : newlabel();
        ex1.jumping(label, 0);
        ex2.jumping(t,f);
        if( t == 0 ) emitlabel(label);
    }
}
