package symbol;
import lexer.*;

public class Array extends Type{
    public Type of;
    public int size=1;
    public Array (int sz, Type x){
        super("[]",Tag.INDEX, sz*x.width);
        size=sz;
        of=x;
    }

    public String toString() {
        return "{"+size+"}"+of.toString();
    }
}
