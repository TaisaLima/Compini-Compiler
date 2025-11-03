package symbol;

import lexer.*;
import intermediate.*;
import parser.rules.*;

public class Id extends Expr{
    public int offset;
    public Id(Word id, Type p, int b){
        super(id,p);
        offset=b;
    }
}
