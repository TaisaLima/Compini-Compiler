package tests;

import lexer.Lexer;
import lexer.Token;
import java.io.*;
import java.util.*;

public class TestLexer {
    public static void main(String[] args) throws Exception {
        String folderPath = "src/tests/valids";
        System.out.println("=== Testando Lexer ===");

        File folder = new File(folderPath);
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            System.out.println("\nArquivo: " + file.getName());

            // Criar Lexer lendo do arquivo
            Lexer lexer = new Lexer(new FileReader(file));

            Token token;
            while ((token = lexer.scan()) != null) {
                System.out.println(token);
            }
        }
    }
}
