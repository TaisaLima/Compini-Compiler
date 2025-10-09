
package intermediate; 
import lexer.*; 
import symbol.*; 


public class Print extends Stmt { 
    public Expr expr; 

    public Print(Expr x) {
        // Construtor: armazena a expressão que será impressa
        this.expr = x;
    }

    public void gen(int b, int a) {

        emit("print " + expr.reduce().toString()); 
    }
}