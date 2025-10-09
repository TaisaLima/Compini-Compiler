package symbol;

import java.util.Hashtable;
import lexer.*;
import intermediate.*;

// Objetivo: Mapear os Tokens de Words aos Objetos da classe Id, representando a Tabela de Símbolos.
public class Env {
    private Hashtable<Token, Id> tabela; 
    protected Env antes;
    
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
}