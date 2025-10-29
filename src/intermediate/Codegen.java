package intermediate;

import java.util.*;
import java.util.regex.*;

public class Codegen {
    private StringBuilder asmCode = new StringBuilder();
    private Map<String, Integer> varOffsets = new LinkedHashMap<>();
    private int stackSpaceRequired = 0;
    private int labelCount = 0;

    private void emitAsm(String s) {
        asmCode.append(s).append("\n");
    }

    private int newLabel() {
        return ++labelCount;
    }

    private int getVarOffsetCreate(String var) {
        if (!varOffsets.containsKey(var)) {
            varOffsets.put(var, -(stackSpaceRequired + 4));
            stackSpaceRequired += 4;
        }
        return varOffsets.get(var);
    }

    private Integer getVarOffset(String var) {
        return varOffsets.get(var);
    }

    private static final Pattern ASSIGN_PATTERN = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+)$");
    private static final Pattern PRINT_PATTERN = Pattern.compile("^print\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern IDENT_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    public String generate(String C3E) {
        asmCode.setLength(0);
        varOffsets.clear();
        stackSpaceRequired = 0;
        labelCount = 0;

        String[] lines = C3E.split("\\r?\\n");

        // 1️⃣ Primeira passagem: identificar variáveis
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.matches("^L[0-9]+:$")) continue;

            Matcher mAssign = ASSIGN_PATTERN.matcher(line);
            Matcher mPrint = PRINT_PATTERN.matcher(line);

            if (mAssign.matches()) {
                String target = mAssign.group(1);
                getVarOffsetCreate(target);

                String expr = mAssign.group(2).trim();
                Matcher idm = IDENT_PATTERN.matcher(expr);
                while (idm.find()) {
                    String id = idm.group();
                    if (!NUMBER_PATTERN.matcher(id).matches()) {
                        getVarOffsetCreate(id);
                    }
                }
            } else if (mPrint.matches()) {
                getVarOffsetCreate(mPrint.group(1));
            }
        }

        // 2️⃣ Alinhamento da pilha
        if (stackSpaceRequired % 8 != 0) {
            stackSpaceRequired += (8 - (stackSpaceRequired % 8));
        }

        // 3️⃣ Cabeçalho
        emitAsm(".global _start");
        emitAsm(".text");
        emitAsm("_start:");
        emitAsm("\tPUSH {R7, R11, LR}");
        emitAsm("\tMOV R11, SP");
        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tSUB SP, SP, #%d", stackSpaceRequired));
        }

        // 4️⃣ Geração de código linha a linha
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (line.matches("^L[0-9]+:$")) {
                emitAsm(line);
                continue;
            }

            Matcher mAssign = ASSIGN_PATTERN.matcher(line);
            Matcher mPrint = PRINT_PATTERN.matcher(line);

            if (mAssign.matches()) {
                String target = mAssign.group(1);
                String expr = mAssign.group(2).trim();
                generateExpr(expr, getVarOffset(target));
            } else if (mPrint.matches()) {
                String var = mPrint.group(1);
                emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(var)));
                emitAsm("\tBL print_int");
            }
        }

        // 5️⃣ Finalização
        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tADD SP, SP, #%d", stackSpaceRequired));
        }
        emitAsm("\tPOP {R7, R11, PC}");
        return asmCode.toString();
    }

    // 6️⃣ Geração recursiva de expressões com suporte a +, -, *, /, &&, ||
    private void generateExpr(String expr, int targetOffset) {
        expr = expr.trim();

        // 6.1 Se for número literal
        if (NUMBER_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tMOV R0, #%s", expr));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        // 6.2 Se for variável simples
        if (IDENT_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(expr)));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        // 6.3 Operadores lógicos ||, &&
        int idx;
        if ((idx = expr.indexOf("||")) != -1) {
            String left = expr.substring(0, idx).trim();
            String right = expr.substring(idx + 2).trim();
            int labelTrue = newLabel();
            int labelEnd = newLabel();
            generateExpr(left, targetOffset);
            emitAsm(String.format("\tCMP R0, #1"));
            emitAsm(String.format("\tBEQ L%d", labelTrue));
            generateExpr(right, targetOffset);
            emitAsm(String.format("\tCMP R0, #1"));
            emitAsm(String.format("\tBEQ L%d", labelTrue));
            emitAsm(String.format("\tMOV R0, #0"));
            emitAsm(String.format("\tB L%d", labelEnd));
            emitAsm(String.format("L%d:", labelTrue));
            emitAsm("\tMOV R0, #1");
            emitAsm(String.format("L%d:", labelEnd));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        if ((idx = expr.indexOf("&&")) != -1) {
            String left = expr.substring(0, idx).trim();
            String right = expr.substring(idx + 2).trim();
            int labelFalse = newLabel();
            int labelEnd = newLabel();
            generateExpr(left, targetOffset);
            emitAsm(String.format("\tCMP R0, #0"));
            emitAsm(String.format("\tBEQ L%d", labelFalse));
            generateExpr(right, targetOffset);
            emitAsm(String.format("\tCMP R0, #0"));
            emitAsm(String.format("\tBEQ L%d", labelFalse));
            emitAsm(String.format("\tMOV R0, #1"));
            emitAsm(String.format("\tB L%d", labelEnd));
            emitAsm(String.format("L%d:", labelFalse));
            emitAsm("\tMOV R0, #0");
            emitAsm(String.format("L%d:", labelEnd));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        // 6.4 Operadores aritméticos +, -, *, /
        char[] ops = {'+', '-', '*', '/'};
        for (char op : ops) {
            int opIdx = findMainOperator(expr, op);
            if (opIdx != -1) {
                String left = expr.substring(0, opIdx).trim();
                String right = expr.substring(opIdx + 1).trim();
                generateExpr(left, targetOffset); // R0 = left
                emitAsm("\tPUSH {R0}");
                generateExpr(right, targetOffset); // R0 = right
                emitAsm("\tPOP {R1}");
                switch (op) {
                    case '+': emitAsm("\tADD R0, R1, R0"); break;
                    case '-': emitAsm("\tSUB R0, R1, R0"); break;
                    case '*': emitAsm("\tMUL R0, R1, R0"); break;
                    case '/': 
                        emitAsm("\tMOV R2, R1"); 
                        emitAsm("\tMOV R1, R0");
                        emitAsm("\tBL divide"); // supondo função divide(R1,R0)->R0
                        break;
                }
                emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
                return;
            }
        }

        // 6.5 Parênteses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            generateExpr(expr.substring(1, expr.length() - 1), targetOffset);
            return;
        }
    }

    private int findMainOperator(String expr, char op) {
        int level = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') level++;
            if (c == ')') level--;
            if (level == 0 && c == op) return i;
        }
        return -1;
    }
}
