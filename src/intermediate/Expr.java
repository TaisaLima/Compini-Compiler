package intermediate;

import lexer.*;
import symbol.*;
import parser.rules.*;


public class Expr extends Node {
    public Token op;
    public Type tipo;

    
    public Expr(Token tok, Type z) {
        this.op = tok;
        this.tipo = z;
    }

   
    public Expr gen() {
        return this;
    }

    // Redução de expressão (padrão: retorna a própria expressão)
    public Expr reduce() {
        return this;
    }

    // Método para saltos condicionais
    public void jumping(int t, int f) {
        emitjumps(toString(), t, f);
    }

    // Emite código para saltos condicional e incondicional
    public void emitjumps(String test, int t, int f) {
        if (t != 0 && f != 0) {
            emit("if " + test + " goto L" + t);
            emit("goto L" + f);
        } else if (t != 0) {
            emit("if " + test + " goto L" + t);
        } else if (f != 0) {
            emit("iffalse " + test + " goto L" + f);
        }
    }

    @Override
    public String toString() {
        return op.toString();
    }
}
