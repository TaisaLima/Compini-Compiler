package main;

import java.io.*;
import java.util.*;
import main.lexer.*;
import main.parser.*;
import main.codegen.*;
import main.runtime.server.*;
import main.semantic.*;

public class Main {

    public static void InitializeServer () {
        CompiniServer server = new CompiniServer();
        new Thread(() -> {
            try {
                server.run(); // ou o método correto do seu servidor
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private static File criarArquivoTemporario(String expressao) throws IOException {
        File temp = File.createTempFile("expr", ".txt");
        temp.deleteOnExit();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            bw.write(expressao);
        }
        return temp;
    }

    public static void compilarExpressao(String expressao) {
        try {
            File arquivo = criarArquivoTemporario(expressao);
            Lexer lex = new Lexer(new FileReader(arquivo));
            Parser parser = new Parser(lex);

            parser.parse();
            System.out.println("Compilação bem-sucedida!");

            if (parser.getC3E() != null) {
                List<String> c3e = parser.getC3E();
                System.out.println("\n=== Código de 3 endereços ===");
                c3e.forEach(System.out::println);

                Codegen codegen = new Codegen();
                String asm = codegen.generate(String.join("\n", c3e));
                System.out.println("\n=== Código Assembly ===");
                System.out.println(asm);
            }

        } catch (Exception e) {
            System.out.println("Erro durante a compilação: " + e.getMessage());
        }
    }

    public static void testarExpressaoManual() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nDigite a expressão aritmética que deseja compilar:");
        String expressao = scanner.nextLine();

        if (expressao.trim().isEmpty()) {
            System.out.println("Expressão vazia! Tente novamente.");
            return;
        }

        compilarExpressao(expressao);
    }

    public static void executarTestesProntos() {
        System.out.println("\nSelecione um teste pré-definido:");
        System.out.println("1. { int a; a = 5; print(a); }");
        System.out.println("2. { int x, y; x = 10; y = x + 2; print(y); }");
        System.out.println("3. 7 - 4 + 2");
        System.out.println("4. Retornar ao menu principal");
        System.out.print("Opção: ");

        Scanner scanner = new Scanner(System.in);
        int opcao = scanner.nextInt();
        scanner.nextLine();

        String expressao = switch (opcao) {
            case 1 -> "{ int a; a = 5; print(a); }";
            case 2 -> "{ int x, y; x = 10; y = x + 2; print(y); }";
            case 3 -> "7 - 4 + 2";
            case 4 -> { System.out.println(); yield null; }
            default -> { System.out.println("Opção inválida!"); yield null; }
        };

        if (expressao == null) return;

        System.out.println("\nCompilando expressão: " + expressao);
        compilarExpressao(expressao);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcao;
    }

    public String compilarExpressaoParaWeb(String expressao) {
        Map<String, String> resultado = new HashMap<>();

        try {
            // Cria arquivo temporário com o código recebido do front-end
            File arquivo = criarArquivoTemporario(expressao);

            // Instancia o lexer e o parser
            Lexer lex = new Lexer(new FileReader(arquivo));
            Parser parser = new Parser(lex);

            // Executa a compilação
            parser.parse();
            resultado.put("log", "Compilação bem-sucedida!");

            // Recupera código intermediário e assembly
            String c3e = "";
            String asm = "";

            if (parser.getC3E() != null) {
                List<String> c3eList = parser.getC3E();
                c3e = String.join("\n", c3eList);

                Codegen codegen = new Codegen();
                asm = codegen.generate(c3e);
            }

            resultado.put("c3e", c3e.isEmpty() ? "Nenhum código de 3 endereços gerado." : c3e);
            resultado.put("asm", asm.isEmpty() ? "Nenhum código assembly gerado." : asm);

        } catch (Exception e) {
            resultado.put("log", "Erro: " + e.getMessage());
            resultado.put("c3e", "");
            resultado.put("asm", "");
        }

        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : resultado.entrySet()) {
            if (i++ > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\": \"")
                .append(escapeJson(entry.getValue())).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escapa caracteres especiais para JSON válido.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }


}
