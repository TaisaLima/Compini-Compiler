package main.lexer;

public class Real extends Token{
    public final float valor;
    public Real(float x){
        super(Tag.REAL);
        valor=x;
    }
    public String toString(){
        return ""+valor;
    }
}
