package main.ast.core;

import main.semantic.visitors.ASTVisitor;

public interface Node {

    public void accept(ASTVisitor visitor);

    String toString();
}