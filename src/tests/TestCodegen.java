package tests;

import lexer.Lexer;
import parser.Parser;
import intermediate.CodeBuffer;
import intermediate.Codegen;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestCodegen {

    private static final String VALID_FOLDER = "src/tests/valids";
    private static final String ASM_EXPECTED_FOLDER = "src/tests/asm_expected";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando Testes de Geração de Código Assembly (ARMv7) ===\n");

        Files.createDirectories(Paths.get(ASM_EXPECTED_FOLDER));

        File[] sourceFiles = new File(VALID_FOLDER).listFiles((dir, name) -> name.endsWith(".txt"));
        if (sourceFiles == null || sourceFiles.length == 0) {
            System.err.println("Nenhum arquivo de teste encontrado em " + VALID_FOLDER);
            return;
        }

        for (File srcFile : sourceFiles) {
            runTest(srcFile);
        }

        System.out.println("\n=== Testes Finalizados ===");
    }

    private static void runTest(File srcFile) throws Exception {
        System.out.println(">> Testando: " + srcFile.getName());

        CodeBuffer.clear(); 

        try (FileReader fr = new FileReader(srcFile)) {
            Lexer lexer = new Lexer(fr);
            Parser parser = new Parser(lexer);
            parser.program(); 
        } catch (Exception e) {
            System.err.println("Erro durante parsing: " + e.getMessage());
            return;
        }

        String c3e = CodeBuffer.getCode().trim();
        if (c3e.isEmpty()) {
            System.err.println("⚠️ Nenhum código intermediário gerado!");
            return;
        }

        Codegen codegen = new Codegen();
        String asm = codegen.generate(c3e);


        Path expectedPath = Paths.get(ASM_EXPECTED_FOLDER,
                srcFile.getName().replace(".txt", ".s"));

        if (!Files.exists(expectedPath)) {
            System.out.println("⚠️ Arquivo esperado não encontrado. Gerando novo arquivo de referência...");
            Files.write(expectedPath, asm.getBytes());
            System.out.println("Arquivo salvo em: " + expectedPath);
        } else {
            String expectedAsm = Files.readString(expectedPath).trim();
            if (asm.equals(expectedAsm)) {
                System.out.println("SUCESSO: ASM corresponde ao esperado.\n");
            } else {
                System.out.println("FALHA: ASM diferente do esperado.\n");
                System.out.println("--- ASM GERADO ---");
                System.out.println(asm);
                System.out.println("\n--- ASM ESPERADO ---");
                System.out.println(expectedAsm);
            }
        }
    }
}
