package projecteHibernateSpring;

import java.util.List;

public class PartidaDAO extends GenericDao<Partida, Integer> implements IPartida{

	@Override
	public int iniciarPartida(List<String> nomsJugadors) {
	        if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
	            throw new IllegalArgumentException("Hi ha d'haver entre 2 i 4 jugadors");
	        }

	        Transaction tx = null;
	        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	            tx = session.beginTransaction();

	            // 1. Partida
	            partida partida = new partida();
	            partida.setEstat(EstatPartida.PREPARACIO);
	            partida.setDataInici(new Date());
	            partida.setRonda(1);
	            partida.setPuntsPerGuanyar(10);
	            partida.setDausTirats(false);
	            partida.setLladrePendent(false);
	            session.persist(partida);

	}

	@Override
	public String mostrarPartida() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mostrarTauler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mostrarRecursos(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String construirCarretera(int idJugador, int idAresta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String construirPoblat(int idJugador, int idVertex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String millorarACiutat(int idJugador, int idVertex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String calcularPunts(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String passarTorn(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String vertexsDisponibles(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String comprarCarta(int idJugador) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
		// TODO Auto-generated method stub
		return null;
	}
	// AQUI IRAN TODOS LOS METODOS DE IPartida IMPLEMENTADOS
	// Implement IJugador y Extenteinde Gen
}
