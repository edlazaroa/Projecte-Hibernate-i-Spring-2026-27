package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String [] args) {
		Partida partida = new Partida();
		Jugador jugador = new Jugador();
		
		System.out.println("--- MÉTODES JUGADOR ---\n");
		
		
		System.out.println("--- MÉTODES PARTIDA ---\n");
		
		List<String> nomsJugadors = new ArrayList<>();

		nomsJugadors.add("Gabriel");
		nomsJugadors.add("Alex");
		nomsJugadors.add("Dennis");
		nomsJugadors.add("Ismael");
		
		System.out.println("1. Iniciar Jugador: ");
		partida.iniciarJugador(nomsJugadors);
		System.out.println("2. Mostrar Partida: ");
		System.out.println("3. Mostrar Tauler: ");
		System.out.println("4. Mostrar Recursos: ");
		System.out.println("5. Construir Carretera: ");
		System.out.println("6. Construir Poblat: ");
		System.out.println("7. Millorar A Ciutat: ");
		System.out.println("8. Comerciar Banca: ");
		System.out.println("9. Calcular Punts: ");
		System.out.println("10. Passar Torn: ");
		System.out.println("11. Vertexs Disponibles: ");
		System.out.println("12. Moure Lladre: ");
		System.out.println("13. Comprar Carta: ");
		System.out.println("14. Jugar Carta: ");
		System.out.println("15. Tirar Daus: ");
		System.out.println("16. Comprovar Victoria: ");
		System.out.println("17. Exercit Mes Gran: ");
		System.out.println("18. Mostrar Tauler: ");
		System.out.println("19. Mostrar Partida: ");
	}
}
