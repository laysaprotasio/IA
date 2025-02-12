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

    public static Map<TipoServico, Map<DiaSemana, Map<Turno, String>>> alocacoes;

    private static class Momento {
        TipoServico servico;
        DiaSemana dia;
        Turno turno;

        public Momento(TipoServico servico, DiaSemana dia, Turno turno) {
            this.servico = servico;
            this.dia = dia;
            this.turno = turno;
        }
    }

    private static Momento proximoMomento(TipoServico servico, DiaSemana dia, Turno turno) {
        Turno[] turnos = Turno.values();
        DiaSemana[] dias = DiaSemana.values();
        TipoServico[] servicos = TipoServico.values();

        int indiceTurno = turno.ordinal();
        int indiceDia = dia.ordinal();
        int indiceServico = servico.ordinal();

        if (indiceTurno < turnos.length - 1) {
            return new Momento(servico, dia, turnos[indiceTurno + 1]);
        } else {
            if (indiceDia < dias.length - 1) {
                return new Momento(servico, dias[indiceDia + 1], turnos[0]);
            } else {
                if (indiceServico < servicos.length - 1) {
                    return new Momento(servicos[indiceServico + 1], dias[0], turnos[0]);
                } else {
                    return null;
                }
            }
        }
    }

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

    public static boolean alocar(TipoServico servico, DiaSemana dia, Turno turno, String nomeBombeiro) {
        alocacoes.get(servico).get(dia).put(turno, nomeBombeiro);
        return true;
    }

    public static boolean desalocar(TipoServico servico, DiaSemana dia, Turno turno, String nomeBombeiro) {
        alocacoes.get(servico).get(dia).remove(turno, nomeBombeiro);
        return true;
    }

    public static boolean bombeiroAlocado(String nomeBombeiro, DiaSemana dia, Turno turno) {
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

    public static Map<String, Map<Escala.TipoServico, Integer>> lerDisponibilidades(String caminhoArquivo) {
        Map<String, Map<Escala.TipoServico, Integer>> disponibilidade = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linhaCabecalho = br.readLine();
            if (linhaCabecalho == null) {
                System.out.println("Arquivo vazio.");
                return disponibilidade;
            }

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] valores = linha.split(",");
                if (valores.length < 4) {
                    continue;
                }

                String nome = valores[0].trim();
                if (nome.isEmpty()) {
                    continue;
                }

                try {
                    int inc = Integer.parseInt(valores[1].trim());
                    int soc = Integer.parseInt(valores[2].trim());
                    int tel = Integer.parseInt(valores[3].trim());

                    Map<Escala.TipoServico, Integer> dispoServicos = new EnumMap<>(Escala.TipoServico.class);
                    dispoServicos.put(Escala.TipoServico.INCENDIO, inc);
                    dispoServicos.put(Escala.TipoServico.SOCORRO, soc);
                    dispoServicos.put(Escala.TipoServico.TELEFONE, tel);

                    disponibilidade.put(nome, dispoServicos);
                } catch (NumberFormatException e) {
                    System.out.println("Erro ao converter disponibilidade do bombeiro " + nome);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return disponibilidade;
    }

    public static Map<TipoServico, Map<DiaSemana, Map<Turno, String>>> backtracking(
            Map<String, Map<Escala.TipoServico, Integer>> disponibilidade,
            Map<TipoServico, Map<DiaSemana, Map<Turno, String>>> alocacoes, TipoServico servico, DiaSemana dia,
            Turno turno) {

        for (String nome : disponibilidade.keySet()) {
            int disponivel = disponibilidade.get(nome).get(servico);
            if (disponivel > 0 && !bombeiroAlocado(nome, dia, turno)) {
                alocar(servico, dia, turno, nome);
                disponibilidade.get(nome).put(servico, disponivel - 1);
                Momento proximo = proximoMomento(servico, dia, turno);
                if (proximo == null) {
                    return alocacoes;
                }
                Map<TipoServico, Map<DiaSemana, Map<Turno, String>>> alocado = backtracking(disponibilidade, alocacoes,
                        proximo.servico, proximo.dia, proximo.turno);
                if (alocado != null) {
                    return alocado;
                } else {
                    desalocar(servico, dia, turno, nome);
                    disponibilidade.get(nome).put(servico, disponivel);
                }
            }
        }
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        String caminhoArquivo = "C:/Users/Laysa/Documents/IA/Bombeiros/entrada_1.txt";

        Map<String, Map<Escala.TipoServico, Integer>> disponibilidade = Escala.lerDisponibilidades(caminhoArquivo);

        Escala escala = new Escala();

        Map<Escala.TipoServico, Map<Escala.DiaSemana, Map<Escala.Turno, String>>> resultado = Escala.backtracking(
                disponibilidade, Escala.alocacoes,
                Escala.TipoServico.INCENDIO,
                Escala.DiaSemana.DOM,
                Escala.Turno.PRIMEIRO);

        if (resultado != null) {
            for (Escala.TipoServico servico : Escala.TipoServico.values()) {
                escala.imprimirEscalaServico(servico);
                System.out.println();
            }
        } else {
            System.out.println("Não foi possível completar a alocação com backtracking.");
        }
    }
}
