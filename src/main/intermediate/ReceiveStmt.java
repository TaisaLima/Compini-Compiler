package intermediate;

import java.util.List;
import parser.rules.*;
import symbol.*;
import java.io.IOException;


public class ReceiveStmt extends Stmt {
    private String chanId;
    private String varId; 

    public ReceiveStmt(String chanId, String varId){
        this.chanId = chanId;
        this.varId = varId;
    }

    @Override
    public void gen(int a, int b){
        VarCChannel canal = (VarCChannel) Env.getVar(chanId);
        try {
            String msg = canal.receive();
            Env.setVar(varId, msg);
            System.out.println("[Receive] Mensagem recebida: " + msg);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
