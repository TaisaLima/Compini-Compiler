package intermediate;

import symbol.*;
import parser.rules.*;

import java.io.IOException;

public class SendStmt extends Stmt {
    private String chanId;
    private String[] valores; 

    public SendStmt(String chanId, String... valores){
        this.chanId = chanId;
        this.valores = valores;
    }

    @Override
    public void gen(int a, int b){
        VarCChannel canal = (VarCChannel) Env.getVar(chanId);
        try {
            String msg = String.join(",", valores);
            canal.send(msg);
            System.out.println("[Send] Mensagem enviada: " + msg);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
