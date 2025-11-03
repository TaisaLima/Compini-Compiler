
package parser.rules; 


import lexer.*;
import intermediate.*;
import symbol.*;

public class Print extends Stmt { 
    public Expr expr; 

    public Print(Expr x) {
        this.expr = x;
    }

    public void gen(int b, int a) {

        emit("print " + expr.reduce().toString()); 
    }
}