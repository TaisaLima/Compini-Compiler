package tests;

import lexer.Lexer;
import parser.Parser;
import java.io.*;
import java.util.*;

public class TestParser {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando Testes de Parser ===");
        
        // 1. Testar Arquivos Válidos (DEVE PASSAR)
        runTests("src/tests/valids", true);
        
        // 2. Testar Arquivos Inválidos (DEVE FALHAR)
        runTests("src/tests/invalids", false);
    }
    
    private static void runTests(String folderPath, boolean shouldPass) {
        String status = shouldPass ? "PASSAR" : "FALHAR";
        String folderName = shouldPass ? "Válidos" : "Inválidos";
        
        System.out.println("\n--- Testando Arquivos " + folderName + " (Esperado: " + status + ") ---");

        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();
        
        if (fileList == null) {
            System.err.println("Aviso: Pasta '" + folderPath + "' não encontrada ou vazia.");
            return;
        }

        for (File file : fileList) {
            if (file.isDirectory() || file.isHidden()) continue; 
            
            System.out.print(file.getName() + " -> ");
            
            boolean passed = runSingleTest(file);

            if (shouldPass) {
                // Caso: Esperava passar
                if (passed) {
                    System.out.println("SUCESSO");
                } else {
                    System.out.println("FALHA INESPERADA! (Erro de Sintaxe encontrado)");
                }
            } else {
                // Caso: Esperava falhar
                if (!passed) {
                    System.out.println("SUCESSO.(Erro de Sintaxe capturado)");
                } else {
                    System.out.println("FALHA INESPERADA! (Não encontrou Erro de Sintaxe)");
                }
            }
        }
    }
    
    private static boolean runSingleTest(File file) {
        try (FileReader fr = new FileReader(file)) {
            Lexer lexer = new Lexer(fr);
            Parser parser = new Parser(lexer); 
            
            parser.program(); 
            
            // Se chegou aqui, passou
            return true;
            
        } catch (Error e) {
            // Captura o erro lançado pelo parser (syntaxError)
           System.out.println("Detalhe: " + e.getMessage()); 
            return false;
        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
            return false;
        }
    }
}