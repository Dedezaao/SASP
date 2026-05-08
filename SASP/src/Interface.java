import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Interface gráfica avançada do simulador de substituição de páginas (SASP).
 * Utiliza desenho customizado em Graphics2D para um dashboard moderno.
 */
public class Interface extends JFrame {

    private final JTextField sequenciaField;
    private final JSpinner quadrosSpinner;
    private final JTextArea resultadosArea;
    private final GraficoBarraPanel graficoPanel;

    public Interface() {
        super("SASP - Simulador Analítico de Substituição de Páginas");
        
        // Tenta usar o visual nativo do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600); // Janela mais larga para o gráfico respirar
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        
        // Adiciona um padding geral na janela
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- PAINEL ESQUERDO (CONTROLES E TEXTO) ---
        JPanel painelEsquerdo = new JPanel(new BorderLayout(10, 15));
        painelEsquerdo.setPreferredSize(new Dimension(300, 0));

        // Sub-painel de Inputs
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Configurações", 
                0, 0, new Font("SansSerif", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Cadeia de Referência:"), gbc);

        gbc.gridy = 1;
        sequenciaField = new JTextField("7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2");
        sequenciaField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputPanel.add(sequenciaField, gbc);

        gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantidade de Quadros na RAM:"), gbc);

        gbc.gridy = 3;
        quadrosSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        quadrosSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(quadrosSpinner, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(15, 8, 8, 8);
        JButton calcularButton = new JButton("Executar Simulação");
        calcularButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        calcularButton.setBackground(new Color(66, 133, 244));
        calcularButton.setForeground(Color.BLACK);
        calcularButton.setFocusPainted(false);
        calcularButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inputPanel.add(calcularButton, gbc);

        painelEsquerdo.add(inputPanel, BorderLayout.NORTH);

        // Sub-painel de Resultados de Texto
        resultadosArea = new JTextArea();
        resultadosArea.setEditable(false);
        resultadosArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultadosArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollResultados = new JScrollPane(resultadosArea);
        scrollResultados.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Relatório Numérico", 
                0, 0, new Font("SansSerif", Font.BOLD, 12)
        ));
        painelEsquerdo.add(scrollResultados, BorderLayout.CENTER);

        add(painelEsquerdo, BorderLayout.WEST);

        // --- PAINEL DIREITO (GRÁFICO) ---
        graficoPanel = new GraficoBarraPanel();
        graficoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Análise de Desempenho (Faltas de Página)", 
                0, 0, new Font("SansSerif", Font.BOLD, 12)
        ));
        add(graficoPanel, BorderLayout.CENTER);

        // --- EVENTOS ---
        calcularButton.addActionListener(e -> executarSimulacao());
    }

    private void executarSimulacao() {
        String entrada = sequenciaField.getText();
        int quadros = (Integer) quadrosSpinner.getValue();

        try {
            int[] paginas = Algoritmos.parseSequencia(entrada);
            if (paginas.length == 0) {
                throw new IllegalArgumentException("A sequência deve conter ao menos uma página.");
            }

            // Chama a classe Algoritmos (que você já tem no outro arquivo)
            List<Algoritmos.Resultado> resultados = Algoritmos.executarTodos(paginas, quadros);
            
            graficoPanel.setResultados(resultados);
            atualizarResultados(resultados);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: Use apenas números inteiros separados por vírgulas.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarResultados(List<Algoritmos.Resultado> resultados) {
        StringBuilder sb = new StringBuilder();
        sb.append(" STATUS DA MEMÓRIA\n");
        sb.append("----------------------------\n");
        for (Algoritmos.Resultado r : resultados) {
            sb.append(String.format(" %-10s : %3d faltas\n", r.nome, r.faltas));
        }
        sb.append("----------------------------\n");
        sb.append("\n * Menos faltas indica um \n   melhor aproveitamento da \n   Memória Virtual.");
        resultadosArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Interface().setVisible(true));
    }

    // =========================================================================
    // INNER CLASS: O Gráfico Customizado (FRONT-END RAIZ)
    // =========================================================================
    class GraficoBarraPanel extends JPanel {
        private List<Algoritmos.Resultado> resultados = new ArrayList<>();
        private final Color[] CORES = {
            new Color(66, 133, 244),  // Azul Google
            new Color(52, 168, 83),   // Verde Google
            new Color(251, 188, 5),   // Amarelo Google
            new Color(234, 67, 53)    // Vermelho Google
        };

        public void setResultados(List<Algoritmos.Resultado> resultados) {
            this.resultados = resultados;
            repaint(); // Pede para a UI redesenhar o painel com os novos dados
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (resultados == null || resultados.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            // Liga o Antialiasing para deixar as fontes e linhas suaves
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 50; // Margem interna do gráfico

            // Acha o valor máximo para escalar as barras
            int maxFaltas = resultados.stream().mapToInt(r -> r.faltas).max().orElse(1);
            // Adiciona uma "folga" no topo para o gráfico não bater no teto
            int limiteEixoY = maxFaltas + (maxFaltas / 5) + 1; 

            // Desenha linhas de grade horizontais
            g2d.setColor(new Color(220, 220, 220));
            int numLinhas = 5;
            for (int i = 0; i <= numLinhas; i++) {
                int y = height - padding - (i * (height - padding * 2) / numLinhas);
                g2d.drawLine(padding, y, width - padding, y);
                
                // Textos do Eixo Y
                g2d.setColor(Color.GRAY);
                String valorEixo = String.valueOf((limiteEixoY * i) / numLinhas);
                g2d.drawString(valorEixo, padding - 30, y + 5);
                g2d.setColor(new Color(220, 220, 220)); // Volta a cor pra linha
            }

            // Desenha as barras verticais
            int barWidth = (width - padding * 2) / (resultados.size() * 2);
            int spacing = (width - padding * 2) / resultados.size();

            for (int i = 0; i < resultados.size(); i++) {
                Algoritmos.Resultado res = resultados.get(i);
                
                // Calcula altura proporcional
                int barHeight = (int) (((double) res.faltas / limiteEixoY) * (height - padding * 2));
                int x = padding + (i * spacing) + (spacing / 2) - (barWidth / 2);
                int y = height - padding - barHeight;

                // Desenha a sombra da barra
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRect(x + 4, y + 4, barWidth, barHeight);

                // Desenha a barra colorida
                g2d.setColor(CORES[i % CORES.length]);
                g2d.fillRect(x, y, barWidth, barHeight);

                // Borda da barra
                g2d.setColor(CORES[i % CORES.length].darker());
                g2d.drawRect(x, y, barWidth, barHeight);

                // Texto embaixo (Nome do Algoritmo)
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(res.nome);
                g2d.drawString(res.nome, x + (barWidth / 2) - (textWidth / 2), height - padding + 20);

                // Texto em cima (Quantidade de faltas)
                g2d.setColor(Color.BLACK);
                String valorStr = String.valueOf(res.faltas);
                int valWidth = fm.stringWidth(valorStr);
                g2d.drawString(valorStr, x + (barWidth / 2) - (valWidth / 2), y - 10);
            }
        }
    }
}