package main.ast.stmt;

import java.util.List;
import main.semantic.types.Type;
import main.ast.core.Node;


public class VariableDeclNode implements Node {

    private final Type variableType;
    private final List<String> identifiers; 
    
    
    public VariableDeclNode(Type variableType, List<String> identifiers) {
        this.variableType = variableType;
        this.identifiers = identifiers;
    }

   
    public Type getVariableType() {
        return variableType;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }


    @Override
    public String toString() {
        return "VAR_DECL [Type: " + variableType + ", Names: " + String.join(", ", identifiers) + "]";
    }
}