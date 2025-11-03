package main.ast.core;

import main.semantic.visitors.ASTVisitor;
import main.ast.stmt.StmtNode;
import java.util.List;

public class ProgramNode implements Node {
    private List<Node> declarations;
    private StmtNode mainBlock;
    
    public ProgramNode(List<Node> declarations, StmtNode mainBlock) {
        this.declarations = declarations;
        this.mainBlock = mainBlock;
    }
    
    public List<Node> getDeclarations() {
        return declarations;
    }
    
    public StmtNode getMainBlock() {
        return mainBlock;
    }
    
    public void accept(ASTVisitor visitor) {

    }
    
    public String toString() {
        return "Program{declarations=" + declarations + ", main=" + mainBlock + "}";
    }
}