package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class Logical extends Expr {
    public Expr ex1, ex2;

    // Construtor
    public Logical(Token tok, Expr e1, Expr e2) {
        super(tok, null); // Tipo inicial null
        this.op = tok;    
        this.ex1 = e1;
        this.ex2 = e2;

        // Verifica tipos
        this.tipo = check(this.ex1.tipo, this.ex2.tipo);
        if (this.tipo == null) error("type error");
    }

    // Verifica se os tipos são compatíveis (boolean)
    public Type check(Type p1, Type p2) {
        if (p1 == Type.Bool && p2 == Type.Bool) return Type.Bool;
        return null;
    }

    // Geração de código intermediário
    @Override
    public Expr gen() {
        int f = newlabel(); // label falso
        int a = newlabel(); // label após
        Temp temp = new Temp(tipo); // variável temporária

        // Avalia ex1 e ex2 com saltos condicionais
        this.jumping(0, f);          

        emit(temp.toString() + " = true");
        emit("goto L" + a);
        emitlabel(f);
        emit(temp.toString() + " = false");
        emitlabel(a);

        return temp; // Temp é um Expr, então funciona
    }

    // Representação textual
    @Override
    public String toString() {
        return ex1.toString() + " " + op.toString() + " " + ex2.toString();
    }
}
