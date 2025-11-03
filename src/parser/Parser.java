package parser;

import java.io.*;
import java.util.*;
import lexer.*;
import symbol.*;
import intermediate.*;
import parser.rules.*;
import parser.rules.Set;

public class Parser {
    private Lexer lex;
    private Token look;
    private Env top = null;
    private int used = 0;
    private List<String> c3e = new ArrayList<>();

    public Parser(Lexer l) throws IOException {
        lex = l;
        move();
    }

    void move() throws IOException {
        look = lex.nextToken();
    }

    void error(String s) {
        throw new Error("Erro próximo à linha " + lex.line + ": " + s);
    }

    void match(int t) throws IOException {
        if (look == null) error("Fim inesperado, esperado " + (char) t);
        if (look.tag == t) move();
        else error("Esperado " + (char) t + ", encontrado " + look);
    }

    public List<String> getC3E() {
        return c3e;
    }

    public Stmt program() throws IOException {
        Env savedEnv = top;
        top = new Env(top);
        decls();
        Stmt s = stmts();
        top = savedEnv;
        return s;
    }

    Stmt block() throws IOException {
        match('{');
        Env savedEnv = top;
        top = new Env(top);
        decls();
        Stmt s = stmts();
        match('}');
        top = savedEnv;
        return s;
    }

    void decls() throws IOException {
        while (look != null && (look.tag == Tag.BASIC || look.tag == Tag.STRING || look.tag == Tag.FUNCTION)) {
            if (look.tag == Tag.FUNCTION) {
                funcDecl();
            } else {
                Type p = type();
                do {
                    Token tok = look;
                    match(Tag.ID);
                    Id id = new Id((Word) tok, p, used);
                    top.put(tok, id);
                    used += p.width;
                    if (look != null && look.tag == ',') move();
                    else break;
                } while (true);
                match(';');
            }
        }
    }

    void funcDecl() throws IOException {
        match(Tag.FUNCTION);
        Token funcName = look;
        match(Tag.ID);
        match('(');
        List<Id> params = new ArrayList<>();
        if (look.tag != ')') {
            do {
                Type p = type();
                Token tok = look;
                match(Tag.ID);
                Id param = new Id((Word) tok, p, used);
                used += p.width;
                params.add(param);
                top.put(tok, param);
                if (look.tag == ',') move();
                else break;
            } while (true);
        }
        match(')');
        Stmt body = block();
        c3e.add("FUNCTION " + funcName.toString() + " BEGIN");
        body.gen(0, 0);
        c3e.add("FUNCTION " + funcName.toString() + " END");
    }

    Type type() throws IOException {
        if (look.tag == Tag.STRING) {
            move();
            return Type.String;
        } else {
            Type p = (Type) look;
            match(Tag.BASIC);
            if (look != null && look.tag == '[') return dims(p);
            else return p;
        }
    }

    Type dims(Type p) throws IOException {
        match('[');
        Token tok = look;
        match(Tag.NUM);
        match(']');
        if (look != null && look.tag == '[') {
            p = dims(p);
        }
        return new Array(((Num) tok).valor, p);
    }

    Stmt stmts() throws IOException {
        if (look == null || look.tag == '}') return Stmt.Null;
        return new Seq(stmt(), stmts());
    }

    Stmt stmt() throws IOException {
        if (look == null) return Stmt.Null;
        Expr x;
        Stmt s1, s2, savedStmt;

        switch (look.tag) {
            case ';':
                move();
                return Stmt.Null;

            case Tag.IF:
                match(Tag.IF);
                match('(');
                x = bool();
                match(')');
                s1 = stmt();
                if (look != null && look.tag == Tag.ELSE) {
                    match(Tag.ELSE);
                    s2 = stmt();
                    return new Else(x, s1, s2);
                }
                return new If(x, s1);

            case Tag.WHILE:
                While wh = new While();
                savedStmt = Stmt.Enclosing;
                Stmt.Enclosing = wh;
                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                s1 = stmt();
                wh.init(x, s1);
                Stmt.Enclosing = savedStmt;
                return wh;

            case Tag.DO:
                Do d = new Do();
                savedStmt = Stmt.Enclosing;
                Stmt.Enclosing = d;
                match(Tag.DO);
                s1 = stmt();
                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                match(';');
                d.init(s1, x);
                Stmt.Enclosing = savedStmt;
                return d;

            case Tag.BREAK:
                match(Tag.BREAK);
                match(';');
                c3e.add("break;");
                return new Break();

            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                x = expr();
                match(')');
                match(';');
                c3e.add("print " + x);
                return new Print(x);

            case Tag.SEQ:
                match(Tag.SEQ);
                match('{');
                Stmt seqBlock = stmts();
                match('}');
                return new SeqBlock(Collections.singletonList(seqBlock));

            case Tag.PAR:
                match(Tag.PAR);
                match('{');
                List<Stmt> threads = new ArrayList<>();
                while (look != null && look.tag != '}') {
                    threads.add(stmt());
                }
                match('}');
                return new ParBlock(threads, c3e);

            case Tag.SEND:
                match(Tag.SEND);
                match('(');
                Id canal = top.get(look);
                match(Tag.ID);
                match(',');
                Expr msg = expr();
                match(')');
                match(';');
                c3e.add("send " + ((Word)msg.op).lexema);
                return new SendStmt(((Word)canal.op).lexema, c3e.toArray(new String[0]));


            case Tag.RECEIVE:
                match(Tag.RECEIVE);
                match('(');
                Id canalRecv = top.get(look);
                match(Tag.ID);
                match(',');
                Id var = top.get(look);
                match(Tag.ID);
                match(')');
                match(';');
               
                return new ReceiveStmt(
                    ((Word)canalRecv.op).lexema, 
                    ((Word)var.op).lexema
                );

            
            case Tag.RETURN:
                match(Tag.RETURN);
                Expr retExpr = expr();
                match(';');
                c3e.add("return " + retExpr);
                return new ReturnStmt(retExpr);

            case '{':
                return block();

            default:
                return assign();
        }
    }

