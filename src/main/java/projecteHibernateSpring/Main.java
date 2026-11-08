package projecteHibernateSpring;

import java.util.ArrayList;

import java.util.List;

import org.hibernate.Session;

public class Main {

	private static final int NUM_RONDES = 3;

	public static void main(String[] args) {

		PartidaDao partidaDao = new PartidaDao();
		JugadorDao jugadorDao = new JugadorDao();

		try {

			List<String> playerNames = new ArrayList<>();

			playerNames.add("Gabriel");

			playerNames.add("Alex");

			playerNames.add("Dennis");

			playerNames.add("Ismael");

			System.out.println("==================================");

			System.out.println("       INICIANT PARTIDA CATAN");

			System.out.println("==================================");

			System.out.println("\n--- CREAR PARTIDA ---");

			int gameId = partidaDao.iniciarPartida(playerNames);

			if (gameId == -1) {

				System.out.println("No s'ha pogut iniciar la partida.");

				return;

			}

			System.out.println("Partida creada amb l'ID: " + gameId);

			List<Integer> playerIds = obtenirIdsJugadors(gameId);

			if (playerIds.isEmpty()) {

				System.out.println("No hi ha cap jugador registrat.");

				return;

			}

			System.out.println("\n--- JUGADORS ---");

			for (int i = 0; i < playerIds.size(); i++) {

				System.out.println(

						"Jugador " + (i + 1) + " - ID: " + playerIds.get(i)

				);

			}

			System.out.println("\n--- ESTAT INICIAL ---");

			System.out.println(partidaDao.mostrarPartida(gameId));

			System.out.println(partidaDao.mostrarTauler(gameId));

			boolean gameFinished = false;

			for (int round = 1; round <= NUM_RONDES && !gameFinished; round++) {

				System.out.println("\n==================================");

				System.out.println("             RONDA " + round);

				System.out.println("==================================");

				for (int i = 0; i < playerIds.size(); i++) {

					int playerId = playerIds.get(i);

					System.out.println("\n----------------------------------");

					System.out.println("TORN DEL JUGADOR " + (i + 1));

					System.out.println("ID: " + playerId);

					System.out.println("----------------------------------");

					System.out.println("\n1. Recursos del jugador:");

					System.out.println(jugadorDao.mostrarRecursos(playerId));

					System.out.println("\n2. Llançar els daus:");

					System.out.println(partidaDao.tirarDaus(gameId));

					System.out.println("\n3. Vèrtexs disponibles:");

					System.out.println(jugadorDao.vertexsDisponibles(playerId));

					System.out.println("\n4. Punts del jugador:");

					System.out.println(jugadorDao.calcularPunts(playerId));

					System.out.println("\n5. Comprovar si hi ha algun guanyador:");

					partidaDao.comprovarVictoria(gameId);

					System.out.println("\n6. Passar el torn:");

					System.out.println(jugadorDao.passarTorn(playerId));

					System.out.println("\nEstat actual de la partida:");

					System.out.println(partidaDao.mostrarPartida(gameId));

				}

			}

			System.out.println("\n==================================");

			System.out.println("          FINAL DE LA PARTIDA");

			System.out.println("==================================");

			System.out.println("\n--- TAULER FINAL ---");

			System.out.println(partidaDao.mostrarTauler(gameId));

			System.out.println("\n--- ESTAT FINAL ---");

			System.out.println(partidaDao.mostrarPartida(gameId));

			System.out.println("\n--- EXÈRCIT MÉS GRAN ---");

			partidaDao.exercitMesGran(gameId);

			System.out.println("\n--- PUNTS FINALS ---");

			for (int playerId : playerIds) {

				System.out.println(

						"Jugador amb ID " + playerId + ": "

								+ jugadorDao.calcularPunts(playerId)

				);

			}

			System.out.println("\nSimulació finalitzada.");

		} catch (RuntimeException e) {

			System.out.println("\nS'ha produït un error durant la simulació:");

			e.printStackTrace();

		} finally {

			SessionManager.tancar();

		}

	}

	private static List<Integer> obtenirIdsJugadors(int gameId) {

		Session session =

				SessionManager.getSessionFactory().getCurrentSession();

		try {

			session.beginTransaction();

			List<Jugador> players = session.createQuery(

					"FROM Jugador j "

							+ "WHERE j.partida.partida_id = :gameId "

							+ "ORDER BY j.ordreTorn",

					Jugador.class

			)

					.setParameter("gameId", gameId)

					.getResultList();

			List<Integer> playerIds = new ArrayList<>();

			for (Jugador player : players) {

				playerIds.add(player.getId());

			}

			session.getTransaction().commit();

			return playerIds;

		} catch (RuntimeException e) {

			if (session.getTransaction().isActive()) {

				session.getTransaction().rollback();

			}

			throw e;

		}

	}

}