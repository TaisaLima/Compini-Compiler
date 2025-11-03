package main.codegen;

import java.util.*;
import java.util.regex.*;
import java.util.function.BiConsumer;

public class Codegen {
    private StringBuilder asmCode = new StringBuilder();
    // Armazena o deslocamento (offset) de cada variável em relação ao Frame Pointer (R11)
    private Map<String, Integer> varOffsets = new LinkedHashMap<>();
    private int stackSpaceRequired = 0;
    private int labelCount = 0;

    /**
     * Adiciona uma linha de código assembly ao buffer.
     * @param s A linha de código assembly.
     */
    private void emitAsm(String s) {
        asmCode.append(s).append("\n");
    }

    /**
     * Retorna as rotinas de biblioteca de runtime em Assembly (print_int e divide).
     * @return String contendo o código assembly da biblioteca de runtime.
     */
    private String getRuntimeLibrary() {
        return """
        
        print_int:
            PUSH {R4-R6, LR}    @ Salva registradores de trabalho (R4-R6) e o Link Register (LR)

            @ R1: Endereço do buffer no stack
            @ R2: Contador de dígitos
            SUB SP, SP, #12     @ Aloca 12 bytes no stack para o buffer de string (para o pior caso 10 digitos + \n + \0)
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

        divide:
            @ Rotina de divisão inteira simples (R0 / R1) -> Quociente em R0
            @ R0 = Dividendo (N)
            @ R1 = Divisor (D)
            PUSH {R4, LR}
            MOV R4, #0      @ R4 = Quociente (Q)
        
        divide_loop:
            CMP R0, R1      @ N < D?
            BLT divide_end  @ Se sim, R4 é o resultado
            SUB R0, R0, R1  @ N = N - D
            ADD R4, R4, #1  @ Q = Q + 1
            B divide_loop
            
        divide_end:
            MOV R0, R4      @ Retorna Quociente em R0
            POP {R4, PC}    @ Retorna
        """;
    }

    /**
     * Gera um novo rótulo sequencial (L1, L2, ...).
     * @return O número do novo rótulo.
     */
    private int newLabel() {
        return ++labelCount;
    }

    /**
     * Obtém o deslocamento de uma variável no stack. Se a variável não existir, ela é criada
     * e o espaço no stack é alocado.
     * @param var O nome da variável.
     * @return O deslocamento (offset) da variável em relação ao R11 (FP).
     */
    private int getVarOffsetCreate(String var) {
        if (!varOffsets.containsKey(var)) {
            // A variável é armazenada em SP-4, SP-8, etc. (offsets negativos em relação a R11)
            varOffsets.put(var, -(stackSpaceRequired + 4));
            stackSpaceRequired += 4;
        }
        return varOffsets.get(var);
    }

    /**
     * Obtém o deslocamento de uma variável existente.
     * @param var O nome da variável.
     * @return O deslocamento (offset) ou null se a variável não foi declarada/usada.
     */
    private Integer getVarOffset(String var) {
        return varOffsets.get(var);
    }

