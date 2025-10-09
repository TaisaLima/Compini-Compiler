package intermediate;

public class CodeBuffer {
    private static StringBuilder buffer = new StringBuilder();

    public static void emit(String s) {
        // Inclui um tab para formatar o C3E
        buffer.append("\t").append(s).append("\n");
    }

    public static String getCode() {
        // Retorna o código, removendo o '\t' e a quebra de linha do início
        return buffer.toString().trim(); 
    }

    public static void clear() {
        buffer.setLength(0);
    }
}