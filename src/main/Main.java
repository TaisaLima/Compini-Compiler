package main;

import java.io.*;
import java.util.*;
import lexer.Lexer;
import parser.Parser;
import intermediate.*;
import symbol.*;

public class Main {

    public static void exibirMenu() {
        System.out.println("==============================================================");
        System.out.println("          COMPILADOR DA LINGUAGEM COMPINI      ");
        System.out.println("==============================================================");
        System.out.println("Escolha uma opção:");
        System.out.println("1. Inserir uma expressão manualmente");
        System.out.println("2. Executar testes automáticos");
        System.out.println("3. Sair");
        System.out.println("--------------------------------------------------------------");
        System.out.print("Digite sua escolha: ");
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

            // Front-end: gera AST internamente
            parser.program();
            System.out.println("Compilação bem-sucedida!");

            if (parser.getC3E() != null) {
                List<String> c3e = parser.getC3E();
                System.out.println("\n=== Código de 3 endereços ===");
                c3e.forEach(System.out::println);

              /**  // Back-end: gerar Assembly
                Codegen codegen = new Codegen();
                String asm = codegen.generate(String.join("\n", c3e));
                System.out.println("\n=== Código Assembly ===");
                System.out.println(asm);  */
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

        do {
            exibirMenu();
            while (!scanner.hasNextInt()) {
                System.out.println("Por favor, digite um número válido.");
                scanner.next();
            }
            opcao = scanner.nextInt();
            scanner.nextLine(); // consumir quebra de linha

            switch (opcao) {
                case 1 -> testarExpressaoManual();
                case 2 -> executarTestesProntos();
                case 3 -> System.out.println("Encerrando o compilador Compini... Até logo!");
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
            System.out.println();

        } while (opcao != 3);
    }

    // ------------------ NOVO MÉTODO PARA WEB ------------------
    public String compilarExpressaoParaWeb(String expressao) {
        Map<String, String> resultado = new HashMap<>();
        try {
            File arquivo = criarArquivoTemporario(expressao);
            Lexer lex = new Lexer(new FileReader(arquivo));
            Parser parser = new Parser(lex);

            parser.program(); // Gera AST internamente
            resultado.put("log", "Compilação bem-sucedida!");

            String c3e = "";
            String asm = "";
            if (parser.getC3E() != null) {
                List<String> c3eList = parser.getC3E();
                c3e = String.join("\n", c3eList);
            }

            resultado.put("c3e", c3e.isEmpty() ? "Nenhum código de 3 endereços gerado." : c3e);
            resultado.put("asm", asm.isEmpty() ? "Nenhum código assembly gerado." : asm);

        } catch (Exception e) {
            resultado.put("log", "Erro: " + e.getMessage());
            resultado.put("c3e", "");
            resultado.put("asm", "");
        }

        // Converte o Map para JSON manualmente (alternativa simples)
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<String, String> entry : resultado.entrySet()) {
            if (count++ > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":")
              .append("\"").append(entry.getValue().replace("\n", "\\n").replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
