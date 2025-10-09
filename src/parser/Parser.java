package parser; 

import java.io.*;
import lexer.*;
import symbol.*;
import intermediate.*; 

public class Parser {
    private Lexer lex; 
    private Token look; 
    Env top = null; 
    int used = 0; // deslocamento de memória

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
    if (look.tag == t) move();
    else error("syntax error: esperado " + (char)t + " / obtido " + look);
    }


    public void program() throws IOException {
       
        Env savedEnv = top;
        top = new Env(top);

        decls();
        
        Stmt s = stmts();
        
        if (s != Stmt.Null) {
            int begin = s.newlabel(); 
            int after = s.newlabel();
            s.emitlabel(begin); 
            s.gen(begin, after); 
            s.emitlabel(after);
        }

        top = savedEnv;
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

    // *** REVISÃO CRÍTICA: Lidar com Múltiplas Declarações e Funções ***
    void decls() throws IOException {
        while (look.tag == Tag.BASIC) { 
            Type p = type(); // Processa o tipo (int, float, etc.)
            
            // Loop para processar múltiplos IDs separados por vírgula (se aplicável)
            do {
                Token tok = look;
                match(Tag.ID); 
                
            
                if (look.tag == '(') {
                   
                    error("Function declaration parsing not implemented yet. (e.g. test3_valid.txt)");
              
                }

               
                Id id = new Id((Word) tok, p, used); 
                top.put(tok, id);
                used = used + p.width;
                
               
                if (look.tag == ',') {
                    move(); 
                } else {
                    break; 
                }

            } while (true); // Loop para IDs separados por vírgula
            
            // Espera o ponto e vírgula final para a(s) declaração(ões)
            match(';');
        }
    }

    Type type() throws IOException { // Type existe no pacote 'symbol'
        Type p = (Type) look; // Type existe no pacote 'symbol'
        match(Tag.BASIC);
        if (look.tag != '[') return p; // T -> basic
        else return dims(p); // retorna tipo do arranjo
    }

    Type dims(Type p) throws IOException {
        match('[');
        Token tok = look;
        match(Tag.NUM); // Tag.NUM existe no pacote 'lexer'
        match(']');
        if (look.tag == '[') {
            p = dims(p);
        }
        return new Array(((Num) tok).valor, p); // Array existe no pacote 'symbol', Num no 'lexer'
    }

    Stmt stmts() throws IOException {
        // Se 'look' for nulo (fim de arquivo inesperado) ou se for '}', encerra.
        if (look == null || look.tag == '}') { 
             return Stmt.Null; // FIM da sequência de comandos
        }
        
        // **IMPORTANTE:** Se seu Lexer usa uma Tag alta para EOF (ex: 270)
        // e retorna um token não-nulo, use a Tag aqui:
        // if (look.tag == '}' || look.tag == Tag.EOF) { ...

        else return new Seq(stmt(), stmts()); 
    }

    Stmt stmt() throws IOException {
        Expr x; 
        Stmt s, s1, s2; 
        Stmt savedStmt; 

        switch (look.tag) {
            case ';':
                move();
                return Stmt.Null;
            case Tag.IF:
                match(Tag.IF);
                match('('); // Correção de sintaxe para IF
                x = bool();
                match(')');
                s1 = stmt();
                if (look.tag != Tag.ELSE) return new If(x, s1); 
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
                return new Break(); 

            case Tag.PRINT:
                Expr printExpr; 

                match(Tag.PRINT); 
                match('('); 
                    
               
                printExpr = expr(); 
                    
                match(')');
                match(';'); 
                
                // Cria e retorna o nó Print (necessita da classe Print no pacote intermediate)
                return new Print(printExpr); 
            case '{':
                return block();
            default:
                return assign();
        }
    }

    Stmt assign() throws IOException {
        Stmt stmt;
        Token t = look;
        match(Tag.ID);
        Id id = top.get(t); 
        if (id == null) error(t.toString() + " undeclared");
        if (look.tag == '=') { 
            move();
            stmt = new Set(id, bool());
        } else { 
            Acess x = offset(id); 
            match('=');
            stmt = new SetElem(x, bool()); 
        }
        match(';');
        return stmt;
    }

    Expr bool() throws IOException {
        Expr x = join();
        while (look.tag == Tag.OR) { 
            Token tok = look;
            move();
            x = new Or(tok, x, join()); 
        }
        return x;
    }

    Expr join() throws IOException {
        Expr x = equality();
        while (look.tag == Tag.AND) { 
            Token tok = look;
            move();
            x = new And(tok, x, equality()); 
        }
        return x;
    }

    Expr equality() throws IOException {
        Expr x = rel();
        while (look.tag == Tag.EQ || look.tag == Tag.NE) { 
            Token tok = look;
            move();
            x = new Rel(tok, x, rel()); 
        }
        return x;
    }

    Expr rel() throws IOException {
        Expr x = expr();
        switch (look.tag) {
            case '<':
            case Tag.LE: 
            case Tag.GE: 
            case '>':
                Token tok = look;
                move();
                return new Rel(tok, x, expr());
            default:
                return x;
        }
    }

    Expr expr() throws IOException {
        Expr x = term();
        while (look.tag == '+' || look.tag == '-') {
            Token tok = look;
            move();
            x = new Arith(tok, x, term()); 
        }
        return x;
    }

    Expr term() throws IOException {
        Expr x = unary();
        while (look.tag == '*' || look.tag == '/') {
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
                String s = look.toString();
                Id id = top.get(look);
                if (id == null) error(look.toString() + " undeclared");
                move();
                if (look.tag != '[') return id;
                else return offset(id);
            default:
                error("syntax error");
                return x;
        }
    }

    Acess offset(Id a) throws IOException { 
        Expr i;
        Expr w;
        Expr t1, t2;
        Expr loc;
        Type type = a.tipo; 
        match('[');
        i = bool();
        match(']'); 
        type = ((Array) type).of; 
        w = new Constant(type.width); 
        t1 = new Arith(new Token('*'), i, w); 
        loc = t1;
        while (look.tag == '[') { 
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