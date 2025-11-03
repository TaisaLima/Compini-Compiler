package main.semantic.types;

import main.lexer.*;

public class ArrayType extends Type{
    public Type of;
    public int size=1;
    public ArrayType (int sz, Type x){
        super("[]",Tag.INDEX, sz*x.width);
        size=sz;
        of=x;
    }

    public String toString() {
        return "{"+size+"}"+of.toString();
    }
}
