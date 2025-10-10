package intermediate;

import java.util.*;
import java.util.regex.*;

public class Codegen {
    private StringBuilder asmCode = new StringBuilder();
    private Map<String, Integer> varOffsets = new LinkedHashMap<>();
    private int stackSpaceRequired = 0;

    private void emitAsm(String s) {
        asmCode.append(s).append("\n");
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
                String var = mPrint.group(1);
                getVarOffsetCreate(var);
            } else {
                Matcher idm = IDENT_PATTERN.matcher(line);
                while (idm.find()) {
                    String id = idm.group();
                    if (!NUMBER_PATTERN.matcher(id).matches()) {
                        getVarOffsetCreate(id);
                    }
                }
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

                if (NUMBER_PATTERN.matcher(expr).matches()) {
                    emitAsm(String.format("\tMOV R0, #%s", expr));
                    emitAsm(String.format("\tSTR R0, [R11, #%d]", getVarOffset(target)));
                }
     
                else if (expr.contains("+")) {
                    String[] ops = expr.split("\\+");
                    String left = ops[0].trim();
                    String right = ops[1].trim();
    
                    emitAsm(String.format("\tSTR R0, [R11, #%d]", getVarOffset(target)));
                }
                else {
                    if (NUMBER_PATTERN.matcher(expr).matches()) {
                        emitAsm(String.format("\tMOV R0, #%s", expr));
                    } else {
                        emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(expr)));
                    }
                    emitAsm(String.format("\tSTR R0, [R11, #%d]", getVarOffset(target)));
                }
            } else if (mPrint.matches()) {
                String var = mPrint.group(1);
                emitAsm(String.format("\tLDR R0, [R11, #%d]", getVarOffset(var)));
                emitAsm("\tBL print_int");
            } else {
            }
        }

        if (stackSpaceRequired > 0) {
            emitAsm(String.format("\tADD SP, SP, #%d", stackSpaceRequired));
        }


        return asmCode.toString();
    }

    private void loadOperandIntoR0(String operand) {
        if (NUMBER_PATTERN.matcher(operand).matches()) {
            emitAsm(String.format("\tMOV R0, #%s", operand));
        } else {
            Integer off = getVarOffset(operand);
            if (off == null) {
              
                off = getVarOffsetCreate(operand);
            }
            emitAsm(String.format("\tLDR R0, [R11, #%d]", off));
        }
    }
}
