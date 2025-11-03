package main.semantic; 

import main.lexer.*;
import main.semantic.types.*;

public class Id extends Expr{
    public int offset;
    public Id(Word id, Type p, int b){
        super(id,p);
        offset=b;
    }
}
