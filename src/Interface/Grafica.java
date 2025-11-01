package Interface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import java.util.*;
import java.util.List;

import lexer.Lexer;
import parser.Parser;
import intermediate.*;

public class Grafica extends JFrame {
    //Espaço para escrever o código
    private JTextArea input;
    //Espaço para cuspir o código impresso
    private JTextArea statusOutput;
    private JTextArea c3eOutput;
    private JTextArea assemblyOutput;

    private final JFrame parentFrame =this;

    // --- Estrutura de Dados de Resultado (Interna) ---
    private static class CompilacaoResultado {
        String status;
        String c3e;
        String assembly;

        public CompilacaoResultado(String status, String c3e, String assembly) {
            this.status = status;
            this.c3e = c3e;
            this.assembly = assembly;
        }
    }

    public Grafica() {
        // 1. Configura a janela
        setTitle("Interface do Compilador Compini");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout()); // Define o layout manager

        // 2. Cria um componente (botão)
        createComponentes();

        // 5. Torna a janela visível
        setVisible(true);
    }

    private void createComponentes() {
        // Área de Input (Expressão)
        input = new JTextArea(10, 50);
        input.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // Painel de Input
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Expressão de Entrada COMPINI:"));
        inputPanel.add(new JScrollPane(input), BorderLayout.CENTER);

        // Áreas de Output (usando JTabbedPane para organização)
        statusOutput = createOutputArea();
        c3eOutput = createOutputArea();
        assemblyOutput = createOutputArea();

        JTabbedPane outputTabbedPane = new JTabbedPane();
        outputTabbedPane.addTab("Status/Log", new JScrollPane(statusOutput));
        outputTabbedPane.addTab("Código 3 Endereços", new JScrollPane(c3eOutput));
        outputTabbedPane.addTab("Assembly", new JScrollPane(assemblyOutput));
        outputTabbedPane.setPreferredSize(new Dimension(800, 300));

        // JSplitPane para dividir o Input e o Output verticalmente
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputTabbedPane);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3); // Ocupa 30% da tela para input

        add(splitPane, BorderLayout.CENTER);

        // Painel de Botões (Topo da Janela)

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        //Criação de cada Botão:
        JButton compile = new JButton("1. Compilar Expressão Manualmente");
        compile.addActionListener(e -> compilarAcao(input.getText()));

        JButton button = new JButton("2. Executar Testes Prontos");
        button.addActionListener(e -> {
            new TestesProntosDialog(parentFrame, this).setVisible(true);
        });
        //Ainda está incompleto o button para outra janela :)
        JButton exitButton = new JButton("3. Sair");
        exitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame, "Encerrando o compilador Compini... Até logo!");
            System.exit(0);
        });

        buttonPanel.add(compile);
        buttonPanel.add(button);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.NORTH);
    }

    private JTextArea createOutputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return area;
    }

    public void atualizarOutput(CompilacaoResultado resultado) {
        statusOutput.setText(resultado.status);
        c3eOutput.setText(resultado.c3e);
        assemblyOutput.setText(resultado.assembly);

        // Muda para a aba de status/log (o primeiro componente dentro do JSplitPane central)
        JSplitPane splitPane = (JSplitPane) this.getContentPane().getComponent(1);
        JTabbedPane tabbedPane = (JTabbedPane) splitPane.getBottomComponent();
        tabbedPane.setSelectedIndex(0);
    }

    public void compilarAcao(String expressao) {
        if (expressao.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "A expressão não pode ser vazia.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CompilacaoResultado resultado = compilarExpressaoGUI(expressao);
        atualizarOutput(resultado);
    }

    private CompilacaoResultado compilarExpressaoGUI(String expressao) {
        String status = "";
        String c3e = "";
        String assembly = "";

        try {
            // 1. Cria o arquivo temporário (igual ao seu main original)
            File arquivo = criarArquivoTemporario(expressao);

            // ==========================================================
            //       INTEGRE SEU CÓDIGO AQUI
            // ==========================================================

            // Exemplo de integração (descomente e ajuste):
            Lexer lex = new Lexer(new FileReader(arquivo));
            Parser parser = new Parser(lex);
            parser.program(); // Front-end

            status = "Compilação bem-sucedida! \nExpressão: " + expressao;

            if (parser.getC3E() != null) {
                List<String> listaC3E = parser.getC3E();
                c3e = String.join("\n", listaC3E);

                Codegen codegen = new Codegen();
                assembly = codegen.generate(c3e);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Algo estranho aconteceu na compilação.");
            status = "Erro durante a compilação:\n" + e.getMessage() + "\n\nExpressão: " + expressao;
            c3e = "Nenhum código C3E gerado devido ao erro.";
            assembly = "Nenhum código Assembly gerado devido ao erro.";
        }

        return new CompilacaoResultado(status, c3e, assembly);
    }

    // Método auxiliar (do seu Main original)
    private static File criarArquivoTemporario(String expressao) throws IOException {
        File temp = File.createTempFile("expr", ".txt");
        temp.deleteOnExit();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            bw.write(expressao);
        }
        return temp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Grafica();
            }
        });
        /*
        SwingUtilities.invokeLater(()-> new Grafica());
         */
    }
}
