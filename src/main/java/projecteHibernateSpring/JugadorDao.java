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
	
	public RecursJugador buscarRecurs(Session session, int idJugador, TipusRecurs tipus) {

	    return session.createQuery(
	        "FROM RecursJugador r " +
	        "WHERE r.jugador.jugador_id = :idJugador " +
	        "AND r.tipus = :tipus",
	        RecursJugador.class
	    )
	    .setParameter("idJugador", idJugador)
	    .setParameter("tipus", tipus)
	    .uniqueResult();
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
	
	        if (aresta.getCarretera() != null) {
	            session.getTransaction().commit();
	            return "Esta arista ya tiene una carretera.";
	        }
	
	        RecursJugador fusta =
	            buscarRecurs(session, idJugador, TipusRecurs.FUSTA);
	
	        RecursJugador mao =
	            buscarRecurs(session, idJugador, TipusRecurs.MAO);
	
	        if (fusta == null || fusta.getQuantitat() < 1
	                || mao == null || mao.getQuantitat() < 1) {
	            session.getTransaction().commit();
	            return "No tienes suficiente madera o arcilla.";
	        }
	
	        fusta.setQuantitat(fusta.getQuantitat() - 1);
	        mao.setQuantitat(mao.getQuantitat() - 1);
	
	        Carretera carretera = new Carretera();
	        carretera.setJugador(jugador);
	        carretera.setAresta(aresta);
	
	        session.persist(carretera);
	
	        session.getTransaction().commit();
	
	        return "Carretera construida correctamente.";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al construir la carretera.";
	    }
	}


	@Override
	public String construirPoblat(int idJugador, int idVertex) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	        Vertex vertex = session.get(Vertex.class, idVertex);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        if (vertex == null) {
	            session.getTransaction().commit();
	            return "El vértice no existe.";
	        }
	
	        if (vertex.getAssentament() != null) {
	            session.getTransaction().commit();
	            return "Este vértice ya está ocupado.";
	        }
	
	        RecursJugador fusta =
	            buscarRecurs(session, idJugador, TipusRecurs.FUSTA);
	
	        RecursJugador mao =
	            buscarRecurs(session, idJugador, TipusRecurs.MAO);
	
	        RecursJugador llana =
	            buscarRecurs(session, idJugador, TipusRecurs.LLANA);
	
	        RecursJugador blat =
	            buscarRecurs(session, idJugador, TipusRecurs.BLAT);
	
	        if (fusta == null || fusta.getQuantitat() < 1
	                || mao == null || mao.getQuantitat() < 1
	                || llana == null || llana.getQuantitat() < 1
	                || blat == null || blat.getQuantitat() < 1) {
	
	            session.getTransaction().commit();
	            return "No tienes suficientes recursos para construir un poblado.";
	        }
	
	        Poblat poblat = new Poblat();
	        poblat.setJugador(jugador);
	        poblat.setVertex(vertex);
	
	        fusta.setQuantitat(fusta.getQuantitat() - 1);
	        mao.setQuantitat(mao.getQuantitat() - 1);
	        llana.setQuantitat(llana.getQuantitat() - 1);
	        blat.setQuantitat(blat.getQuantitat() - 1);
	
	        session.persist(poblat);
	
	        session.getTransaction().commit();
	
	        return "Poblado construido correctamente.";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al construir el poblado.";
	    }
	}

	@Override
	public String millorarACiutat(int idJugador, int idVertex) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	        Vertex vertex = session.get(Vertex.class, idVertex);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        if (vertex == null) {
	            session.getTransaction().commit();
	            return "El vértice no existe.";
	        }
	
	        Assentament assentament = vertex.getAssentament();
	
	        if (!(assentament instanceof Poblat)) {
	            session.getTransaction().commit();
	            return "En este vértice no hay un poblado para mejorar.";
	        }
	
	        if (!assentament.getJugador().getId().equals(idJugador)) {
	            session.getTransaction().commit();
	            return "Este poblado no pertenece al jugador.";
	        }
	
	        RecursJugador blat =
	                buscarRecurs(session, idJugador, TipusRecurs.BLAT);
	
	        RecursJugador mineral =
	                buscarRecurs(session, idJugador, TipusRecurs.MINERAL);
	
	        if (blat == null || blat.getQuantitat() < 2
	                || mineral == null || mineral.getQuantitat() < 3) {
	            session.getTransaction().commit();
	            return "No tienes suficientes recursos para mejorar la ciudad.";
	        }
	
	        blat.setQuantitat(blat.getQuantitat() - 2);
	        mineral.setQuantitat(mineral.getQuantitat() - 3);
	
	        session.remove(assentament);
	
	        Ciutat ciutat = new Ciutat();
	        ciutat.setJugador(jugador);
	        ciutat.setVertex(vertex);
	
	        session.persist(ciutat);
	
	        session.getTransaction().commit();
	
	        return "Ciudad construida correctamente.";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al mejorar el poblado a ciudad.";
	    }
	}

	@Override
	public String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        if (dona == null || rep == null) {
	            session.getTransaction().commit();
	            return "Los recursos no pueden ser nulos.";
	        }
	
	        if (dona == rep) {
	            session.getTransaction().commit();
	            return "Debes intercambiar dos recursos diferentes.";
	        }
	
	        RecursJugador recursoDado = buscarRecurs(session, idJugador, dona);
	
	        RecursJugador recursoRecibido = buscarRecurs(session, idJugador, rep);
	
	        if (recursoDado == null || recursoDado.getQuantitat() < 4) {
	            session.getTransaction().commit();
	            return "No tienes 4 unidades del recurso que quieres intercambiar.";
	        }
	
	        if (recursoRecibido == null) {
	            session.getTransaction().commit();
	            return "No existe el recurso que quieres recibir.";
	        }
	
	        recursoDado.setQuantitat(recursoDado.getQuantitat() - 4);
	
	        recursoRecibido.setQuantitat(recursoRecibido.getQuantitat() + 1);
	
	        session.getTransaction().commit();
	
	        return "Intercambio realizado correctamente: 4 " + dona + " por 1 " + rep + ".";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al comerciar con la banca.";
	    }
	}

	@Override
	public String calcularPunts(int idJugador) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        Long poblados = session.createQuery(
	                "SELECT COUNT(p) FROM Poblat p "
	                + "WHERE p.jugador.jugador_id = :idJugador",
	                Long.class
	        )
	        .setParameter("idJugador", idJugador)
	        .uniqueResult();
	
	        Long ciudades = session.createQuery(
	                "SELECT COUNT(c) FROM Ciutat c "
	                + "WHERE c.jugador.jugador_id = :idJugador",
	                Long.class
	        )
	        .setParameter("idJugador", idJugador)
	        .uniqueResult();
	
	        int puntos = poblados.intValue()
	                + ciudades.intValue() * 2;
	
	        session.getTransaction().commit();
	
	        return "El jugador " + jugador.getNom()
	                + " tiene " + puntos + " puntos.";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al calcular los puntos.";
	    }
	}


	@Override
	public String passarTorn(int idJugador) {
	    Session session = sessionFactory.getCurrentSession();
	
	    try {
	        session.beginTransaction();
	
	        Jugador jugador = session.get(Jugador.class, idJugador);
	
	        if (jugador == null) {
	            session.getTransaction().commit();
	            return "El jugador no existe.";
	        }
	
	        Partida partida = jugador.getPartida();
	
	        if (partida == null) {
	            session.getTransaction().commit();
	            return "El jugador no pertenece a ninguna partida.";
	        }
	
	        Jugador actiu = partida.getActiu();
	
	        if (actiu == null || !actiu.getId().equals(idJugador)) {
	            session.getTransaction().commit();
	            return "No es el turno de este jugador.";
	        }
	
	        List<Jugador> jugadors = session.createQuery(
	                "FROM Jugador j "
	                + "WHERE j.partida.partida_id = :idPartida "
	                + "ORDER BY j.ordreTorn",
	                Jugador.class
	        )
	        .setParameter("idPartida", partida.getId())
	        .getResultList();
	
	        if (jugadors.isEmpty()) {
	            session.getTransaction().commit();
	            return "No hay jugadores en la partida.";
	        }
	
	        int posicio = -1;
	
	        for (int i = 0; i < jugadors.size(); i++) {
	            if (jugadors.get(i).getId().equals(idJugador)) {
	                posicio = i;
	                break;
	            }
	        }
	
	        if (posicio == -1) {
	            session.getTransaction().commit();
	            return "No se ha encontrado el jugador en la partida.";
	        }
	
	        int seguent = (posicio + 1) % jugadors.size();
	        Jugador jugadorSeguent = jugadors.get(seguent);
	
	        partida.setActiu(jugadorSeguent);
	
	        // Preparar el turno siguiente
	        partida.setDausTirats(false);
	        partida.setLladrePendent(false);
	
	        session.getTransaction().commit();
	
	        return "Turno pasado. Ahora juega "
	                + jugadorSeguent.getNom() + ".";
	
	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error al pasar el turno.";
	    }
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
