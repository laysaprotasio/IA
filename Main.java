import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.util.*;

class Escala {

    enum DiaSemana {
        DOM, SEG, TER, QUA, QUI, SEX, SAB;
    }

    enum Turno {
        PRIMEIRO, SEGUNDO;
    }

    enum TipoServico {
        INCENDIO, SOCORRO, TELEFONE;
    }

    private Map<TipoServico, Map<DiaSemana, Map<Turno, String>>> alocacoes;

    public Escala() {
        alocacoes = new EnumMap<>(TipoServico.class);
        for (TipoServico servico : TipoServico.values()) {
            Map<DiaSemana, Map<Turno, String>> dias = new EnumMap<>(DiaSemana.class);
            for (DiaSemana dia : DiaSemana.values()) {
                Map<Turno, String> turnos = new EnumMap<>(Turno.class);
                for (Turno turno : Turno.values()) {
                    turnos.put(turno, null);
                }
                dias.put(dia, turnos);
            }
            alocacoes.put(servico, dias);
        }
    }

    public boolean alocar(TipoServico servico, DiaSemana dia, Turno turno, String nomeBombeiro) {
        if (bombeiroAlocado(nomeBombeiro, dia, turno)) {
            System.out.println("Conflito: o bombeiro " + nomeBombeiro + " já está escalado em " + dia + " " + turno);
            return false;
        }
        // Implementar verificação se o bombeiro tem disponibilidade
        alocacoes.get(servico).get(dia).put(turno, nomeBombeiro);
        return true;
    }

    public boolean bombeiroAlocado(String nomeBombeiro, DiaSemana dia, Turno turno) {
        for (TipoServico servico : TipoServico.values()) {
            String nome = alocacoes.get(servico).get(dia).get(turno);
            if (nome != null && nome.equals(nomeBombeiro)) {
                return true;
            }
        }
        return false;
    }

    public void imprimirEscalaServico(TipoServico servico) {
        System.out.println("Serviço: " + servico);

        for (DiaSemana dia : DiaSemana.values()) {
            System.out.printf("%-10s", dia);
        }
        System.out.println();

        for (Turno turno : Turno.values()) {
            for (DiaSemana dia : DiaSemana.values()) {
                String nome = alocacoes.get(servico).get(dia).get(turno);
                System.out.printf("%-10s", (nome != null ? nome : "-"));
            }
            System.out.println();
        }
    }

}

class GerenciadorEscala {

    public static void executarEscala(String caminhoArquivo) {
        List<Map<String, String>> registros = TratamentoArquivo.lerArquivoParaMapa(caminhoArquivo);

        Map<String, Map<Escala.TipoServico, Integer>> disponibilidade = new LinkedHashMap<>();
        for (Map<String, String> registro : registros) {
            String nome = registro.get("Bombeiro");
            if (nome == null || nome.isEmpty())
                continue;
            try {
                
                int inc = Integer.parseInt(registro.get("Incêncio"));
                int soc = Integer.parseInt(registro.get("Socorro"));
                int tel = Integer.parseInt(registro.get("Telefone"));

                Map<Escala.TipoServico, Integer> dispoServicos = new EnumMap<>(Escala.TipoServico.class);
                dispoServicos.put(Escala.TipoServico.INCENDIO, inc);
                dispoServicos.put(Escala.TipoServico.SOCORRO, soc);
                dispoServicos.put(Escala.TipoServico.TELEFONE, tel);

                disponibilidade.put(nome, dispoServicos);
            } catch (NumberFormatException e) {
                System.out.println("Erro ao converter disponibilidade do bombeiro " + nome);
            }
        }

        Escala escala = new Escala();

        for (Escala.TipoServico servico : Escala.TipoServico.values()) {
            for (Escala.DiaSemana dia : Escala.DiaSemana.values()) {
                for (Escala.Turno turno : Escala.Turno.values()) {
                    boolean alocado = false;
                    for (String nome : disponibilidade.keySet()) {
                        int disponivel = disponibilidade.get(nome).get(servico);
                        if (disponivel > 0) {
                            if (!escala.bombeiroAlocado(nome, dia, turno)) {
                                boolean sucesso = escala.alocar(servico, dia, turno, nome);
                                if (sucesso) {
                                    disponibilidade.get(nome).put(servico, disponivel - 1);
                                    alocado = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!alocado) {
                        escala.alocar(servico, dia, turno, "SEM ALOCACAO");
                    }
                }
            }
        }

        for (Escala.TipoServico servico : Escala.TipoServico.values()) {
            escala.imprimirEscalaServico(servico);
            System.out.println();
        }
    }
}

class TratamentoArquivo {

    /**
     *
     * @param caminhoArquivo O caminho do arquivo a ser lido.
     * @return Uma lista de mapas, onde cada mapa representa um registro do arquivo.
     */
    public static List<Map<String, String>> lerArquivoParaMapa(String caminhoArquivo) {
        List<Map<String, String>> registros = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {

            String linhaCabecalho = br.readLine();
            if (linhaCabecalho == null) {
                System.out.println("Arquivo vazio.");
                return registros;
            }

            String[] chaves = linhaCabecalho.split(",");

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] valores = linha.split(",");
                Map<String, String> registro = new LinkedHashMap<>();

                for (int i = 0; i < chaves.length; i++) {
                    String valor = i < valores.length ? valores[i] : "";
                    registro.put(chaves[i], valor);
                }
                registros.add(registro);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return registros;
    }

}

public class Main {
    public static void main(String[] args) {
        String caminhoArquivo = "C:/Users/Laysa/Documents/IA/Bombeiros/entrada_1.txt";
        GerenciadorEscala.executarEscala(caminhoArquivo);
    }

}