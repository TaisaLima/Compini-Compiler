package intermediate;

import java.util.List;
import parser.rules.*;

public class SeqBlock extends Stmt {
    private List<Stmt> stmts;

    public SeqBlock(List<Stmt> stmts){
        this.stmts = stmts;
    }

    @Override
    public void gen(int a, int b){
        for(Stmt s : stmts){
            s.gen(a,b);
        }
    }
}
