package intermediate;

import java.util.List;
import parser.rules.*;

public class ParBlock extends Stmt {
    private List<Stmt> stmts;

    public ParBlock(List<Stmt> stmts, List<String> c3e) {
        this.stmts = stmts;
        c3e.add("PAR_BEGIN");
        for (Stmt s : stmts) {
            s.gen(0, 0);
        }
        c3e.add("PAR_END");
    }
}
