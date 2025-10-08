package interm;
import lexer.*;
import symbol.*;

public class Acess extends Op {
    public Id array;
    public Expr index;
    public Acess(Id a, Expr i, Type p) { // p é o tipo de elemento após
        super(new Word("[]", Tag.INDEX), p); // achatar o arranjo
        array = a; index = i;
    }
    public Expr gen() { return new Acess(array, index.reduce(), tipo); }
    public void jumping(int t,int f) { emitjumps(reduce().toString(),t,f); }
    public String toString() {
        return array.toString() + " [ " + index.toString() + " ]";
    }
}