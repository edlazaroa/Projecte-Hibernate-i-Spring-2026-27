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

public class IPartida {
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

            // 2. Jugadors: ordre aleatori, color segons l'ordre
            List<String> noms = new ArrayList<>(nomsJugadors);
            Collections.shuffle(noms);
            for (int i = 0; i < noms.size(); i++) {
                Jugador j = new Jugador();
                j.setNom(noms.get(i));
                j.setOrdreTorn(i + 1);
                j.setColor(ColorJugador.values()[i]);
                j.setCavallersJugats(0);
                j.setPartida(partida);
                session.persist(j);

                for (TipusRecurs tipus : TipusRecurs.values()) {
                    RecursJugador r = new RecursJugador();
                    r.setJugador(j);
                    r.setTipus(tipus);
                    r.setQuantitat(0);
                    session.persist(r);
                }
            }

            // 3. Topologia des dels CSV (hexàgons, vèrtexs, arestes, hexagon_vertex)
            List<Hexagon> hexagons = carregarTopologia(session, partida);

            // 4. Terrenys, números i lladre
            assignarTerrenysINumeros(hexagons, partida);

            // 5. Pila de cartes (només per a la part avançada)
            crearPilaCartes(session, partida);

            tx.commit();
            return partida.getId();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    private void assignarTerrenysINumeros(List<Hexagon> hexagons, Partida partida) {
        List<TipusTerreny> terrenys = new ArrayList<>();
        afegir(terrenys, TipusTerreny.BOSC, 4);
        afegir(terrenys, TipusTerreny.PASTURES, 4);
        afegir(terrenys, TipusTerreny.CAMPS, 4);
        afegir(terrenys, TipusTerreny.TURONS, 3);
        afegir(terrenys, TipusTerreny.MUNTANYES, 3);
        afegir(terrenys, TipusTerreny.DESERT, 1);
        Collections.shuffle(terrenys);

        List<Integer> numeros = new ArrayList<>(
            List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        Collections.shuffle(numeros);

        int idxNumero = 0;
        for (int i = 0; i < hexagons.size(); i++) {
            Hexagon h = hexagons.get(i);
            h.setTerreny(terrenys.get(i));
            if (terrenys.get(i) == TipusTerreny.DESERT) {
                h.setNumero(null);
                partida.setLladre(h);
            } else {
                h.setNumero(numeros.get(idxNumero++));
            }
        }
    }

    private void afegir(List<TipusTerreny> llista, TipusTerreny t, int vegades) {
        for (int i = 0; i < vegades; i++) llista.add(t);
    }

    // TODO: llegir els 4 CSV, crear Hexagon, Vertex, Aresta i HexagonVertex
    // lligats a la partida, i tornar la llista de hexàgons
    private List<Hexagon> carregarTopologia(Session session, Partida partida) {
        return null;
    }

    // TODO: 14 CAVALLER, 5 PUNT_VICTORIA, 2 MONOPOLI, sense propietari
    private void crearPilaCartes(Session session, Partida partida) {
    }
    
    
    
    public String MostrarPartida () {
	List<partida> Partidas = Session.createQuery(
			"From Partida p where p.Partida.id = :idPartida",
			EstatPartida.class)
			.setParameter("idPartida", idPartida)
			.getResultList();
		
	List <jugador> Jugadors = ArrayList<>();
		
		for (pj : partida.jugador) {
		Jugadors.add(Session.createQuery(
				"From Jugadors j where j.id = idJugador")
				.setParameter("j.id", idJugador)
				.getResultList());
		}
		
		
		List <jugador> JugadorActiu = ArrayList<>();
		for (j : Jugadors) {		
		JugadorActiu.add(Session.createQuery(
				"From jugadors j where j.id = idJugador and j.estat = 'ACTIU'",
				JugadorActiu.class)
				.setParameter("idJugador", idJugador)
				.getResultList());
		}
		
		
		
		return "Estat de la partida: " + partida.estat + 
				" \n Ronda: " + partida.ronda + 
				" \n Jugador actiu: " + JugadorActiu.nombre + 
				" \n Ultima tirada: " + partida.tirada + 
				" \n Punts victoria: " + jugador.puntsVictoria + 
				" \n Poblats: " + jugador.poblats + 
				" \n Carreteres: " + jugador.carreteres + 
				" \n Recursos Jugador: " + jugador.recursos; 
		
	}
    
    
    public String MostrarTauler() {
    	List<Hexagons> Hexagon = new ArrayList<>();
    	
    	List<vertex> Vertexs = Session.createQuery(
    			"select construccio From Vertex where Vertex.id = :idVertex ORDER BY construccion",
    			ConstruccionsVertex.class)
    			.setParameter("idVertex", idVertex)
    			.getResultList();
    	
    	for (hexagon : Hexagons) {
    		boolean mostrarNum = false;
    		if (hexagon.jugador.tipus == "LLADRE") {
    			mostrarNum = true;
    		}
    		if (mostrarNum == false) {
    			return hexagon.terreny + " \n Construccions: " + vertex.construccions;
    		}
    		else {
    			return hexagon.terreny + " " + hexagon.numero + " \n Construccions: " + vertex.construccions;
    		}
    	}	
    }
}
