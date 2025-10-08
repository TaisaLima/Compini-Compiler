package symbol;

import java.util.Hashtable;
import lexer.*;
import interm.*;

//Objetivo Mapear os Tokens de Words aos Objetos da classe Id
public class Env {
    private Hashtable tabela;
    protected Env antes;
    public Env(Env n){
        tabela = new Hashtable();
        antes = n;
    }
    public void put(Token x, Id y){
        tabela.put(x,y);
    }
    public Id get(Token z){
        for(Env e=this; e!=null; e=e.antes){
            Id acha=(Id)(e.tabela.get(z));
            if(acha != null) return acha;
        }
        return null;
    }
}
