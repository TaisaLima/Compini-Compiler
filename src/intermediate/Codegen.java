package intermediate;

import lexer.*;
import parser.*;
import java.util.*;
import java.util.regex.*;
import java.util.function.BiConsumer;

public class Codegen {
    private StringBuilder asmCode = new StringBuilder();
    private Map<String, Integer> varOffsets = new LinkedHashMap<>();
    private int stackSpaceRequired = 0;
    private int labelCount = 0;

    private void emitAsm(String s) {
        asmCode.append(s).append("\n");
    }

    private String getRuntimeLibrary() {
        return """
        
        print_int:
            PUSH {R4-R6, LR}    @ Salva registradores de trabalho (R4-R6) e o Link Register (LR)

            @ R1: Endereço do buffer no stack
            @ R2: Contador de dígitos
            SUB SP, SP, #12     @ Aloca 12 bytes no stack para o buffer de string
            MOV R1, SP          
            MOV R2, #0          

            @ --- Tratamento do caso N=0 ---
            CMP R0, #0
            BNE print_int_div_start

            @ Se R0 é 0, armazena '0' e pula a divisão
            MOV R0, #'0'        @ R0 = caractere '0'
            STRB R0, [R1, #0]   @ Armazena '0' no buffer
            MOV R2, #1          @ R2 = 1 dígito
            B print_int_reverse_loop_start

        print_int_div_start:
            @ R0 contém o número de entrada (N)

        print_int_loop:
            @ Implementação da divisão manual (N / 10):
            MOV R6, R0          @ R6 = N (Numerador atual)
            MOV R4, #0          @ R4 = Quociente (Q) = 0
            MOV R5, #10         @ R5 = Divisor (D) = 10

        div_by_10_loop:
            CMP R6, R5          @ Compara N com 10
            BLT div_by_10_end   @ Se N < 10, R6 é o resto.
            SUB R6, R6, R5      @ N = N - 10
            ADD R4, R4, #1      @ Q = Q + 1
            B div_by_10_loop

        div_by_10_end:
            @ R6 agora tem o Resto (R) - dígito
            @ R4 agora tem o Quociente (Q) - novo N
            
            @ Converte o dígito (resto R6) para caractere ASCII (em R0)
            MOV R0, R6
            ADD R0, R0, #'0'
            STRB R0, [R1, R2]   @ Armazena o dígito no buffer (R1+R2)
            ADD R2, R2, #1      @ Incrementa o contador de dígitos

            @ R0 = Quociente (Q) para a próxima iteração
            MOV R0, R4
            
            CMP R0, #0          @ Compara se o quociente é 0
            BNE print_int_loop  @ Se não for 0, continua

        print_int_reverse_loop_start:
            @ Agora imprime a string (dígitos) na ordem inversa
            CMP R2, #0
            BEQ print_int_end   
            
        print_int_reverse_loop:
            SUB R2, R2, #1      @ Decrementa o contador (indice do digito)
            LDRB R0, [R1, R2]   @ Carrega o dígito (byte) do buffer

            @ Syscall 'write' para imprimir 1 caractere
            PUSH {R0}           @ Salva R0 (o caractere) no stack
            
            MOV R7, #4          @ Syscall 'write'
            MOV R0, #1          @ File descriptor 1 (stdout)
            MOV R1, SP          @ R1 aponta para o caractere salvo (Endereço)
            MOV R3, R2          @ Salva R2 temporariamente
            MOV R2, #1          @ Tamanho de 1 byte
            SWI 0               @ Chama o sistema
            MOV R2, R3          @ Restaura R2
            
            POP {R0}            @ Restaura R0 
            
            CMP R2, #0         
            BNE print_int_reverse_loop 

        print_int_end:
            ADD SP, SP, #12     @ Libera o espaço alocado no stack
            POP {R4-R6, PC}     @ Restaura registradores e retorna

        """;
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

        if (stackSpaceRequired % 8 != 0) {
            stackSpaceRequired += (8 - (stackSpaceRequired % 8));
        }

        emitAsm(".global _start");
        emitAsm(".text");
        emitAsm("_start:");
        emitAsm("\tPUSH {R7, R11, LR}");
        emitAsm("\tMOV R11, SP");
        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tSUB SP, SP, #%d", stackSpaceRequired));
        }

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

        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tADD SP, SP, #%d", stackSpaceRequired));
        }
        emitAsm("\tPOP {R7, R11, PC}");
        asmCode.append(getRuntimeLibrary());
        return asmCode.toString();
    }

    private void generateExpr(String expr, int targetOffset) {
        expr = expr.trim();

        if (NUMBER_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tMOV R0, #%s", expr));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        if (IDENT_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(expr)));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

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

        char[] ops_high = {'*', '/'};
        char[] ops_low = {'+', '-'};

        // Função auxiliar para calcular uma subexpressão e deixar o resultado em R0
        BiConsumer<String, Integer> evaluateSubExpr = (subExpr, tempOffset) -> {
            if (NUMBER_PATTERN.matcher(subExpr).matches()) {
                emitAsm(String.format("\tMOV R0, #%s", subExpr));
            } else if (IDENT_PATTERN.matcher(subExpr).matches()) {
                emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(subExpr)));
            } else {
                // Chama a função recursivamente, usando um TEMP OFFSET temporário 
                // para o STR interno, mas garantindo que R0 tenha o resultado.
                generateExpr(subExpr, tempOffset); 
            }
        };

        // Procura primeiro os operadores de alta precedência (*, /)
        for (char op : ops_high) {
            int opIdx = findMainOperator(expr, op);
            if (opIdx != -1) {
            
            }
        }
        // Procura os operadores de baixa precedência (+, -)
        for (char op : ops_low) {
            int opIdx = findMainOperator(expr, op);
            if (opIdx != -1) {
                String left = expr.substring(0, opIdx).trim();
                String right = expr.substring(opIdx + 1).trim();
                
                // 1. Calcula o Lado Esquerdo (Resultado em R0)
                // Usamos o targetOffset temporariamente para o STR de subexpressões
                evaluateSubExpr.accept(left, targetOffset); 
                emitAsm("\tPUSH {R0} @ Salva left (R0) em R1 (posicao R1 na pilha)");
                
                // 2. Calcula o Lado Direito (Resultado em R0)
                evaluateSubExpr.accept(right, targetOffset); 
                emitAsm("\tPOP {R1} @ Carrega left (salvo) em R1. R0 tem right");
                
                // Agora: R0 = right (op2), R1 = left (op1)

                emitAsm("\t@ Operacao: R0 = op1 " + op + " op2");
                switch (op) {
                    case '+': emitAsm("\tADD R0, R1, R0"); break; // R0 = R1 + R0
                    case '-': emitAsm("\tSUB R0, R1, R0"); break; // R0 = R1 - R0
                    case '*': emitAsm("\tMUL R0, R1, R0"); break; // R0 = R1 * R0
                    case '/': 
                        emitAsm("\t@ Chamada divide(R1, R0) -> R0");
                        emitAsm("\tMOV R2, R0 @ Salva R0 (right)");
                        emitAsm("\tMOV R0, R1 @ R0 = R1 (left/dividendo)");
                        emitAsm("\tMOV R1, R2 @ R1 = R2 (right/divisor)");
                        emitAsm("\tBL divide");
                        break;
                }
                
                emitAsm(String.format("\tSTR R0, [R11, #%d] @ Salva resultado final", targetOffset));
                return;
            }
        }

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