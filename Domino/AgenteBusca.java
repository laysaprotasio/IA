import java.util.*;

class AgenteBaseadoEmBusca implements Agente {

    @Override
    public int indicePecaParaJogar(String mesa, String mao) {
        String[] pecasMao = mao.split("<");
        if (pecasMao.length <= 1)
            return -1;

        List<Peca> pecas = new ArrayList<Peca>();
        for (int i = 1; i < pecasMao.length; i++) {
            int lado1 = Character.getNumericValue(pecasMao[i].charAt(0));
            int lado2 = Character.getNumericValue(pecasMao[i].charAt(2));
            pecas.add(new Peca(lado1, lado2));
        }

        int melhorIndice = -1;
        Double melhorValor = - Double.MAX_VALUE;

        for (int i = 0; i < pecas.size(); i++) {
            
            Peca peca = pecas.get(i);
            if (pecaEncaixaNaMesa(peca, mesa)) {
                if (pecas.size() == 7) {
                    return i;
                }
                List<Peca> novaMao = new ArrayList<Peca>(pecas);
                novaMao.remove(peca);
                String novaMesa = atualizarMesa(mesa, peca);

                int qtdOponente = calcularQtdPecasOponente(mesa, pecas);
                List<Peca> listaOponente = obterListaOponente(mesa, pecas);

                Double valor = buscaMinimax(novaMesa, novaMao, false, listaOponente, qtdOponente, false);
            
                if (valor > melhorValor) {
                    melhorValor = valor;
                    melhorIndice = i;
                }
            }
        }

    

        return melhorIndice;
    }

    private Double buscaMinimax(String mesa, List<Peca> mao, boolean turno, List<Peca> listaOponente, int qtdOponente, boolean empate) {

        if (mao.size() <= 1 || qtdOponente <= 1) {
            return avaliarEstado(mesa, mao, listaOponente);
        }

        boolean falta = true;

        Double valorMini = Double.MAX_VALUE;
        if (turno == false) {
            for (Peca peca : listaOponente) {
                List<Peca> novaListaOponente = new ArrayList<Peca>(listaOponente);
                if (pecaEncaixaNaMesa(peca, mesa)) {
                    falta = false;
                    String novaMesa = atualizarMesa(mesa, peca);
                    novaListaOponente.remove(peca);
                    Double valorMiniTemp = buscaMinimax(novaMesa, mao, true, novaListaOponente, qtdOponente - 1, false);
                    if (valorMiniTemp < valorMini) {
                        valorMini = valorMiniTemp;
                    }
                }
            }
            if (falta) {
                if (empate){
                    return 0.5;
                }
                valorMini = buscaMinimax(mesa, mao, true, listaOponente, qtdOponente, true);
            }
            return valorMini;
        }

        Double valorMax = - Double.MAX_VALUE;
        if (turno == true) {
            for (Peca peca : mao) {
                List<Peca> novaMao = new ArrayList<Peca>(mao);
                if (pecaEncaixaNaMesa(peca, mesa)) {
                    falta = false;
                    String novaMesa1 = atualizarMesa(mesa, peca);
                    novaMao.remove(peca);
                    Double valorMaxTemp = buscaMinimax(novaMesa1, novaMao, false, listaOponente, qtdOponente, false);
                    if (valorMaxTemp > valorMax) {
                        valorMax = valorMaxTemp;
                    }
                }
            }
            if (falta) {
                if (empate){
                    return 0.5;
                }
                valorMax = buscaMinimax(mesa, mao, false, listaOponente, qtdOponente, true);
            }
          
            return valorMax;
        }

        return 0.0;

    }

    private String atualizarMesa(String mesa, Peca peca) {
        char op1 = mesa.charAt(2);
        char op2 = mesa.charAt(mesa.length() - 3);

        if (peca.lado2 == Character.getNumericValue(op1)) {
            return "[<" + peca.lado1 + "." + peca.lado2 + ">, " + mesa.substring(1);
        } else if (peca.lado1 == Character.getNumericValue(op1)) {
            return "[<" + peca.lado2 + "." + peca.lado1 + ">, " + mesa.substring(1);
        }

        if (peca.lado1 == Character.getNumericValue(op2)) {
            return mesa.substring(0, mesa.length() - 1) + ", <" + peca.lado1 + "." + peca.lado2 + ">]";
        } else if (peca.lado2 == Character.getNumericValue(op2)) {
            return mesa.substring(0, mesa.length() - 1) + ", <" + peca.lado2 + "." + peca.lado1 + ">]";
        }

        return mesa;
    }

    private boolean pecaEncaixaNaMesa(Peca peca, String mesa) {
        char op1 = mesa.charAt(2);
        char op2 = mesa.charAt(mesa.length() - 3);
        boolean a = peca.lado1 == Character.getNumericValue(op1) || peca.lado2 == Character.getNumericValue(op1)
                || peca.lado1 == Character.getNumericValue(op2) || peca.lado2 == Character.getNumericValue(op2);
        return a;
    }

    private int calcularQtdPecasOponente(String mesa, List<Peca> mao) {

        String[] pecasMesa = mesa.split("<");
        int qtd = 15 - mao.size() - (pecasMesa.length - 1);

        return qtd;
    }

    private List<Peca> inicializarMonte() {
        List<Peca> monte = new ArrayList<Peca>();
        for (int i = 0; i <= 6; i++) {
            for (int j = i; j <= 6; j++) {
                monte.add(new Peca(i, j));
            }
        }
        return monte;
    }

    private List<Peca> obterListaOponente(String mesa, List<Peca> mao) {

        List<Peca> domino = inicializarMonte();

        String[] pecasMesa = mesa.split("<");

        List<Peca> pecas = new ArrayList<Peca>();
        for (int i = 1; i < pecasMesa.length; i++) {
            int lado1 = Character.getNumericValue(pecasMesa[i].charAt(0));
            int lado2 = Character.getNumericValue(pecasMesa[i].charAt(2));
            pecas.add(new Peca(lado1, lado2));
        }

        domino.removeAll(mao);
        domino.removeAll(pecas);

        return domino;
    }

    private Double avaliarEstado(String mesa, List<Peca> mao, List<Peca> listaOponente) {

        List<Peca> possiveisJogadas = new ArrayList<Peca>();
        Double razao = 0.0;
        
        for (Peca peca : listaOponente) {
            if (pecaEncaixaNaMesa(peca, mesa)) {
                possiveisJogadas.add(peca);
            }
        }

        if (possiveisJogadas.size() != 0) {
            razao = 1.0 /(double) possiveisJogadas.size();
        } else {
            razao = 1.0;
        }
        return razao;
    }
    
    class Peca {
        int lado1, lado2;
    
        public Peca(int lado1, int lado2) {
            this.lado1 = lado1;
            this.lado2 = lado2;
        }
    
        @Override
        public String toString() {
            return "<" + lado1 + "." + lado2 + ">";
        }
    
        public boolean encaixa(int valor) {
            return lado1 == valor || lado2 == valor;
        }
    
        public void inverter() {
            int temp = lado1;
            lado1 = lado2;
            lado2 = temp;
        }

        @Override
        public boolean equals(Object peca) {
            if (this == peca) return true; 
            if (peca == null || getClass() != peca.getClass()) return false;
            Peca pecaTeste = (Peca) peca;
            return (lado1 == pecaTeste.lado1 &&  lado2 == pecaTeste.lado2) 
            ||(lado2 == pecaTeste.lado1 &&  lado1 == pecaTeste.lado2)  ; 
        }
    }
}
