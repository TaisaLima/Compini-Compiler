package parser.rules;

import lexer.*;
import intermediate.*;
import symbol.*;

public class Acess extends Op {
    public Id array;
    public Expr index;
    public Acess(Id a, Expr i, Type p) { 
        super(new Word("[]", Tag.INDEX), p);
    }
    public Expr gen() { return new Acess(array, index.reduce(), tipo); }
    public void jumping(int t,int f) { emitjumps(reduce().toString(),t,f); }
    public String toString() {
        return array.toString() + " [ " + index.toString() + " ]";
    }
}