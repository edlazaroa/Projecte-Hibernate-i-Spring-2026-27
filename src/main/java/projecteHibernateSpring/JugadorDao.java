package projecteHibernateSpring;

import java.util.List;

import org.hibernate.Session;

public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugador{

	public JugadorDao() {
        super();
    }
	

	@Override
	public String mostrarRecursos(int idJugador) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        List<RecursJugador> recursos = session.createQuery(
	            "FROM RecursJugador r WHERE r.jugador.jugador_id = :idJugador",
	            RecursJugador.class
	        )
	        .setParameter("idJugador", idJugador)
	        .getResultList();
	
	        String resultado = "Recursos de " + jugador.getNom() + ":\n";
	
	        for (RecursJugador recurso : recursos) {
	            resultado += recurso.getTipus() + ": "
	                      + recurso.getQuantitat() + "\n";
	        }
	
	        session.getTransaction().commit();
	        return resultado;
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al consultar los recursos del jugador.";
	    }
	}



	@Override
	public String construirCarretera(int idJugador, int idAresta) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	        Aresta aresta = session.get(Aresta.class, idAresta);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        if (aresta == null) {
	            session.getTransaction().commit();
	            return "La arista no existe.";
	        }
	
	        // Aquí comprobaremos:
	        // 1. Que sea el turno del jugador.
	        // 2. Que la arista esté disponible.
	        // 3. Que el jugador tenga 1 madera y 1 arcilla.
	        // 4. Que la carretera se pueda conectar a su red.
	
	        session.getTransaction().commit();
	
	        return "Jugador y arista encontrados. Falta validar los requisitos de construcción.";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al construir la carretera.";
	    }
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

}
