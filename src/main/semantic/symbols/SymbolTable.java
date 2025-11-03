package main.semantic; 

import java.util.Hashtable;
import main.lexer.*;

public class SymbolTable {
    private Hashtable<Token, Id> tabela; 
    protected SymbolTable antes;

    
    private static Hashtable<String, Object> globalTable = new Hashtable<>();

    public SymbolTable(SymbolTable n){
        tabela = new Hashtable<Token, Id>(); 
        antes = n;
    }

    public void put(Token x, Id y){
        tabela.put(x,y);
    }

    public Id get(Token z){
        for(SymbolTable e = this; e != null; e = e.antes){
            Id acha = e.tabela.get(z); 
            if(acha != null) return acha;
        }
        return null;
    }


    public static Object getVar(String name){
        return globalTable.get(name);
    }

    public static void setVar(String name, Object value){
        globalTable.put(name, value);
    }
}
