package tests;

import lexer.Lexer;
import parser.Parser;
import intermediate.CodeBuffer;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestIntermediate {
    
    // Acessa apenas a pasta de testes válidos
    private static final String VALID_FOLDER = "src/tests/valids";
    private static final String EXPECTED_C3E_FOLDER = "src/tests/c3e_expected";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando Testes de Geração de Código Intermediário (C3E) ===");
        
        // Cria a pasta de referência se ela não existir
        new File(EXPECTED_C3E_FOLDER).mkdirs();

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
            
            // Define o nome do arquivo de referência (Ex: test1_valid.txt -> test1_valid.c3e)
            String expectedFileName = sourceFile.getName().replace(".txt", ".c3e");
            File expectedFile = new File(EXPECTED_C3E_FOLDER, expectedFileName);

            System.out.print(sourceFile.getName() + " -> ");
            runSingleC3ETest(sourceFile, expectedFile);
        }
    }
    
    private static void runSingleC3ETest(File sourceFile, File expectedFile) {
        CodeBuffer.clear(); // Limpa o buffer antes de cada teste
        
        try (FileReader fr = new FileReader(sourceFile)) {
            Lexer lexer = new Lexer(fr);
            Parser parser = new Parser(lexer); 
            
            // 1. Executa o parser, que chama gen() e emit()
            parser.program(); 
            
            String generatedCode = CodeBuffer.getCode();
            
            // 2. Se o arquivo esperado não existir, salva o gerado para criar a referência
            if (!expectedFile.exists()) {
                System.out.println("AVISO: Arquivo de referência C3E não encontrado.");
                Files.write(Paths.get(expectedFile.getPath()), generatedCode.getBytes());
                System.out.println("Gerado e salvo em: " + expectedFile.getName() + " para comparação futura.");
                return;
            }

            // 3. Compara o código gerado com o código esperado
            String expectedCode = readFileContents(expectedFile);

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
        // Lê o conteúdo do arquivo e remove espaços em branco/quebras de linha desnecessárias
        return new String(Files.readAllBytes(Paths.get(file.getPath()))).trim();
    }
}