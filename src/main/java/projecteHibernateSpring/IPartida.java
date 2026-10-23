package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ClasePartida.ConstruccionsVertex;
import ClasePartida.EstatPartida;
import ClasePartida.Hexagons;
import ClasePartida.Jugador;
import ClasePartida.Partida;
import ClasePartida.RecursJugador;
import ClasePartida.TipusRecurs;
import ClasePartida.TipusTerreny;
import ClasePartida.jugador;
import ClasePartida.partida;
import ClasePartida.vertex;

public interface IPartida extends IGenericDao<Partida, Integer>{
	// Gestió de la partida
	int iniciarPartida(List<String> nomsJugadors);
	String mostrarPartida();
	String mostrarTauler();

	// Recursos del jugador
	String mostrarRecursos(int idJugador);

	// Construccions
	String construirCarretera(int idJugador, int idAresta);
	String construirPoblat(int idJugador, int idVertex);
	String millorarACiutat(int idJugador, int idVertex);

	// Comerç
	String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);

	// Puntuació
	String calcularPunts(int idJugador);

	// Torn
	String passarTorn(int idJugador);

	// Consultes
	String vertexsDisponibles(int idJugador);

	// Lladre
	String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

	// Cartes de desenvolupament
	String comprarCarta(int idJugador);
	String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);
}
