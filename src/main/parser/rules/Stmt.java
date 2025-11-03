package parser.rules;

import lexer.*;
import intermediate.*;


public class Stmt extends Node{
    public Stmt(){}
    public static Stmt Null = new Stmt();
    public void gen(int a, int b){}
    int after=0;
    
    public static Stmt Enclosing=Stmt.Null;
}
