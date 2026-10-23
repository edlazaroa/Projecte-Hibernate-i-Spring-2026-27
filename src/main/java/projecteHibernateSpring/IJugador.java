package projecteHibernateSpring;

public interface IJugador extends IGenericDao<Jugador, Integer>{
	// Recursos del jugador
    String mostrarRecursos(int idJugador);

    // Construcciones
    String construirCarretera(int idJugador, int idAresta);
    String construirPoblat(int idJugador, int idVertex);
    String millorarACiutat(int idJugador, int idVertex);

    // Comercio
    String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);

    // Puntuación
    String calcularPunts(int idJugador);

    // Turno
    String passarTorn(int idJugador);

    // Consultas
    String vertexsDisponibles(int idJugador);

    // Ladrón
    String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

    // Cartas de desarrollo
    String comprarCarta(int idJugador);
    String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);
}
