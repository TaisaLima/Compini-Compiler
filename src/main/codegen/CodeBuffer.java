package main.codegen;

import main.lexer.*;


public class CodeBuffer {
    private static StringBuilder buffer = new StringBuilder();

    public static void emit(String s) {
        buffer.append("\t").append(s).append("\n");
    }

    public static String getCode() {
        return buffer.toString().trim(); 
    }

    public static void clear() {
        buffer.setLength(0);
    }
}