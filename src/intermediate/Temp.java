package interm;
import lexer.*;
import symbol.*;

public class Temp extends Expr{
    static int count=0;
    int numero=0;
    public Temp(Type p){
        super(Word.temp,p);
        numero =++count;
    }

    @Override
    public String toString() {
        return "t"+numero;
    }
}
