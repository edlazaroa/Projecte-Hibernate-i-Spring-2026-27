package projecteHibernateSpring;

import java.util.List;

public interface IPartida extends IGenericDao<Partida, Integer> {

	// Gestió de la partida
	int iniciarPartida(List<String> nomsJugadors);

	String mostrarPartida(int idPartida);

	String mostrarTauler(int idPartida);

	String tirarDaus(int idPartida);
	
	void comprovarVictoria(int idPartida);

	void exercitMesGran(int idPartida);
}