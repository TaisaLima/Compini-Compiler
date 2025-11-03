package symbol;

import java.util.Hashtable;
import lexer.*;
import intermediate.*;

public class Env {
    private Hashtable<Token, Id> tabela; 
    protected Env antes;

    
    private static Hashtable<String, Object> globalTable = new Hashtable<>();

    public Env(Env n){
        tabela = new Hashtable<Token, Id>(); 
        antes = n;
    }

    public void put(Token x, Id y){
        tabela.put(x,y);
    }

    public Id get(Token z){
        for(Env e = this; e != null; e = e.antes){
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
