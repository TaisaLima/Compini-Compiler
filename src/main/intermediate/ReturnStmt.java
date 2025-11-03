package intermediate;

import parser.rules.*;

public class ReturnStmt extends Stmt {
    private Expr expr;

    public ReturnStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public void gen(int b, int a) {
        emit("return " + (expr != null ? expr.toString() : ""));
    }
}
