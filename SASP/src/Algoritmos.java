import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Algoritmos de substituição de páginas.
 * <p>
 * Esta classe implementa os quatro algoritmos exigidos: FIFO, LRU, Ótimo e Relógio.
 * Cada método recebe a sequência de referências e o número de quadros e devolve
 * o número de faltas de página.
 * </p>
 */
public class Algoritmos {

    /**
     * Estrutura de resultado para os algoritmos.
     */
    public static class Resultado {
        public final String nome;
        public final int faltas;

        public Resultado(String nome, int faltas) {
            this.nome = nome;
            this.faltas = faltas;
        }

        @Override
        public String toString() {
            return nome + ": " + faltas + " faltas";
        }
    }

    /**
     * Executa todos os algoritmos e retorna os resultados em lista.
     *
     * @param paginas vetor de referências de página
     * @param quadros número de quadros de memória física
     * @return lista de resultados para FIFO, LRU, Ótimo e Relógio
     */
    public static List<Resultado> executarTodos(int[] paginas, int quadros) {
        List<Resultado> resultados = new ArrayList<>();
        resultados.add(fifo(paginas, quadros));
        resultados.add(lru(paginas, quadros));
        resultados.add(otimo(paginas, quadros));
        resultados.add(relogio(paginas, quadros));
        return resultados;
    }

    /**
     * Simula o algoritmo FIFO (First-In, First-Out).
     *
     * @param paginas vetor de referências de página
     * @param quadros número de quadros disponíveis
     * @return resultado com número de faltas de página
     */
    public static Resultado fifo(int[] paginas, int quadros) {
        Set<Integer> memoria = new HashSet<>();
        LinkedList<Integer> fila = new LinkedList<>();
        int faltas = 0;

        for (int pagina : paginas) {
            if (!memoria.contains(pagina)) {
                faltas++;
                if (memoria.size() == quadros) {
                    int removido = fila.removeFirst();
                    memoria.remove(removido);
                }
                fila.addLast(pagina);
                memoria.add(pagina);
            }
        }

        return new Resultado("FIFO", faltas);
    }

    /**
     * Simula o algoritmo LRU (Least Recently Used).
     *
     * @param paginas vetor de referências de página
     * @param quadros número de quadros disponíveis
     * @return resultado com número de faltas de página
     */
    public static Resultado lru(int[] paginas, int quadros) {
        List<Integer> frames = new ArrayList<>();
        int faltas = 0;

        for (int pagina : paginas) {
            if (frames.contains(pagina)) {
                frames.remove(Integer.valueOf(pagina));
                frames.add(pagina);
            } else {
                faltas++;
                if (frames.size() == quadros) {
                    frames.remove(0);
                }
                frames.add(pagina);
            }
        }

        return new Resultado("LRU", faltas);
    }

    /**
     * Simula o algoritmo Ótimo de substituição de páginas.
     *
     * @param paginas vetor de referências de página
     * @param quadros número de quadros disponíveis
     * @return resultado com número de faltas de página
     */
    public static Resultado otimo(int[] paginas, int quadros) {
        List<Integer> frames = new ArrayList<>();
        int faltas = 0;

        for (int i = 0; i < paginas.length; i++) {
            int pagina = paginas[i];
            if (!frames.contains(pagina)) {
                faltas++;
                if (frames.size() < quadros) {
                    frames.add(pagina);
                } else {
                    int indiceParaSubstituir = encontraPaginaMaisDistante(frames, paginas, i + 1);
                    frames.set(indiceParaSubstituir, pagina);
                }
            }
        }

        return new Resultado("Ótimo", faltas);
    }

    private static int encontraPaginaMaisDistante(List<Integer> frames, int[] paginas, int inicio) {
        int indiceSubstituir = -1;
        int distanciaMaisLonga = -1;

        for (int i = 0; i < frames.size(); i++) {
            int pagina = frames.get(i);
            int proximaOcorrencia = encontraProximaOcorrencia(pagina, paginas, inicio);
            if (proximaOcorrencia == -1) {
                return i;
            }
            if (proximaOcorrencia > distanciaMaisLonga) {
                distanciaMaisLonga = proximaOcorrencia;
                indiceSubstituir = i;
            }
        }

        return indiceSubstituir;
    }

    private static int encontraProximaOcorrencia(int pagina, int[] paginas, int inicio) {
        for (int j = inicio; j < paginas.length; j++) {
            if (paginas[j] == pagina) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Simula o algoritmo Relógio (Clock).
     *
     * @param paginas vetor de referências de página
     * @param quadros número de quadros disponíveis
     * @return resultado com número de faltas de página
     */
    public static Resultado relogio(int[] paginas, int quadros) {
        int[] frames = new int[quadros];
        boolean[] uso = new boolean[quadros];
        for (int i = 0; i < quadros; i++) {
            frames[i] = -1;
        }
        int ponteiro = 0;
        int faltas = 0;

        for (int pagina : paginas) {
            int posicao = encontraPaginaEmFrames(frames, pagina);
            if (posicao != -1) {
                uso[posicao] = true;
                continue;
            }

            faltas++;
            while (true) {
                if (frames[ponteiro] == -1) {
                    frames[ponteiro] = pagina;
                    uso[ponteiro] = true;
                    ponteiro = (ponteiro + 1) % quadros;
                    break;
                }
                if (!uso[ponteiro]) {
                    frames[ponteiro] = pagina;
                    uso[ponteiro] = true;
                    ponteiro = (ponteiro + 1) % quadros;
                    break;
                }
                uso[ponteiro] = false;
                ponteiro = (ponteiro + 1) % quadros;
            }
        }

        return new Resultado("Relógio", faltas);
    }

    private static int encontraPaginaEmFrames(int[] frames, int pagina) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == pagina) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Converte uma cadeia de páginas em um vetor de inteiros.
     *
     * @param entrada texto de páginas separado por vírgulas, espaços ou ponto-e-vírgula
     * @return vetor de inteiros representando as páginas
     * @throws NumberFormatException se a entrada contiver valores não inteiros
     */
    public static int[] parseSequencia(String entrada) {
        String[] tokens = entrada.trim().split("[\\s,;]+");
        List<Integer> valores = new ArrayList<>();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            valores.add(Integer.parseInt(token));
        }
        int[] paginas = new int[valores.size()];
        for (int i = 0; i < valores.size(); i++) {
            paginas[i] = valores.get(i);
        }
        return paginas;
    }
}