    private static final Pattern ASSIGN_PATTERN = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+)$");
    private static final Pattern PRINT_PATTERN = Pattern.compile("^print\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern IDENT_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    /**
     * O primeiro passo de duas fases para calcular o espaço necessário no stack.
     * @param C3E O código de três endereços (3-Address Code) a ser processado.
     * @return O código assembly gerado.
     */
    public String generate(String C3E) {
        asmCode.setLength(0);
        varOffsets.clear();
        stackSpaceRequired = 0;
        labelCount = 0;

        String[] lines = C3E.split("\\r?\\n");

        // Fase 1: Encontrar todas as variáveis e alocar espaço no stack
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("L")) continue;

            Matcher mAssign = ASSIGN_PATTERN.matcher(line);
            Matcher mPrint = PRINT_PATTERN.matcher(line);

            if (mAssign.matches()) {
                String target = mAssign.group(1);
                getVarOffsetCreate(target);

                String expr = mAssign.group(2).trim();
                // Usa regex para encontrar todos os identificadores (variáveis) na expressão
                Matcher idm = IDENT_PATTERN.matcher(expr);
                while (idm.find()) {
                    String id = idm.group();
                    // Garante que não está pegando um literal numérico
                    if (!NUMBER_PATTERN.matcher(id).matches()) {
                        getVarOffsetCreate(id);
                    }
                }
            } else if (mPrint.matches()) {
                getVarOffsetCreate(mPrint.group(1));
            }
        }

        // Ajusta o espaço do stack para ser múltiplo de 8 (alinhamento ABI ARM)
        if (stackSpaceRequired % 8 != 0) {
            stackSpaceRequired += (8 - (stackSpaceRequired % 8));
        }

        // --- Geração do Código Principal (Fase 2) ---

        emitAsm(".global _start");
        emitAsm(".text");
        emitAsm("_start:");
        emitAsm("\tPUSH {R7, R11, LR} @ R7 (syscall), R11 (FP), LR (return address)");
        emitAsm("\tMOV R11, SP        @ R11 (FP) aponta para o topo da pilha de salvamento");
        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tSUB SP, SP, #%d @ Aloca espaço para variáveis locais", stackSpaceRequired));
        }

        // Processa as linhas novamente e gera código para cada comando
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (line.matches("^L[0-9]+:$")) {
                emitAsm(line); // Emite rótulos diretamente
                continue;
            }

            Matcher mAssign = ASSIGN_PATTERN.matcher(line);
            Matcher mPrint = PRINT_PATTERN.matcher(line);

            if (mAssign.matches()) {
                String target = mAssign.group(1);
                String expr = mAssign.group(2).trim();
                generateExpr(expr, getVarOffset(target)); // Gera código para a expressão
            } else if (mPrint.matches()) {
                String var = mPrint.group(1);
                emitAsm("\t@ print " + var);
                emitAsm(String.format("\tLDR R0, [R11, #%d] @ Carrega valor de %s em R0", getVarOffset(var), var));
                emitAsm("\tBL print_int              @ Chama rotina de impressão");
            }
        }

        // Finalização
        emitAsm("\t@ Finaliza o programa");
        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tADD SP, SP, #%d @ Libera espaço do stack", stackSpaceRequired));
        }
        emitAsm("\tPOP {R7, R11, LR}");
        emitAsm("\tMOV R7, #1 @ Syscall 'exit'");
        emitAsm("\tMOV R0, #0 @ Exit code 0");
        emitAsm("\tSWI 0");
        
        // Adiciona o código da biblioteca de runtime no final
        asmCode.append(getRuntimeLibrary());
        return asmCode.toString();
    }

    /**
     * Gera código assembly para uma expressão e armazena o resultado no deslocamento (offset) alvo.
     * Esta função é recursiva e tenta seguir a precedência de operadores.
     * @param expr A string da expressão.
     * @param targetOffset O deslocamento onde o resultado deve ser armazenado.
     */
    private void generateExpr(String expr, int targetOffset) {
        expr = expr.trim();

        // Caso base 1: Literal Numérico
        if (NUMBER_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tMOV R0, #%s", expr));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        // Caso base 2: Identificador (variável)
        if (IDENT_PATTERN.matcher(expr).matches()) {
            emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(expr)));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        // Função auxiliar para calcular uma subexpressão e deixar o resultado em R0
        BiConsumer<String, Integer> evaluateSubExpr = (subExpr, tempOffset) -> {
            if (NUMBER_PATTERN.matcher(subExpr).matches()) {
                emitAsm(String.format("\tMOV R0, #%s", subExpr));
            } else if (IDENT_PATTERN.matcher(subExpr).matches()) {
                emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(subExpr)));
            } else {
                // Chama a função recursivamente, usando um OFFSET temporário 
                // para o STR interno, mas garantindo que R0 tenha o resultado.
                generateExpr(subExpr, tempOffset); 
                // Após a chamada, R0 já deve ter o resultado, mas a chamada recursiva
                // também fez um STR. O próximo POP/STR usará o R0 correto.
            }
        };

        // --- OPERADORES LÓGICOS (Precedência baixa) ---
        int idx;
        if ((idx = expr.indexOf("||")) != -1) {
            String left = expr.substring(0, idx).trim();
            String right = expr.substring(idx + 2).trim();
            int labelTrue = newLabel();
            int labelEnd = newLabel();
            emitAsm("\t@ OR logico (||)");
            generateExpr(left, targetOffset);
            emitAsm(String.format("\tCMP R0, #1"));
            emitAsm(String.format("\tBEQ L%d", labelTrue));
            generateExpr(right, targetOffset);
            emitAsm(String.format("\tCMP R0, #1"));
            emitAsm(String.format("\tBEQ L%d", labelTrue));
            emitAsm(String.format("\tMOV R0, #0 @ Resultado FALSO"));
            emitAsm(String.format("\tB L%d", labelEnd));
            emitAsm(String.format("L%d:", labelTrue));
            emitAsm("\tMOV R0, #1 @ Resultado VERDADEIRO");
            emitAsm(String.format("L%d:", labelEnd));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        if ((idx = expr.indexOf("&&")) != -1) {
            String left = expr.substring(0, idx).trim();
            String right = expr.substring(idx + 2).trim();
            int labelFalse = newLabel();
            int labelEnd = newLabel();
            emitAsm("\t@ AND logico (&&)");
            generateExpr(left, targetOffset);
            emitAsm(String.format("\tCMP R0, #0"));
            emitAsm(String.format("\tBEQ L%d", labelFalse));
            generateExpr(right, targetOffset);
            emitAsm(String.format("\tCMP R0, #0"));
            emitAsm(String.format("\tBEQ L%d", labelFalse));
            emitAsm(String.format("\tMOV R0, #1 @ Resultado VERDADEIRO"));
            emitAsm(String.format("\tB L%d", labelEnd));
            emitAsm(String.format("L%d:", labelFalse));
            emitAsm("\tMOV R0, #0 @ Resultado FALSO");
            emitAsm(String.format("L%d:", labelEnd));
            emitAsm(String.format("\tSTR R0, [R11, #%d]", targetOffset));
            return;
        }

        char[] ops_high = {'*', '/'};
        char[] ops_low = {'+', '-'};

        // --- OPERADORES ARITMÉTICOS (Alta Precedência: *, /) ---
        for (char op : ops_high) {
            int opIdx = findMainOperator(expr, op);
            if (opIdx != -1) {
                String left = expr.substring(0, opIdx).trim();
                String right = expr.substring(opIdx + 1).trim();
                
                emitAsm(String.format("\t@ Operacao aritmetica %c (Alta Prec)", op));

                // 1. Calcula o Lado Esquerdo (Resultado em R0)
                evaluateSubExpr.accept(left, targetOffset); 
                emitAsm("\tPUSH {R0} @ Salva left (op1) no stack");
                
                // 2. Calcula o Lado Direito (Resultado em R0)
                evaluateSubExpr.accept(right, targetOffset); 
                emitAsm("\tPOP {R1} @ Carrega left (op1) em R1. R0 tem right (op2)");
                
                // Agora: R0 = op2 (right), R1 = op1 (left)

                switch (op) {
                    case '*': emitAsm("\tMUL R0, R1, R0 @ R0 = R1 * R0"); break;
                    case '/': 
                        emitAsm("\t@ Chamada divide(R1, R0) -> R0");
                        emitAsm("\tMOV R2, R0 @ Salva R0 (divisor)");
                        emitAsm("\tMOV R0, R1 @ R0 = R1 (dividendo)");
                        emitAsm("\tMOV R1, R2 @ R1 = R2 (divisor)");
                        emitAsm("\tBL divide");
                        break;
                }
                
                emitAsm(String.format("\tSTR R0, [R11, #%d] @ Salva resultado final", targetOffset));
                return;
            }
        }
        
        // --- OPERADORES ARITMÉTICOS (Baixa Precedência: +, -) ---
        for (char op : ops_low) {
            int opIdx = findMainOperator(expr, op);
            if (opIdx != -1) {
                String left = expr.substring(0, opIdx).trim();
                String right = expr.substring(opIdx + 1).trim();
                
                emitAsm(String.format("\t@ Operacao aritmetica %c (Baixa Prec)", op));

                // 1. Calcula o Lado Esquerdo (Resultado em R0)
                evaluateSubExpr.accept(left, targetOffset); 
                emitAsm("\tPUSH {R0} @ Salva left (op1) no stack");
                
                // 2. Calcula o Lado Direito (Resultado em R0)
                evaluateSubExpr.accept(right, targetOffset); 
                emitAsm("\tPOP {R1} @ Carrega left (op1) em R1. R0 tem right (op2)");
                
                // Agora: R0 = op2 (right), R1 = op1 (left)

                switch (op) {
                    case '+': emitAsm("\tADD R0, R1, R0 @ R0 = R1 + R0"); break;
                    case '-': emitAsm("\tSUB R0, R1, R0 @ R0 = R1 - R0"); break;
                    // Removido * e / daqui
                }
                
                emitAsm(String.format("\tSTR R0, [R11, #%d] @ Salva resultado final", targetOffset));
                return;
            }
        }


        // Caso 3: Expressão entre parênteses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            generateExpr(expr.substring(1, expr.length() - 1), targetOffset);
            return;
        }
        
        // Se a expressão for complexa e não cair em nenhum caso acima, pode ser um erro de parsing
        // ou um caso não implementado (e.g., !=, >, <, etc.)
        // Adiciona um log de erro ou assume que é um caso mais complexo de relacional/unário
        // A geração de código para relacionais (>, <, ==) deve ser feita aqui, mas não estava no seu código.
        emitAsm(String.format("\t@ ERRO: Expressao nao tratada: %s", expr));
    }

    /**
     * Encontra o índice do operador principal no nível mais baixo de aninhamento
     * (fora de parênteses) para respeitar a precedência.
     * @param expr A string da expressão.
     * @param op O caractere do operador a ser procurado.
     * @return O índice do operador ou -1 se não for encontrado.
     */
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