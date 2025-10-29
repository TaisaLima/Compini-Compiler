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
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) throws IOException {
        if (look == null) error("unexpected end of input, esperado " + (char) t);
        if (look.tag == t) move();
        else error("syntax error: esperado " + (char)t + " / obtido " + look);
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
        while (look != null && look.tag == Tag.BASIC) {
            Type p = type();
            do {
                Token tok = look;
                match(Tag.ID);
                if (look != null && look.tag == '(') {
                    error("Function declarations not implemented yet.");
                }
                Id id = new Id((Word) tok, p, used);
                top.put(tok, id);
                used += p.width;
                if (look != null && look.tag == ',') move();
                else break;
            } while (true);
            match(';');
        }
    }

    Type type() throws IOException {
        Type p = (Type) look;
        match(Tag.BASIC);
        if (look != null && look.tag == '[') return dims(p);
        else return p;
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
        Stmt s1, s2;
        Stmt savedStmt;

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
        
                if (look == null || look.tag != Tag.ELSE) return new If(x, s1); 
                match(Tag.ELSE);
                s2 = stmt();
        
                return new Else(x, s1, s2);
            case Tag.WHILE:
        
                While whilenode = new While(); 
                savedStmt = Stmt.Enclosing;
                Stmt.Enclosing = whilenode;
                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                s1 = stmt();
                whilenode.init(x, s1);
                Stmt.Enclosing = savedStmt;
                return whilenode;
            case Tag.DO:
        
                Do donode = new Do(); 
                savedStmt = Stmt.Enclosing;
                Stmt.Enclosing = donode;
                match(Tag.DO);
                s1 = stmt();
                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                match(';');
                donode.init(s1, x);
                Stmt.Enclosing = savedStmt;
                return donode;
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
                c3e.add("print " + x.toString()); 
        
                return new Print(x); 
            case '{':
                return block();
            default:
                return assign();
        }
    }

    Stmt assign() throws IOException {
        if (look == null) return Stmt.Null;
        Stmt stmt;
        Token t = look;
        match(Tag.ID);
        Id id = top.get(t);
        if (id == null) error(t.toString() + " undeclared");

        if (look != null && look.tag == '=') { // Atribuição simples: id = bool()
            move();
            Expr value = bool();
            // 1️⃣ CORREÇÃO: Removido c3e do construtor
            // 2️⃣ CORREÇÃO: 'Set' agora refere-se a 'parser.rules.Set' via import
            stmt = new Set(id, value); 
            c3e.add(id.toString() + " = " + value.toString()); 
        } else { // Atribuição de array: id[..] = bool()
            Acess x = offset(id);
            match('=');
            Expr value = bool();
    
            stmt = new SetElem(x, value); 
            c3e.add(x.toString() + " = " + value.toString()); 
        }
        match(';');
        return stmt;
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
        if (look != null) {
            switch (look.tag) {
                case '<':
                case Tag.LE:
                case Tag.GE:
                case '>':
                    Token tok = look;
                    move();
                    
                    return new Rel(tok, x, expr());
            }
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
        if (look != null) {
            if (look.tag == '-') {
                move();
                return new Unary(Word.minus, unary()); 
            } else if (look.tag == '!') {
                Token tok = look;
                move();
                return new Not(tok, unary()); 
            } else return factor();
        }
        return null;
    }

    Expr factor() throws IOException {
        if (look == null) return null;
        Expr x = null;
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
            case Tag.ID:
                Id id = top.get(look);
                if (id == null) error(look.toString() + " undeclared");
                move();
                if (look != null && look.tag == '[') return offset(id); 
                else return id; 
            default:
                error("syntax error");
                return x;
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