import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

abstract interface Agente {

	public abstract int indicePecaParaJogar(String mesa, String mao);

}

class AgenteConstante implements Agente {

	@Override
	public int indicePecaParaJogar(String mesa, String mao) {
		return 0;
	}

}

class AgenteRandomicoTotal implements Agente {

	static final Random rand = new Random();

	@Override
	public int indicePecaParaJogar(String mesa, String mao) {
		String[] maos = mao.split("<");
		return rand.nextInt(maos.length - 1);
	}

}

class AgenteSimples implements Agente {

	static final Random rand = new Random();

	@Override
	public int indicePecaParaJogar(String mesa, String mao) {
		// duas op��es de n�meros poss�veis para jogar
		char op1 = mesa.charAt(2);
		char op2 = mesa.charAt(mesa.length() - 3);
		// System.out.println(op1 + " ou " + op2 );
		String[] maos = mao.split("<"); // todas as pe�as
		int indice = 0;
		System.out.println();
		for (int i = 1; i < maos.length; i++) {
			// System.out.print(maos[i].charAt(0) + ", " + maos[i].charAt(2) + ", ");
			// System.out.println("--" + maos[i] + "--");
			if (maos[i].charAt(0) == op1)
				return i - 1; // a pe�a encaixa do lado esquerdo com a op1
			if (maos[i].charAt(2) == op1)
				return i - 1;// a pe�a encaixa do lado direito com a op1
			if (maos[i].charAt(0) == op2)
				return i - 1;// a pe�a encaixa do lado esquerdo com a op2
			if (maos[i].charAt(2) == op2)
				return i - 1;// a pe�a encaixa do lado direito com a op2
		}
		System.out.println();
		return indice;
	}

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
}

class Jogador extends Thread {
	String nome;
	List<Peca> mao = new ArrayList<>();
	Agente agente;
	Peca peca_escolhida;
	String mesa;

	public Jogador(String nome, Agente agente) {
		this.nome = nome;
		this.agente = agente;
	}

	public synchronized int pontos() {
		int p = 0;
		for (Iterator<Peca> iterator = mao.iterator(); iterator.hasNext();) {
			Peca peca = (Peca) iterator.next();
			p += peca.lado1;
			p += peca.lado2;
		}
		return p;
	}

	public synchronized void comprarPeca(Peca peca) {
		mao.add(peca);
	}

	public synchronized void mostrarMao() {
		System.out.println("M�o de " + nome + ": " + mao);
	}

	public synchronized void jogarPeca() {
		int indice = agente.indicePecaParaJogar(mesa, mao.toString());
		if (indice < 0)
			indice = 0;
		if (indice >= mao.size())
			indice = 0;
		peca_escolhida = mao.remove(indice);
	}

	public synchronized void setMesa(String mesa) {
		this.mesa = mesa;
	}

	@Override
	public void run() {
		while (true) {
			try {
				synchronized (this) {
					this.wait(); // Aguarda at� que algum agendamento ocorra.
				}
			} catch (InterruptedException e) {
				System.out.println(this.getName() + ": Erro no Controlador");
			}

			jogarPeca();

		}
	}
}

public class Domino {

	List<Peca> monte = new ArrayList<>();
	List<Peca> mesa = new ArrayList<>();
	Jogador jogador1, jogador2;

	public Domino(Jogador nome1, Jogador nome2) {
		jogador1 = nome1;
		jogador2 = nome2;
	}

	private void inicializarMonte() {
		for (int i = 0; i <= 6; i++) {
			for (int j = i; j <= 6; j++) {
				monte.add(new Peca(i, j));
			}
		}
		Collections.shuffle(monte);
	}

	private void distribuirPecas() {
		for (int i = 0; i < 7; i++) {
			jogador1.comprarPeca(monte.remove(0));
			jogador2.comprarPeca(monte.remove(0));
		}
	}

	private void mostrarMesa() {
		System.out.println("Mesa: " + mesa);
	}

