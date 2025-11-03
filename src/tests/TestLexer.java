package tests;

import lexer.Lexer;
import lexer.Token;
import java.io.*;
import java.util.*;

public class TestLexer {
    public static void main(String[] args) throws Exception {
        String folderPath = "src/tests/valids" ;
        System.out.println("=== Testando Lexer ===");

        File folder = new File(folderPath);
        

        File[] fileList = folder.listFiles();
        if (fileList == null) {
            System.err.println("Erro: Pasta 'src/tests/valids' não encontrada ou vazia.");
            return;
        }

        for (File file : fileList) {
            if (file.isDirectory() || file.isHidden()) continue; 
            
            System.out.println("\nArquivo: " + file.getName());

            try (FileReader fr = new FileReader(file)) {
                Lexer lexer = new Lexer(fr);

                Token token;

                while ((token = lexer.nextToken()) != null) { 
                    System.out.println(token);
                }
            } catch (IOException e) {
                System.err.println("Erro de leitura/IO no arquivo " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}