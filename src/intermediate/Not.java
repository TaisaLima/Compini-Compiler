package intermediate; // Arquivo Not.java
import lexer.*;
import symbol.*;

public class Not extends Logical {
    public Not(Token tok, Expr x2) {
        super(tok, x2, x2);
    }
    public void jumping(int t, int f) { ex2.jumping(f, t); }
    public String toString() { return op.toString()+" "+ex2.toString(); }
}