	public int iniciarJogo(long miliSegundosDeIntervalo, int maximoDeJogadasInvalidas) {

		jogador1.start();
		jogador2.start();

		inicializarMonte();
		distribuirPecas();

		mesa.add(monte.remove(0)); // Coloca a primeira pe�a na mesa

		boolean vezJogador1 = true;
		int jogadasInvalidas = 0;

		while (!jogador1.mao.isEmpty() && !jogador2.mao.isEmpty() && jogadasInvalidas < maximoDeJogadasInvalidas) {

			mostrarMesa();

			Jogador jogadorAtual = vezJogador1 ? jogador1 : jogador2;
			System.out.println(jogadorAtual.nome + ", � sua vez!");
			jogadorAtual.mostrarMao();
			jogadorAtual.setMesa(mesa.toString());

			synchronized (jogadorAtual) {
				jogadorAtual.notify();
			}

			try {
				Thread.sleep(miliSegundosDeIntervalo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Peca pecaEscolhida = jogadorAtual.peca_escolhida;

			// Verifica se a pe�a encaixa em algum lado da mesa
			Peca primeiraPecaMesa = mesa.get(0);
			Peca ultimaPecaMesa = mesa.get(mesa.size() - 1);

			if (pecaEscolhida.encaixa(primeiraPecaMesa.lado1)) {
				if (pecaEscolhida.lado2 != primeiraPecaMesa.lado1) {
					pecaEscolhida.inverter();
				}
				mesa.add(0, pecaEscolhida); // Adiciona no in�cio da mesa
			} else if (pecaEscolhida.encaixa(ultimaPecaMesa.lado2)) {
				if (pecaEscolhida.lado1 != ultimaPecaMesa.lado2) {
					pecaEscolhida.inverter();
				}
				mesa.add(pecaEscolhida); // Adiciona no final da mesa
			} else {
				System.out.println("Pe�a inv�lida! Perdeu a vez.");
				jogadorAtual.comprarPeca(pecaEscolhida); // Devolve a pe�a se n�o puder jogar
				jogadasInvalidas++;
			}

			vezJogador1 = !vezJogador1; // Troca de jogador
		}

		if (jogador1.mao.isEmpty()) {
			System.out.println("Parab�ns " + jogador1.nome + ", voc� venceu!");
			return 1;
		} else if (jogador2.mao.isEmpty()) {
			System.out.println("Parab�ns " + jogador2.nome + ", voc� venceu!");
			return 2;
		} else if (jogador1.pontos() < jogador2.pontos()) {
			System.out.println("Parab�ns " + jogador1.nome + ", voc� venceu!");
			return 1;
		} else if (jogador1.pontos() > jogador2.pontos()) {
			System.out.println("Parab�ns " + jogador2.nome + ", voc� venceu!");
			return 2;
		}
		System.out.println("O jogo terminou empatado.");
		return -1;

	}

	public static void main(String[] args) {
		Jogador jog1 = new Jogador("CONSTANTE", new AgenteConstante());
		System.out.println("Nome do Jogador 1: " + jog1.nome);
		Jogador jog2 = new Jogador("RAND�MICO", new AgenteRandomicoTotal());
		System.out.println("Nome do Jogador 2: " + jog2.nome);
		Jogador jog3 = new Jogador("SIMPLES-1", new AgenteSimples());
		System.out.println("Nome do Jogador 3: " + jog3.nome);
		Jogador jog4 = new Jogador("BUSCA", new AgenteBaseadoEmBusca());
		System.out.println("Nome do Jogador 4: " + jog4.nome);

		Jogador jogador1 = jog4;
		Jogador jogador2 = jog2;
		Domino jogo = new Domino(jogador1, jogador2);

		long miliSegundosDeIntervaloPorJogador = 100;
		int maximoDeJogadasInvalidas = 4;
		int saida = jogo.iniciarJogo(miliSegundosDeIntervaloPorJogador, maximoDeJogadasInvalidas);
		System.out.println(saida);
		System.exit(0);
	}
}
