package tests;

import intermediate.Codegen; 
import lexer.Lexer;
import parser.Parser;
import intermediate.CodeBuffer;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestIntermediate {
    
    private static final String VALID_FOLDER = "src/tests/valids";
    private static final String EXPECTED_C3E_FOLDER = "src/tests/c3e_expected";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando Testes de Geração de Código de 3 endereços (C3E) ===");

        runC3ETests(VALID_FOLDER);
    }
    
    private static void runC3ETests(String folderPath) {
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();
        
        if (fileList == null) {
            System.err.println("Aviso: Pasta '" + folderPath + "' não encontrada ou vazia.");
            return;
        }

        for (File sourceFile : fileList) {
            if (sourceFile.isDirectory() || sourceFile.isHidden() || !sourceFile.getName().endsWith(".txt")) continue; 
            
      
            String expectedFileName = sourceFile.getName().replace(".txt", ".c3e");
            File expectedFile = new File(EXPECTED_C3E_FOLDER, expectedFileName);

            System.out.print(sourceFile.getName() + " -> ");
            runSingleC3ETest(sourceFile, expectedFile);
        }
    }
    
    private static void runSingleC3ETest(File sourceFile, File expectedFile) {
        CodeBuffer.clear(); 
        boolean passed = false; 
        String generatedC3E = "";
        
        try (FileReader fr = new FileReader(sourceFile)) {
         
            Lexer lexer = new Lexer(fr);
            Parser parser = new Parser(lexer); 
            
            parser.program(); 
            
            generatedC3E = CodeBuffer.getCode();

            Codegen codegen = new Codegen();
  
            String assemblyCode = codegen.generate(generatedC3E);
          
              if (!expectedFile.exists()) {
            return;
            }
            if (passed) { 
                System.out.println("SUCESSO (C3E OK)");
                System.out.println("\n--- Assembly ARMv7 Gerado para " + sourceFile.getName() + " ---");
                System.out.println(assemblyCode);
            }

            String expectedCode = readFileContents(expectedFile);

            String generatedCode = CodeBuffer.getCode().trim(); 

            if (generatedCode.equals(expectedCode)) {
                System.out.println("SUCESSO (C3E OK)");
            } else {
                System.out.println("FALHA (C3E NÃO CORRESPONDE)");
                System.out.println("\n--- Código Gerado (" + sourceFile.getName() + ") ---");
                System.out.println(generatedCode);
                System.out.println("\n--- Código Esperado (" + expectedFile.getName() + ") ---");
                System.out.println(expectedCode);
            }
            
        } catch (Error e) {
           System.out.println("FALHA DE PARSING/SINTAXE INESPERADA: " + e.getMessage()); 
        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
        }
    }
    
    private static String readFileContents(File file) throws IOException {
        return new String(Files.readAllBytes(Paths.get(file.getPath()))).trim();
    }
}