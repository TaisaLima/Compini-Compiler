package intermediate;

import lexer.*;

public class Node {
    public int lexline = 0;          
    public static int labels = 0;    

    public Node() {                 
        lexline = Lexer.line;
    }

    public void error(String s) {   
        throw new Error("near line " + lexline + ": " + s);
    }

    public int newlabel() {        
        return ++labels;
    }

    public void emitlabel(int i) {  
        System.out.print("L" + i + ":");
    }

    public void emit(String s) {    
        System.out.println("\t" + s);
    }
}
