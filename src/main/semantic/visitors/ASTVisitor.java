package main.semantic.visitors;

import main.ast.expr.*;
import main.ast.stmt.*;

public interface ASTVisitor<T> {
    // Expressões
    T visitBinaryOp(BinaryOpNode node);
    T visitUnaryOp(UnaryOpNode node);
    T visitLiteral(LiteralNode node);
    T visitIdentifier(IdentifierNode node);
    T visitArrayAccess(ArrayAccessNode node);
    
    // Statements
    T visitAssignment(AssignmentNode node);
    T visitIf(IfNode node);
    T visitWhile(WhileNode node);
    T visitDoWhile(DoWhileNode node);
    T visitBreak(BreakNode node);
    T visitPrint(PrintNode node);
    T visitSequence(SequenceNode node);
    T visitSeq(SeqBlockNode node);
    T visitPar(ParBlockNode node);
    T visitSend(SendNode node);
    T visitReceive(ReceiveNode node);
}