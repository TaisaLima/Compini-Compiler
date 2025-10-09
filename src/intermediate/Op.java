package intermediate;
import lexer.*;
import symbol.*;

public class Op extends Expr{
    public Op(Token tok, Type p){
        super(tok,p);
    }
    public Expr reduce(){
        Expr x= gen();
        Temp t = new Temp(tipo);
        emit( t.toString()+" = "+x.toString());
        return t;
    }
}
