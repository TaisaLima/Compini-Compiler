package main.ast.stmt;

import java.util.List;
import main.ast.expr.ExprNode;
import main.ast.core.Node; 


public class StmtNode implements Node {
    
    public int after = 0;

    public enum Type {

        // --- Controle de Fluxo ---
        IF, ELSE, WHILE, DO, BREAK, RETURN, NOP, // NOP (No-Operation) é essencial

        // --- Variáveis e I/O ---
        DECL,       // Usado temporariamente para incluir declarações na lista de comandos.
        ASSIGN,     // Atribuição simples: a = 5
        ARRAY_ASSIGN, // Atribuição em array: arr[i] = 5
        PRINT, 

        // --- Concorrência e Blocos ---
        SEND, 
        RECEIVE,
        SEQ_BLOCK,  
        PAR_BLOCK,  // Bloco Paralelo: { s1 | s2; }
    }

    public final Type type;
    

    // Bloco/Sequência: (SEQ_BLOCK, PAR_BLOCK)
    private final List<StmtNode> body; 
    
    // IF/WHILE/DO: (IF, WHILE, DO)
    private final ExprNode condition; 
    private final StmtNode stmt1; 
    private final StmtNode stmt2; // Usado apenas pelo ELSE

    // Atribuição/Expressão: (ASSIGN, ARRAY_ASSIGN, PRINT, RETURN)
    private final ExprNode target; 
    private final ExprNode value;  

    // Comunicação: (SEND, RECEIVE)
    private final String channelName;
    
    private StmtNode(Type type, List<StmtNode> body, ExprNode condition, StmtNode stmt1, StmtNode stmt2, ExprNode target, ExprNode value, String channelName) {
        this.type = type;
        this.body = body;
        this.condition = condition;
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
        this.target = target;
        this.value = value;
        this.channelName = channelName;
    }

    public static StmtNode seqBlock(List<StmtNode> body) {
        return new StmtNode(Type.SEQ_BLOCK, body, null, null, null, null, null, null);
    }
    
    public static StmtNode parBlock(List<StmtNode> body) {
        return new StmtNode(Type.PAR_BLOCK, body, null, null, null, null, null, null);
    }

    // --- Controle de Fluxo ---
    public static StmtNode ifNode(ExprNode condition, StmtNode stmt1) {
        return new StmtNode(Type.IF, null, condition, stmt1, null, null, null, null);
    }

    public static StmtNode ifElseNode(ExprNode condition, StmtNode stmt1, StmtNode stmt2) {
        return new StmtNode(Type.ELSE, null, condition, stmt1, stmt2, null, null, null);
    }
    
    public static StmtNode whileNode(ExprNode condition, StmtNode stmt1) {
        return new StmtNode(Type.WHILE, null, condition, stmt1, null, null, null, null);
    }

    public static StmtNode doWhileNode(StmtNode stmt1, ExprNode condition) {
        return new StmtNode(Type.DO, null, condition, stmt1, null, null, null, null);
    }
    
    public static StmtNode breakNode() {
        return new StmtNode(Type.BREAK, null, null, null, null, null, null, null);
    }
    
    public static StmtNode nopNode() {
        return new StmtNode(Type.NOP, null, null, null, null, null, null, null);
    }
    
    // --- I/O e Atribuição ---
    public static StmtNode assignNode(ExprNode target, ExprNode value) {
        return new StmtNode(Type.ASSIGN, null, null, null, null, target, value, null);
    }
    
    public static StmtNode arrayAssignNode(ExprNode target, ExprNode value) {
        return new StmtNode(Type.ARRAY_ASSIGN, null, null, null, null, target, value, null);
    }

    public static StmtNode printNode(ExprNode expr) {
        return new StmtNode(Type.PRINT, null, null, null, null, expr, null, null);
    }
    
    public static StmtNode returnNode(ExprNode expr) {
        return new StmtNode(Type.RETURN, null, null, null, null, expr, null, null);
    }

    // --- Comunicação ---
    public static StmtNode sendNode(String channelName, ExprNode messageExpr) {
        return new StmtNode(Type.SEND, null, null, null, null, messageExpr, null, channelName);
    }
    
    public static StmtNode receiveNode(String channelName, ExprNode variableTarget) {
        return new StmtNode(Type.RECEIVE, null, null, null, null, variableTarget, null, channelName);
    }
    
    
    public Type getStmtType() { return type; }
    public List<StmtNode> getBody() { return body; }
    public ExprNode getCondition() { return condition; }
    public StmtNode getStmt1() { return stmt1; }
    public StmtNode getStmt2() { return stmt2; }
    public ExprNode getTarget() { return target; }
    public ExprNode getValue() { return value; }
    public String getChannelName() { return channelName; }
    
    
    @Override
    public void accept(main.semantic.visitors.ASTVisitor visitor) {
        // Implemente a lógica de aceitar o visitor (que pode ser Semântica ou Codegen)
        // O método 'visit' exato dependerá de como você estrutura seu ASTVisitor.
        // visitor.visit(this); 
    }

    // ----------------------------------------------------
    // toString() para Debug
    // ----------------------------------------------------

    @Override
    public String toString() {
        switch (type) {
            case IF:
                return "IF (" + condition + ") THEN " + stmt1;
            case ELSE:
                return "IF (" + condition + ") THEN " + stmt1 + " ELSE " + stmt2;
            case WHILE:
                return "WHILE (" + condition + ") " + stmt1;
            case DO:
                return "DO " + stmt1 + " WHILE (" + condition + ");";
            case BREAK:
                return "BREAK;";
            case PRINT:
                return "PRINT (" + target + ");";
            case RETURN:
                return "RETURN " + target + ";";
            case ASSIGN:
                return target + " = " + value + ";";
            case ARRAY_ASSIGN:
                return "ARRAY_ASSIGN: " + target + " = " + value + ";";
            case SEND:
                return "SEND (" + channelName + ", " + target + ");";
            case RECEIVE:
                return target + " <- " + channelName + ";";
            case SEQ_BLOCK:
                return "{ SEQ BLOCK: " + body + " }";
            case PAR_BLOCK:
                return "{ PAR BLOCK: " + body + " }";
            case NOP:
                return "NOP";
            default:
                return "StmtNode (" + type + ")";
        }
    }
}