
package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

public class Main {

    public static void main(String[] args) {

        PartidaDao partidaDao = new PartidaDao();
        JugadorDao jugadorDao = new JugadorDao();

        List<String> nomsJugadors = new ArrayList<>();
        nomsJugadors.add("Gabriel");
        nomsJugadors.add("Alex");
        nomsJugadors.add("Dennis");
        nomsJugadors.add("Ismael");

        try {

            // 1. INICIAR PARTIDA
            System.out.println("--- MÉTODES PARTIDA ---\n");
            System.out.println("1. Iniciar Partida:");

            int idPartida = partidaDao.iniciarPartida(nomsJugadors);

            if (idPartida == -1) {
                System.out.println("No se ha podido iniciar la partida.");
                return;
            }

            System.out.println("Partida creada con ID: " + idPartida);

            // 2. OBTENER IDS DE LOS JUGADORES
            List<Integer> idsJugadors = obtenirIdsJugadors(idPartida);

            if (idsJugadors.isEmpty()) {
                System.out.println("No hay jugadores registrados.");
                return;
            }

            int idJugador = idsJugadors.get(0);

            int idJugadorVictima = idsJugadors.size() > 1
                    ? idsJugadors.get(1) : idJugador;

            System.out.println("\nJugadores:");

            for (Integer id : idsJugadors) {
                System.out.println("ID jugador: " + id);
            }

            // IDs de prueba
            int idAresta = 1;
            int idVertex = 1;
            int idHexagon = 1;

            // 3. MÉTODOS DE JUGADOR
            System.out.println("\n--- MÉTODES JUGADOR ---\n");

            System.out.println("1. Mostrar recursos:");
            System.out.println(jugadorDao.mostrarRecursos(idJugador));

            System.out.println("\n2. Construir carretera:");
            System.out.println(jugadorDao.construirCarretera(
                    idJugador, idAresta));

            System.out.println("\n3. Construir poblado:");
            System.out.println(jugadorDao.construirPoblat(
                    idJugador, idVertex));

            System.out.println("\n4. Mejorar a ciudad:");
            System.out.println(jugadorDao.millorarACiutat(
                    idJugador, idVertex));

            System.out.println("\n5. Comerciar con la banca:");
            System.out.println(jugadorDao.comerciarBanca(
                    idJugador,
                    TipusRecurs.FUSTA,
                    TipusRecurs.BLAT));

            System.out.println("\n6. Calcular puntos:");
            System.out.println(jugadorDao.calcularPunts(idJugador));

            System.out.println("\n7. Pasar turno:");
            System.out.println(jugadorDao.passarTorn(idJugador));

            System.out.println("\n8. Vértices disponibles:");
            System.out.println(jugadorDao.vertexsDisponibles(idJugador));

            // 4. RESTO DE MÉTODOS DE PARTIDA
            System.out.println("\n--- MÉTODES PARTIDA ---\n");

            System.out.println("2. Mostrar Partida:");
            System.out.println(partidaDao.mostrarPartida(idPartida));

            System.out.println("\n3. Mostrar Tablero:");
            System.out.println(partidaDao.mostrarTauler(idPartida));

            System.out.println("\n4. Mover ladrón:");
            System.out.println("Pendiente de implementar.");

            System.out.println("\n5. Comprar carta:");
            System.out.println("Pendiente de implementar.");

            System.out.println("\n6. Jugar carta:");
            System.out.println("Pendiente de implementar.");

            System.out.println("\n7. Tirar dados:");
            System.out.println(partidaDao.tirarDaus(idPartida));

            System.out.println("\n8. Comprobar victoria:");
            partidaDao.comprovarVictoria(idPartida);

            System.out.println("\n9. Ejército más grande:");
            partidaDao.exercitMesGran(idPartida);

            System.out.println("\n10. Mostrar tablero final:");
            System.out.println(partidaDao.mostrarTauler(idPartida));

            System.out.println("\n11. Mostrar partida final:");
            System.out.println(partidaDao.mostrarPartida(idPartida));

        } catch (RuntimeException e) {

            System.out.println("Error durante las pruebas:");
            e.printStackTrace();

        } finally {

            SessionManager.tancar();

        }
    }

    private static List<Integer> obtenirIdsJugadors(int idPartida) {

        Session session =
                SessionManager.getSessionFactory().getCurrentSession();

        try {

            session.beginTransaction();

            List<Jugador> jugadors = session.createQuery(
                    "FROM Jugador j "
                    + "WHERE j.partida.partida_id = :idPartida "
                    + "ORDER BY j.ordreTorn",
                    Jugador.class
            )
            .setParameter("idPartida", idPartida)
            .getResultList();

            List<Integer> ids = new ArrayList<>();

            for (Jugador jugador : jugadors) {
                ids.add(jugador.getId());
            }

            session.getTransaction().commit();

            return ids;

        } catch (RuntimeException e) {

            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }

            throw e;
        }
    }
}