    Stmt assign() throws IOException {
        Token t = look;
        match(Tag.ID);
        Id id = top.get(t);
        if (id == null) error(t.toString() + " não declarado");

        if (look.tag == '=') {
            move();
            Expr value = bool();
            c3e.add(id.toString() + " = " + value.toString());
            match(';');
            return new Set(id, value);
        } else {
            Acess x = offset(id);
            match('=');
            Expr value = bool();
            match(';');
            c3e.add(x.toString() + " = " + value.toString());
            return new SetElem(x, value);
        }
    }

    Expr bool() throws IOException {
        Expr x = join();
        while (look != null && look.tag == Tag.OR) {
            Token tok = look;
            move();
            x = new Or(tok, x, join());
        }
        return x;
    }

    Expr join() throws IOException {
        Expr x = equality();
        while (look != null && look.tag == Tag.AND) {
            Token tok = look;
            move();
            x = new And(tok, x, equality());
        }
        return x;
    }

    Expr equality() throws IOException {
        Expr x = rel();
        while (look != null && (look.tag == Tag.EQ || look.tag == Tag.NE)) {
            Token tok = look;
            move();
            x = new Rel(tok, x, rel());
        }
        return x;
    }

    Expr rel() throws IOException {
        Expr x = expr();
        if (look != null && (look.tag == '<' || look.tag == Tag.LE || look.tag == Tag.GE || look.tag == '>')) {
            Token tok = look;
            move();
            return new Rel(tok, x, expr());
        }
        return x;
    }

    Expr expr() throws IOException {
        Expr x = term();
        while (look != null && (look.tag == '+' || look.tag == '-')) {
            Token tok = look;
            move();
            x = new Arith(tok, x, term());
        }
        return x;
    }

    Expr term() throws IOException {
        Expr x = unary();
        while (look != null && (look.tag == '*' || look.tag == '/')) {
            Token tok = look;
            move();
            x = new Arith(tok, x, unary());
        }
        return x;
    }

    Expr unary() throws IOException {
        if (look.tag == '-') {
            move();
            return new Unary(Word.minus, unary());
        } else if (look.tag == '!') {
            Token tok = look;
            move();
            return new Not(tok, unary());
        } else return factor();
    }

    Expr factor() throws IOException {
        Expr x;
        switch (look.tag) {
            case '(':
                move();
                x = bool();
                match(')');
                return x;
            case Tag.NUM:
                x = new Constant(look, Type.Int);
                move();
                return x;
            case Tag.REAL:
                x = new Constant(look, Type.Float);
                move();
                return x;
            case Tag.TRUE:
                x = Constant.True;
                move();
                return x;
            case Tag.FALSE:
                x = Constant.False;
                move();
                return x;
            case Tag.STRING:
                x = new StringExpr((Word) look,Type.String);
                move();
                return x;
            case Tag.ID:
                Id id = top.get(look);
                if (id == null) error(look.toString() + " não declarado");
                move();
                if (look != null && look.tag == '[') return offset(id);
                return id;
            default:
                error("Erro de sintaxe");
                return null;
        }
    }

    Acess offset(Id a) throws IOException {
        Expr i, w, t1, t2, loc;
        Type type = a.tipo;
        match('[');
        i = bool();
        match(']');
        type = ((Array) type).of;
        w = new Constant(type.width);
        t1 = new Arith(new Token('*'), i, w);
        loc = t1;
        while (look != null && look.tag == '[') {
            match('[');
            i = bool();
            match(']');
            type = ((Array) type).of;
            w = new Constant(type.width);
            t1 = new Arith(new Token('*'), i, w);
            t2 = new Arith(new Token('+'), loc, t1);
            loc = t2;
        }
        return new Acess(a, loc, type);
    }
}
