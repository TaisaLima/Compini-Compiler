package intermediate;
import lexer.*;
import symbol.*;

public class Id extends Expr{
    public int offset;
    public Id(Word id, Type p, int b){
        super(id,p);
        offset=b;
    }
}
