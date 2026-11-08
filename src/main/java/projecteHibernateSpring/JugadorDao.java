package projecteHibernateSpring;

import java.util.List;

import org.hibernate.Session;

public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugador {

	public JugadorDao() {
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

			List<RecursJugador> recursos = session
					.createQuery("from RecursJugador r where r.jugador.jugador_id = :idJugador", RecursJugador.class)
					.setParameter("idJugador", idJugador).getResultList();

			String resultado = "Recursos de " + jugador.getNom() + ":\n";

			for (RecursJugador recurso : recursos) {
				resultado += recurso.getTipus() + ": " + recurso.getQuantitat() + "\n";
			}

			session.getTransaction().commit();
			return resultado;

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al consultar los recursos del jugador.";
		}
	}

	public RecursJugador buscarRecurs(Session session, int idJugador, TipusRecurs tipus) {

		return session
				.createQuery("from RecursJugador r where r.jugador.jugador_id = :idJugador and r.tipus = :tipus",
						RecursJugador.class)
				.setParameter("idJugador", idJugador).setParameter("tipus", tipus).uniqueResult();
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

			Partida partida = jugador.getPartida();

			if (partida == null || aresta.getPartida() == null
					|| !aresta.getPartida().getId().equals(partida.getId())) {
				session.getTransaction().commit();
				return "La arista no pertenece a la partida del jugador.";
			}

			if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
				session.getTransaction().commit();
				return "No es el turno de este jugador.";
			}

			if (partida.getEstat() != EstatPartida.PREPARACIO && partida.getEstat() != EstatPartida.EN_CURS) {
				session.getTransaction().commit();
				return "La partida no está disponible para construir.";
			}

			if (aresta.getCarretera() != null) {
				session.getTransaction().commit();
				return "Esta arista ya tiene una carretera.";
			}

			Vertex vertexA = aresta.getVertexA();
			Vertex vertexB = aresta.getVertexB();

			if (partida.getEstat() == EstatPartida.PREPARACIO) {

				Long settlements = session
						.createQuery("select count(p) from Poblat p where p.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).uniqueResult();

				Long roads = session
						.createQuery("select count(c) from Carretera c where c.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).uniqueResult();

				if (settlements > 2 || settlements != roads + 1) {
					session.getTransaction().commit();
					return "Primero debes colocar un poblado antes de construir su carretera.";
				}

				List<Vertex> pendingVertices = session.createQuery(
						"select p.vertex from Poblat p where p.jugador.jugador_id = :idJugador and not exists (select c from Carretera c where c.jugador.jugador_id = :idJugador and (c.aresta.vertexA = p.vertex or c.aresta.vertexB = p.vertex))",
						Vertex.class).setParameter("idJugador", idJugador).getResultList();

				if (pendingVertices.size() != 1) {
					session.getTransaction().commit();
					return "No se ha podido identificar el poblado pendiente de carretera.";
				}

				Vertex pendingVertex = pendingVertices.get(0);

				if (!pendingVertex.equals(vertexA) && !pendingVertex.equals(vertexB)) {
					session.getTransaction().commit();
					return "La carretera debe estar conectada al último poblado colocado.";
				}

			} else {

				if (!partida.isDausTirats()) {
					session.getTransaction().commit();
					return "Primero debes tirar los dados.";
				}

				if (partida.isLladrePendent()) {
					session.getTransaction().commit();
					return "Primero debes mover el ladrón.";
				}

				Long roadCount = session
						.createQuery("select count(c) from Carretera c where c.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).uniqueResult();

				if (roadCount >= 15) {
					session.getTransaction().commit();
					return "Has alcanzado el límite de 15 carreteras.";
				}

				Long connectedBuildings = session.createQuery(
						"select count(a) from Assentament a where a.jugador.jugador_id = :idJugador and (a.vertex = :vertexA or a.vertex = :vertexB)",
						Long.class).setParameter("idJugador", idJugador).setParameter("vertexA", vertexA)
						.setParameter("vertexB", vertexB).uniqueResult();

				Long connectedRoads = session.createQuery(
						"select count(c) from Carretera c where c.jugador.jugador_id = :idJugador and ((c.aresta.vertexA = :vertexA or c.aresta.vertexB = :vertexA) or (c.aresta.vertexA = :vertexB or c.aresta.vertexB = :vertexB))",
						Long.class).setParameter("idJugador", idJugador).setParameter("vertexA", vertexA)
						.setParameter("vertexB", vertexB).uniqueResult();

				if (connectedBuildings == 0 && connectedRoads == 0) {
					session.getTransaction().commit();
					return "La carretera debe estar conectada a una construcción o carretera propia.";
				}

				RecursJugador fusta = buscarRecurs(session, idJugador, TipusRecurs.FUSTA);

				RecursJugador mao = buscarRecurs(session, idJugador, TipusRecurs.MAO);

				if (fusta == null || fusta.getQuantitat() < 1 || mao == null || mao.getQuantitat() < 1) {
					session.getTransaction().commit();
					return "No tienes suficiente madera o arcilla.";
				}

				fusta.setQuantitat(fusta.getQuantitat() - 1);
				mao.setQuantitat(mao.getQuantitat() - 1);
			}

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

			Partida partida = jugador.getPartida();

			if (partida == null) {
				session.getTransaction().commit();
				return "El jugador no pertenece a ninguna partida.";
			}

			if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
				session.getTransaction().commit();
				return "No es el turno de este jugador.";
			}

			if (partida.getEstat() != EstatPartida.PREPARACIO && partida.getEstat() != EstatPartida.EN_CURS) {
				session.getTransaction().commit();
				return "La partida no está disponible para construir.";
			}

			if (!vertex.getPartida().getId().equals(partida.getId())) {
				session.getTransaction().commit();
				return "El vértice pertenece a otra partida.";
			}

			if (partida.getEstat() == EstatPartida.EN_CURS) {
				if (!partida.isDausTirats()) {
					session.getTransaction().commit();
					return "Primero debes tirar los dados.";
				}

				if (partida.isLladrePendent()) {
					session.getTransaction().commit();
					return "Primero debes mover el ladrón.";
				}
			}

			if (vertex.getAssentament() != null) {
				session.getTransaction().commit();
				return "Este vértice ya está ocupado.";
			}

			List<Vertex> adjacentVertices = session.createQuery(
					"select case when a.vertexA = :vertex then a.vertexB else a.vertexA end from Aresta a where a.vertexA = :vertex or a.vertexB = :vertex",
					Vertex.class).setParameter("vertex", vertex).getResultList();

			Long adjacentSettlements = session
					.createQuery("select count(a) from Assentament a where a.vertex in :vertices", Long.class)
					.setParameter("vertices", adjacentVertices).uniqueResult();

			if (adjacentSettlements > 0) {
				session.getTransaction().commit();
				return "No puedes construir: incumple la regla de la distancia.";
			}

			Long settlements = session
					.createQuery("select count(p) from Poblat p where p.jugador.jugador_id = :idJugador", Long.class)
					.setParameter("idJugador", idJugador).uniqueResult();

			if (partida.getEstat() == EstatPartida.PREPARACIO) {

				Long roads = session
						.createQuery("select count(c) from Carretera c where c.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).uniqueResult();

				if (settlements >= 2) {
					session.getTransaction().commit();
					return "Ya has colocado los dos poblados iniciales.";
				}

				if (!settlements.equals(roads)) {
					session.getTransaction().commit();
					return "Primero debes construir la carretera del poblado anterior.";
				}

			} else {

				if (settlements >= 5) {
					session.getTransaction().commit();
					return "Has alcanzado el límite de 5 poblados.";
				}

				Long connectedRoads = session.createQuery(
						"select count(c) from Carretera c where c.jugador.jugador_id = :idJugador and (c.aresta.vertexA = :vertex or c.aresta.vertexB = :vertex)",
						Long.class).setParameter("idJugador", idJugador).setParameter("vertex", vertex).uniqueResult();

				if (connectedRoads == 0) {
					session.getTransaction().commit();
					return "El poblado debe estar conectado a una carretera propia.";
				}
			}

			RecursJugador fusta = null;
			RecursJugador mao = null;
			RecursJugador llana = null;
			RecursJugador blat = null;

			if (partida.getEstat() == EstatPartida.EN_CURS) {

				fusta = buscarRecurs(session, idJugador, TipusRecurs.FUSTA);
				mao = buscarRecurs(session, idJugador, TipusRecurs.MAO);
				llana = buscarRecurs(session, idJugador, TipusRecurs.LLANA);
				blat = buscarRecurs(session, idJugador, TipusRecurs.BLAT);

				if (fusta == null || fusta.getQuantitat() < 1 || mao == null || mao.getQuantitat() < 1 || llana == null
						|| llana.getQuantitat() < 1 || blat == null || blat.getQuantitat() < 1) {

					session.getTransaction().commit();
					return "No tienes suficientes recursos para construir un poblado.";
				}

				fusta.setQuantitat(fusta.getQuantitat() - 1);
				mao.setQuantitat(mao.getQuantitat() - 1);
				llana.setQuantitat(llana.getQuantitat() - 1);
				blat.setQuantitat(blat.getQuantitat() - 1);
			}

			Poblat poblat = new Poblat();
			poblat.setJugador(jugador);
			poblat.setVertex(vertex);
			session.persist(poblat);

			if (partida.getEstat() == EstatPartida.PREPARACIO && settlements == 1) {

				for (HexagonVertex position : vertex.getPosicions()) {

					Hexagon hexagon = position.getHexagon();

					if (hexagon.getTerreny() == TipusTerreny.DESERT) {
						continue;
					}

					TipusRecurs resourceType = null;

					switch (hexagon.getTerreny()) {
					case BOSC:
						resourceType = TipusRecurs.FUSTA;
						break;
					case TURONS:
						resourceType = TipusRecurs.MAO;
						break;
					case PASTURES:
						resourceType = TipusRecurs.LLANA;
						break;
					case CAMPS:
						resourceType = TipusRecurs.BLAT;
						break;
					case MUNTANYES:
						resourceType = TipusRecurs.MINERAL;
						break;
					default:
						break;
					}

					if (resourceType != null) {

						RecursJugador resource = buscarRecurs(session, idJugador, resourceType);

						if (resource == null) {
							resource = new RecursJugador(null, resourceType, 1, jugador);
							session.persist(resource);
						} else {
							resource.setQuantitat(resource.getQuantitat() + 1);
						}
					}
				}
			}

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

			RecursJugador blat = buscarRecurs(session, idJugador, TipusRecurs.BLAT);

			RecursJugador mineral = buscarRecurs(session, idJugador, TipusRecurs.MINERAL);

			if (blat == null || blat.getQuantitat() < 2 || mineral == null || mineral.getQuantitat() < 3) {
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

			Long poblados = session
					.createQuery("select count(p) from Poblat p where p.jugador.jugador_id = :idJugador", Long.class)
					.setParameter("idJugador", idJugador).uniqueResult();

			Long ciudades = session
					.createQuery("select count(c) from Ciutat c where c.jugador.jugador_id = :idJugador", Long.class)
					.setParameter("idJugador", idJugador).uniqueResult();

			int puntos = poblados.intValue() + ciudades.intValue() * 2;

			session.getTransaction().commit();

			return "El jugador " + jugador.getNom() + " tiene " + puntos + " puntos.";

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

			List<Jugador> jugadors = session
					.createQuery("from Jugador j where j.partida.partida_id = :idPartida order by j.ordreTorn",
							Jugador.class)
					.setParameter("idPartida", partida.getId()).getResultList();

			if (jugadors.isEmpty()) {
				session.getTransaction().commit();
				return "No hay jugadores en la partida.";
			}

			if (partida.getEstat() == EstatPartida.FINALITZADA) {
				session.getTransaction().commit();
				return "La partida ha finalizado.";
			}

			if (partida.getEstat() == EstatPartida.PREPARACIO) {

				Long totalPoblats = session
						.createQuery("select count(p) from Poblat p where p.jugador.partida.partida_id = :idPartida",
								Long.class)
						.setParameter("idPartida", partida.getId()).getSingleResult();

				Long poblatsJugador = session
						.createQuery("select count(p) from Poblat p where p.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).getSingleResult();

				Long carreteresJugador = session
						.createQuery("select count(c) from Carretera c where c.jugador.jugador_id = :idJugador",
								Long.class)
						.setParameter("idJugador", idJugador).getSingleResult();

				if (!poblatsJugador.equals(carreteresJugador)) {
					session.getTransaction().commit();
					return "Debes colocar una carretera por cada poblado antes de pasar turno.";
				}

				int totalJugadors = jugadors.size();
				int ordreEsperat;

				if (totalPoblats <= totalJugadors) {
					ordreEsperat = totalPoblats.intValue();
				} else {
					ordreEsperat = (totalJugadors * 2) + 1 - totalPoblats.intValue();
				}

				if (jugador.getOrdreTorn() != ordreEsperat) {
					session.getTransaction().commit();
					return "Debes completar tu colocación antes de pasar turno.";
				}

				int poblatsEsperats = totalPoblats <= totalJugadors ? 1 : 2;

				if (poblatsJugador != poblatsEsperats) {
					session.getTransaction().commit();
					return "Debes colocar el poblado correspondiente antes de pasar turno.";
				}

				if (totalPoblats == totalJugadors * 2) {

					Jugador primerJugador = jugadors.get(0);

					partida.setEstat(EstatPartida.EN_CURS);
					partida.setRonda(1);
					partida.setActiu(primerJugador);
					partida.setDausTirats(false);
					partida.setLladrePendent(false);

					session.getTransaction().commit();

					return "Preparación finalizada. La partida comienza con " + primerJugador.getNom() + ".";
				}

				int seguentOrdre;

				if (totalPoblats < totalJugadors) {
					seguentOrdre = jugador.getOrdreTorn() + 1;
				} else if (totalPoblats == totalJugadors) {
					seguentOrdre = jugador.getOrdreTorn();
				} else {
					seguentOrdre = jugador.getOrdreTorn() - 1;
				}

				Jugador jugadorSeguent = jugadors.get(seguentOrdre - 1);

				partida.setActiu(jugadorSeguent);
				partida.setDausTirats(false);
				partida.setLladrePendent(false);

				session.getTransaction().commit();

				return "Colocación completada. Ahora juega " + jugadorSeguent.getNom() + ".";

			}

			// FASE EN_CURS
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
			partida.setDausTirats(false);
			partida.setLladrePendent(false);

			if (seguent == 0) {
				partida.setRonda(partida.getRonda() + 1);
			}

			session.getTransaction().commit();

			return "Turno pasado. Ahora juega " + jugadorSeguent.getNom() + ".";

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al pasar el turno.";
		}
	}

	@Override
	public String vertexsDisponibles(int idJugador) {
		Session session = sessionFactory.getCurrentSession();

		try {
			session.beginTransaction();

			Jugador jugador = session.get(Jugador.class, idJugador);

			if (jugador == null) {
				session.getTransaction().commit();
				return "El jugador no existe.";
			}

			if (jugador.getPartida() == null) {
				session.getTransaction().commit();
				return "El jugador no pertenece a ninguna partida.";
			}

			Partida game = jugador.getPartida();

			if (game.getEstat() == EstatPartida.FINALITZADA) {
				session.getTransaction().commit();
				return "La partida ha finalizado.";
			}

			String query = "select distinct v from Vertex v where v.partida.partida_id = :idPartida and not exists (select a from Assentament a where a.vertex = v) and not exists (select a from Aresta a, Assentament s where a.vertexA = v and s.vertex = a.vertexB) and not exists (select a from Aresta a, Assentament s where a.vertexB = v and s.vertex = a.vertexA)";

			if (game.getEstat() == EstatPartida.EN_CURS) {
				query += " and exists (select c from Carretera c where c.jugador.jugador_id = :idJugador and (c.aresta.vertexA = v or c.aresta.vertexB = v))";
			}

			var queryBuilder = session.createQuery(query, Vertex.class).setParameter("idPartida", game.getId());

			if (game.getEstat() == EstatPartida.EN_CURS) {
				queryBuilder.setParameter("idJugador", idJugador);
			}

			List<Vertex> availableVertices = queryBuilder.getResultList();

			String result = "Vértices disponibles:\n";

			if (availableVertices.isEmpty()) {
				result += "No hay vértices disponibles.";
			} else {
				for (Vertex vertex : availableVertices) {
					result += "Vértice ID: " + vertex.getVertex_id() + "\n";
				}
			}

			session.getTransaction().commit();

			return result;

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al consultar los vértices disponibles.";
		}
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
