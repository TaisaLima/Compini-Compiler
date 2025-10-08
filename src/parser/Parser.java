package tests;

import lexer.Lexer;
import parser.Parser;
import java.io.*;
import java.util.*;

public class TestParser {
    public static void main(String[] args) throws Exception {
        String folderPath = "src/tests/valids";
        System.out.println("=== Testando Parser ===");

        File folder = new File(folderPath);
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            System.out.println("\nArquivo: " + file.getName());

            Lexer lexer = new Lexer(new FileReader(file));
            Parser parser = new Parser(lexer); // Ajuste se necessário

            parser.program(); // Trocar para o método correto no seu Parser
        }
    }
}
