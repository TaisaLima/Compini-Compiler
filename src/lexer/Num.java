package lexer;

public class Num extends Token{
    public final int valor;
    public Num(int val){
        super(Tag.NUM);
        valor = val;
    }
    public String toString(){
        return "" + valor;
    }
}
