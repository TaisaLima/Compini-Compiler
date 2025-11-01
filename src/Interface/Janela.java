package Interface;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

// JDialog para seleção dos testes pré-definidos (Segunda Janela)
class TestesProntosDialog extends JDialog {

    private final Map<String, String> testes;

    public TestesProntosDialog(JFrame owner, Grafica gui) {
        // 'owner' é a janela CompiladorGUI, 'true' torna-a modal (trava a janela pai)
        super(owner, "Seleção de Testes Prontos", true);

        // Define os testes do seu main original
        testes = new LinkedHashMap<>();
        testes.put("Teste 1: { int a; a = 5; print(a); }", "{ int a; a = 5; print(a); }");
        testes.put("Teste 2: { int x, y; x = 10; y = x + 2; print(y); }", "{ int x, y; x = 10; y = x + 2; print(y); }");
        testes.put("Teste 3: 7 - 4 + 2", "7 - 4 + 2");

        // 1. Configuração da Janela
        setSize(500, 300);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // 2. Criação dos Componentes
        String[] opcoes = testes.keySet().toArray(new String[0]);
        JList<String> listaTestes = new JList<>(opcoes);
        listaTestes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaTestes.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton selecionarButton = new JButton("Executar Teste");
        JButton fecharButton = new JButton("Retornar ao Menu Principal");

        // 3. Adiciona Listeners
        selecionarButton.addActionListener(e -> {
            String selecionado = listaTestes.getSelectedValue();
            if (selecionado != null) {
                String expressao = testes.get(selecionado);

                // Chama o método de compilação da GUI principal (MUITO IMPORTANTE!)
                gui.compilarAcao(expressao);
                dispose(); // Fecha esta JDialog
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um teste.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        fecharButton.addActionListener(e -> dispose());

        // 4. Monta a Janela
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(selecionarButton);
        buttonPanel.add(fecharButton);

        add(new JLabel("Selecione um teste pré-definido:", SwingConstants.CENTER), BorderLayout.NORTH);
        add(new JScrollPane(listaTestes), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
