package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.List;

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

            System.out.println("--- MÉTODES JUGADOR ---\n");

            System.out.println("--- MÉTODES PARTIDA ---\n");

            // 1. Iniciar partida
            System.out.println("1. Iniciar Partida:");

            int idPartida = partidaDao.iniciarPartida(nomsJugadors);

            if (idPartida == -1) {
                System.out.println("No se ha podido iniciar la partida.");
                return;
            }

            System.out.println("Partida creada con ID: " + idPartida);

            // Obtener jugadores de la partida
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

            // IDs de ejemplo: deben existir en la base de datos
            int idAresta = 1;
            int idVertex = 1;
            int idHexagon = 1;

            // 2. Mostrar partida
            System.out.println("\n2. Mostrar Partida:");
            System.out.println(partidaDao.mostrarPartida(idPartida));

            // 3. Mostrar tablero
            System.out.println("\n3. Mostrar Tauler:");
            System.out.println(partidaDao.mostrarTauler(idPartida));

            // 4. Mostrar recursos
            System.out.println("\n4. Mostrar Recursos:");
            System.out.println(jugadorDao.mostrarRecursos(idJugador));

            // 5. Construir carretera
            System.out.println("\n5. Construir Carretera:");
            System.out.println(jugadorDao.construirCarretera(
                    idJugador, idAresta));

            // 6. Construir poblado
            System.out.println("\n6. Construir Poblat:");
            System.out.println(jugadorDao.construirPoblat(
                    idJugador, idVertex));

            // 7. Mejorar a ciudad
            System.out.println("\n7. Millorar A Ciutat:");
            System.out.println(jugadorDao.millorarACiutat(
                    idJugador, idVertex));

            // 8. Comercio con la banca
            System.out.println("\n8. Comerciar Banca:");
            System.out.println(jugadorDao.comerciarBanca(
                    idJugador, TipusRecurs.FUSTA, TipusRecurs.BLAT));

            // 9. Calcular puntos
            System.out.println("\n9. Calcular Punts:");
            System.out.println(jugadorDao.calcularPunts(idJugador));

            // 10. Pasar turno
            System.out.println("\n10. Passar Torn:");
            System.out.println(jugadorDao.passarTorn(idJugador));

            // 11. Vértices disponibles
            System.out.println("\n11. Vertexs Disponibles:");
            System.out.println(jugadorDao.vertexsDisponibles(idJugador));

            // 12. Mover ladrón (pendiente de implementar)
            System.out.println("\n12. Moure Lladre:");
            System.out.println("Pendiente de implementar.");

            // 13. Comprar carta (pendiente de implementar)
            System.out.println("\n13. Comprar Carta:");
            System.out.println("Pendiente de implementar.");

            // 14. Jugar carta (pendiente de implementar)
            System.out.println("\n14. Jugar Carta:");
            System.out.println("Pendiente de implementar.");

            // 15. Tirar dados
            System.out.println("\n15. Tirar Daus:");
            System.out.println(partidaDao.tirarDaus(idPartida));

            // 16. Comprobar victoria
            System.out.println("\n16. Comprovar Victoria:");
            partidaDao.comprovarVictoria(idPartida);

            // 17. Ejército más grande
            System.out.println("\n17. Exercit Mes Gran:");
            partidaDao.exercitMesGran(idPartida);

            // 18. Mostrar tablero
            System.out.println("\n18. Mostrar Tauler:");
            System.out.println(partidaDao.mostrarTauler(idPartida));

            // 19. Mostrar partida
            System.out.println("\n19. Mostrar Partida:");
            System.out.println(partidaDao.mostrarPartida(idPartida));

        } catch (RuntimeException e) {

            System.out.println("Error durante las pruebas:");
            e.printStackTrace();

        } finally {
            SessionManager.tancar();
        }
    }

    private static List<Integer> obtenirIdsJugadors(int idPartida) {

        org.hibernate.Session session =
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
