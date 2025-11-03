package main.ast.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Nó monolítico para todas as expressões na AST.
 * Nota: Em um compilador de produção, seria melhor usar uma hierarquia de classes (herança).
 * Aqui, usamos um único enum Type e Factory Methods para simplificar.
 */
public class ExprNode {
    
    public enum Type {
        LITERAL,    // Ex: 10, 3.14, true, "string"
        VARIABLE,   // Ex: x
        ACCESS,     // Ex: arr[i]
        UNARY,      // Ex: -x, !b
        ARITH,      // Aritmética: +, -, *, /
        REL,        // Relacional: <, >, ==, !=, <=, >=
        LOGIC       // Lógica: AND, OR
    }

    public final Type type;
    public final String name;      // Usado para VARIABLE e ACCESS (nome da variável/array)
    public final String operator;  // Usado para UNARY, ARITH, REL, LOGIC
    public final Object value;     // Usado para LITERAL (o valor real: Integer, Float, Boolean, String)
    public final List<ExprNode> children; // Sub-expressões (operandos, índices)

    // Construtor Privado: Força o uso dos Factory Methods
    private ExprNode(Type type, Object value, String name, String operator, List<ExprNode> children) {
        this.type = type;
        this.value = value;
        this.name = name;
        this.operator = operator;
        this.children = children != null ? children : Collections.emptyList();
    }
    
    public static ExprNode literal(Object value) { 
        return new ExprNode(Type.LITERAL, value, null, null, null); 
    }
    
    // Ex: x
    public static ExprNode variable(String name) { 
        return new ExprNode(Type.VARIABLE, null, name, null, null); 
    }

    public static ExprNode access(String arrayName, ExprNode index) { 
        return new ExprNode(Type.ACCESS, null, arrayName, null, Collections.singletonList(index)); 
    }

    // Ex: !b, -x. O primeiro filho é a expressão unária.
    public static ExprNode unary(String operator, ExprNode expr) { 
        return new ExprNode(Type.UNARY, null, null, operator, Collections.singletonList(expr)); 
    }

    // Ex: a + b. Filhos são left e right.
    public static ExprNode arith(ExprNode left, String operator, ExprNode right) { 
        return new ExprNode(Type.ARITH, null, null, operator, Arrays.asList(left, right)); 
    }

    // Ex: a > b. Filhos são left e right.
    public static ExprNode rel(ExprNode left, String operator, ExprNode right) { 
        return new ExprNode(Type.REL, null, null, operator, Arrays.asList(left, right)); 
    }
    
    // Ex: a && b. Filhos são left e right.
    public static ExprNode logic(ExprNode left, String operator, ExprNode right) { 
        return new ExprNode(Type.LOGIC, null, null, operator, Arrays.asList(left, right)); 
    }
    
    // Métodos utilitários de conveniência
    public static ExprNode and(ExprNode a, ExprNode b) { return logic(a, "&&", b); }
    public static ExprNode or(ExprNode a, ExprNode b) { return logic(a, "||", b); }
    public static ExprNode not(ExprNode expr) { return unary("!", expr); }
    
    
    // --- Getters (Simplificados para usar a lista children) ---

    public Type getType() { return type; }
    public String getName() { return name; }
    public String getOperator() { return operator; }
    public Object getValue() { return value; }
    public List<ExprNode> getChildren() { return children; }
    
    // Getters de conveniência para nós binários
    public ExprNode getLeft() { return children.size() > 0 ? children.get(0) : null; }
    public ExprNode getRight() { return children.size() > 1 ? children.get(1) : null; }
    // Getter de conveniência para nós unários/acesso
    public ExprNode getExpr() { return children.size() > 0 ? children.get(0) : null; }


    @Override
    public String toString() {
        switch (type) {
            case LITERAL:
                return value.toString();
            case VARIABLE:
                return name;
            case ACCESS:
                return name + "[" + getExpr() + "]";
            case UNARY:
                return operator + getExpr();
            case ARITH:
            case REL:
            case LOGIC:
                return "(" + getLeft() + " " + operator + " " + getRight() + ")";
            default:
                return "ExprNode";
        }
    }
}
