package main.codegen;

import main.ast.expr.ExprNode;
import main.semantic.types.Type;
import main.lexer.Word;

public class Label extends ExprNode {
    private static int count = 0;
    
    public Label(Type p) {
        super();
        this.type = p;
    }
    
    public static int newLabel() {
        return ++count;
    }
    
    @Override
    public String toString() {
        return "t" + count;
    }
}