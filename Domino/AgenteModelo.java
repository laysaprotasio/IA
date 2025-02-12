import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class AgenteBaseadoEmModelo implements Agente {

    static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Set<String> pecasAdversario = new HashSet<>();
    private final Set<String> maoInicial = new HashSet<>();
    private boolean primeiraRodada = true;

    @Override
    public int indicePecaParaJogar(String mesa, String mao) {
        String[] maos = mao.split("<");

        char pontaEsquerda = mesa.charAt(2);
        char pontaDireita = mesa.charAt(mesa.length() - 3);

        if (primeiraRodada) {
            registrarMaoInicial(maos);
            primeiraRodada = false;
        }

        registrarJogadasAdversario(mesa, maos);

        List<String> pecasPossiveis = definirPecasPossiveis(maos, pontaEsquerda, pontaDireita);

        if (pecasPossiveis.isEmpty()) {
            return 0;
        }

        CompletableFuture<Map<String, Integer>> pontuacoesFuturo = CompletableFuture.supplyAsync(() -> calcularPontuacaoPecas(pecasPossiveis, mesa), executor);
        CompletableFuture<List<Character>> ladosVisiveisFuturo = CompletableFuture.supplyAsync(() -> obterLadosVisiveis(mesa), executor);

        try {
            Map<String, Integer> pontuacoes = pontuacoesFuturo.get(60, TimeUnit.MILLISECONDS);
            List<Character> ladosVisiveis = ladosVisiveisFuturo.get(60, TimeUnit.MILLISECONDS);

            String melhorPeca = pontuacoes.entrySet().stream()
                    .max(Comparator.comparingInt(e -> calcularPontuacaoFinal(e.getKey(), e.getValue(), ladosVisiveis)))
                    .map(Map.Entry::getKey)
                    .orElse(pecasPossiveis.get(0));

            return Arrays.asList(maos).indexOf(melhorPeca) - 1;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        return Arrays.asList(maos).indexOf(pecasPossiveis.get(0)) - 1;
    }

    private void registrarMaoInicial(String[] maos) {
        for (int i = 1; i < maos.length; i++) {
            maoInicial.add(maos[i]);
        }
    }

    private void registrarJogadasAdversario(String mesa, String[] maos) {
        List<String> pecasVisiveis = Arrays.asList(mesa.split("<"));

        for (String peca : pecasVisiveis) {
            if (!peca.isEmpty() && !maoInicial.contains(peca) && !Arrays.asList(maos).contains(peca) && !pecasAdversario.contains(peca)) {
                registrarPecaAdversario(peca);
            }
        }
    }

    private List<String> definirPecasPossiveis(String[] maos, char pontaEsquerda, char pontaDireita) {
        List<String> pecasPossiveis = new ArrayList<>();
        for (int i = 1; i < maos.length; i++) {
            String peca = maos[i];
            if (pecaEncaixa(peca, pontaEsquerda) || pecaEncaixa(peca, pontaDireita)) {
                pecasPossiveis.add(peca);
            }
        }
        return pecasPossiveis;
    }

    private boolean pecaEncaixa(String peca, char ponta) {
        return peca.charAt(0) == ponta || peca.charAt(2) == ponta;
    }

    private Map<String, Integer> calcularPontuacaoPecas(List<String> pecasPossiveis, String mesa) {
        Map<String, Integer> pontuacoes = new HashMap<>();
        List<Character> ladosVisiveis = obterLadosVisiveis(mesa);

        for (String peca : pecasPossiveis) {
            int pontuacao = calcularPontuacaoAvancada(peca, ladosVisiveis);
            pontuacoes.put(peca, pontuacao);
        }

        return pontuacoes;
    }

    private int calcularPontuacaoAvancada(String peca, List<Character> ladosVisiveis) {
        int pontuacao = 0;

        if (peca.charAt(0) == peca.charAt(2)) {
            pontuacao += 10;
        }

        for (char lado : new char[]{peca.charAt(0), peca.charAt(2)}) {
            pontuacao += Collections.frequency(ladosVisiveis, lado);
        }

        for (char lado : new char[]{peca.charAt(0), peca.charAt(2)}) {
            if (pecasAdversario.contains(String.valueOf(lado))) {
                pontuacao -= 5;
            }
        }

        return pontuacao;
    }

    private int calcularPontuacaoFinal(String peca, int pontuacaoBase, List<Character> ladosVisiveis) {
        int valorPeca = calcularValorPeca(peca);

        return pontuacaoBase * 2 - valorPeca + calcularPontuacaoAvancada(peca, ladosVisiveis);
    }

    private int calcularValorPeca(String peca) {
        return Character.getNumericValue(peca.charAt(0)) + Character.getNumericValue(peca.charAt(2));
    }

    private List<Character> obterLadosVisiveis(String mesa) {
        return mesa.chars()
                .filter(Character::isDigit)
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
    }

    public void registrarPecaAdversario(String peca) {
        pecasAdversario.add(peca);
    }
}
