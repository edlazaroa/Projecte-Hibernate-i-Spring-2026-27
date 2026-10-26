package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.mysql.cj.Session;

import jakarta.transaction.Transaction;

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
            Partida partida = new partida();
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
	
	@Override
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

@Override
public String mostrarPartida(int idPartida) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        Partida partida = session.get(Partida.class, idPartida);
        if (partida == null) {
            return "La partida no existeix";
        }
 
        StringBuilder sb = new StringBuilder();
        sb.append("Estat de la partida: ").append(partida.getEstat()).append("\n");
        sb.append("Ronda: ").append(partida.getRonda()).append("\n");
 
        Jugador actiu = partida.getActiu();
        sb.append("Jugador actiu: ").append(actiu != null ? actiu.getNom() : "cap").append("\n");
 
        Integer tirada = partida.getUltimaTirada();
        sb.append("Última tirada: ").append(tirada != null ? tirada : "encara no hi ha tirada").append("\n");
 
        // Jugadors de la partida, en ordre de torn
        List<Jugador> jugadors = session.createQuery(
                "FROM Jugador j WHERE j.partida = :partida ORDER BY j.ordreTorn", Jugador.class)
            .setParameter("partida", partida)
            .getResultList();
 
        for (Jugador j : jugadors) {
            long poblats = comptarConstruccions(session, "Poblat", j);
            long ciutats = comptarConstruccions(session, "Ciutat", j);
            long carreteres = comptarConstruccions(session, "Carretera", j);
 
            // Cartes PUNT_VICTORIA a la mà: 1 punt cadascuna
            long cartesPunt = session.createQuery(
                    "SELECT COUNT(c) FROM CartaDesenvolupament c "
                    + "WHERE c.propietari = :jugador AND c.tipus = :tipus", Long.class)
                .setParameter("jugador", j)
                .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                .getSingleResult();
 
            // TODO (avançat): sumar 2 punts si j és el jugador de exercitMesGran
            long punts = poblats + 2 * ciutats + cartesPunt;
 
            // Només el total de recursos: els tipus són secrets
            long totalRecursos = session.createQuery(
                    "SELECT COALESCE(SUM(r.quantitat), 0) FROM RecursJugador r WHERE r.jugador = :jugador",
                    Long.class)
                .setParameter("jugador", j)
                .getSingleResult();
 
            sb.append("\n").append(j.getNom()).append(" (").append(j.getColor()).append(")\n");
            sb.append("  Punts de victòria: ").append(punts).append("\n");
            sb.append("  Poblats: ").append(poblats).append("\n");
            sb.append("  Ciutats: ").append(ciutats).append("\n");
            sb.append("  Carreteres: ").append(carreteres).append("\n");
            sb.append("  Total de recursos: ").append(totalRecursos).append("\n");
        }
        return sb.toString();
    }
}
 
/** Compta les construccions d'un tipus (Poblat, Ciutat o Carretera) d'un jugador. */
private long comptarConstruccions(Session session, String entitat, Jugador jugador) {
    // "entitat" és sempre una constant del codi, mai un valor de l'usuari
    return session.createQuery(
            "SELECT COUNT(c) FROM " + entitat + " c WHERE c.propietari = :jugador", Long.class)
        .setParameter("jugador", jugador)
        .getSingleResult();
}
 
@Override
public String mostrarTauler(int idPartida) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        Partida partida = session.get(Partida.class, idPartida);
        if (partida == null) {
            return "La partida no existeix";
        }
 
        Integer idLladre = (partida.getLladre() != null) ? partida.getLladre().getId() : null;
 
        List<Hexagon> hexagons = session.createQuery(
                "FROM Hexagon h WHERE h.partida = :partida ORDER BY h.codi", Hexagon.class)
            .setParameter("partida", partida)
            .getResultList();
 
        StringBuilder sb = new StringBuilder();
        for (Hexagon h : hexagons) {
            sb.append("Hexàgon ").append(h.getCodi()).append(": ").append(h.getTerreny());
            sb.append(" | número: ").append(h.getNumero() != null ? h.getNumero() : "-");
            if (h.getId().equals(idLladre)) {
                sb.append(" | LLADRE");
            }
            sb.append("\n");
 
            // Construccions (poblats i ciutats) als vèrtexs d'aquest hexàgon, amb el propietari
            List<Assentament> assentaments = session.createQuery(
                    "SELECT a FROM Assentament a WHERE a.vertex IN "
                    + "(SELECT hv.vertex FROM HexagonVertex hv WHERE hv.hexagon = :hexagon) "
                    + "ORDER BY a.vertex.codi", Assentament.class)
                .setParameter("hexagon", h)
                .getResultList();
 
            if (assentaments.isEmpty()) {
                sb.append("   Sense construccions\n");
            }
            for (Assentament a : assentaments) {
                String tipus = (a instanceof Ciutat) ? "Ciutat" : "Poblat";
                sb.append("   ").append(tipus)
                  .append(" al vèrtex ").append(a.getVertex().getCodi())
                  .append(" de ").append(a.getPropietari().getNom())
                  .append(" (").append(a.getPropietari().getColor()).append(")\n");
            }
        }
        return sb.toString();
    }
}

	// Posa-ho a false si encara no has fet la part avançada (lladre i descart amb un 7)
	private static final boolean PART_AVANCADA = true;

	@Override
	public String tirarDaus(int idPartida) {
	    Transaction tx = null;
	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        tx = session.beginTransaction();

	        // 1. Validacions
	        Partida partida = session.get(Partida.class, idPartida);
	        String error = validarTirada(partida);
	        if (error != null) {
	            tx.rollback();
	            return error;
	        }

	        // 2. Tirada: es guarda a la partida i es marca dausTirats
	        int tirada = tirarDosDaus();
	        partida.setUltimaTirada(tirada);
	        partida.setDausTirats(true);

	        // 3. Resum: id del jugador -> recursos rebuts (acumulats) i recursos descartats
	        List<Jugador> jugadors = new ArrayList<>(partida.getJugadors());
	        jugadors.sort(Comparator.comparing(Jugador::getOrdreTorn));

	        Map<Integer, Map<TipusRecurs, Integer>> rebuts = new LinkedHashMap<>();
	        Map<Integer, Integer> descartats = new LinkedHashMap<>();
	        for (Jugador j : jugadors) {
	            rebuts.put(j.getId(), new LinkedHashMap<>());
	        }

	        // 4. Producció o 7
	        if (tirada == 7) {
	            if (PART_AVANCADA) {
	                aplicarDescart(jugadors, descartats);
	                partida.setLladrePendent(true);
	            }
	        } else {
	            produir(session, partida, tirada, rebuts);
	        }

	        tx.commit();
	        return construirResum(tirada, jugadors, rebuts, descartats);
	    } catch (RuntimeException e) {
	        if (tx != null) tx.rollback();
	        throw e;
	    }
	}

	/** Torna null si es pot tirar, o el motiu pel qual no es pot. */
	private String validarTirada(Partida partida) {
	    if (partida == null) return "La partida no existeix";
	    if (partida.getEstat() != EstatPartida.EN_CURS) return "La partida no està en curs";
	    if (partida.getActiu() == null) return "La partida no té jugador actiu";
	    if (partida.isDausTirats()) return "El jugador actiu ja ha tirat els daus aquest torn";
	    return null;
	}

	private int tirarDosDaus() {
	    int dau1 = ThreadLocalRandom.current().nextInt(1, 7);
	    int dau2 = ThreadLocalRandom.current().nextInt(1, 7);
	    return dau1 + dau2;
	}

	/** Reparteix els recursos dels hexàgons amb aquest número que no tinguin el lladre. */
	private void produir(Session session, Partida partida, int tirada,
	        Map<Integer, Map<TipusRecurs, Integer>> rebuts) {

	    int idLladre = (partida.getLladre() != null) ? partida.getLladre().getId() : -1;

	    List<Hexagon> hexagons = session.createQuery(
	            "FROM Hexagon h WHERE h.partida = :partida AND h.numero = :numero AND h.id <> :idLladre",
	            Hexagon.class)
	        .setParameter("partida", partida)
	        .setParameter("numero", tirada)
	        .setParameter("idLladre", idLladre)
	        .getResultList();

	    for (Hexagon h : hexagons) {
	        TipusRecurs recurs = recursDeTerreny(h.getTerreny());
	        if (recurs == null) continue; // desert

	        // Assentaments (poblats i ciutats) als vèrtexs d'aquest hexàgon
	        List<Assentament> assentaments = session.createQuery(
	                "SELECT a FROM Assentament a WHERE a.vertex IN "
	                + "(SELECT hv.vertex FROM HexagonVertex hv WHERE hv.hexagon = :hexagon)",
	                Assentament.class)
	            .setParameter("hexagon", h)
	            .getResultList();

	        for (Assentament a : assentaments) {
	            int quantitat = (a instanceof Ciutat) ? 2 : 1;
	            Jugador propietari = a.getPropietari();
	            afegirRecurs(session, propietari, recurs, quantitat);
	            rebuts.get(propietari.getId()).merge(recurs, quantitat, Integer::sum);
	        }
	    }
	}

	private TipusRecurs recursDeTerreny(TipusTerreny terreny) {
	    switch (terreny) {
	        case BOSC:      return TipusRecurs.FUSTA;
	        case TURONS:    return TipusRecurs.MAO;
	        case PASTURES:  return TipusRecurs.LLANA;
	        case CAMPS:     return TipusRecurs.BLAT;
	        case MUNTANYES: return TipusRecurs.MINERAL;
	        default:        return null; // DESERT no produeix
	    }
	}

	private void afegirRecurs(Session session, Jugador jugador, TipusRecurs tipus, int quantitat) {
	    RecursJugador fila = session.createQuery(
	            "FROM RecursJugador r WHERE r.jugador = :jugador AND r.tipus = :tipus",
	            RecursJugador.class)
	        .setParameter("jugador", jugador)
	        .setParameter("tipus", tipus)
	        .getSingleResult();
	    // Hibernate desa el canvi en fer commit (la fila està gestionada per la sessió)
	    fila.setQuantitat(fila.getQuantitat() + quantitat);
	}

	/** Part avançada: qui té més de 7 recursos descarta la meitat (cap avall), a l'atzar. */
	private void aplicarDescart(List<Jugador> jugadors, Map<Integer, Integer> descartats) {
	    for (Jugador j : jugadors) {
	        int total = 0;
	        List<RecursJugador> bossa = new ArrayList<>(); // una entrada per cada unitat
	        for (RecursJugador fila : j.getRecursos()) {
	            total += fila.getQuantitat();
	            for (int i = 0; i < fila.getQuantitat(); i++) {
	                bossa.add(fila);
	            }
	        }
	        if (total > 7) {
	            int adescartar = total / 2; // divisió entera: arrodoneix cap avall
	            Collections.shuffle(bossa);
	            for (int i = 0; i < adescartar; i++) {
	                RecursJugador fila = bossa.get(i);
	                fila.setQuantitat(fila.getQuantitat() - 1);
	            }
	            descartats.put(j.getId(), adescartar);
	        }
	    }
	}

	private String construirResum(int tirada, List<Jugador> jugadors,
	        Map<Integer, Map<TipusRecurs, Integer>> rebuts, Map<Integer, Integer> descartats) {

	    StringBuilder sb = new StringBuilder("Tirada de daus: " + tirada + "\n");

	    if (tirada == 7) {
	        sb.append("Ha sortit un 7: no hi ha producció.\n");
	        for (Jugador j : jugadors) {
	            Integer n = descartats.get(j.getId());
	            if (n != null) {
	                sb.append(j.getNom()).append(" descarta ").append(n).append(" recursos\n");
	            }
	        }
	        if (PART_AVANCADA) {
	            sb.append("El jugador actiu ha de moure el lladre.\n");
	        }
	        return sb.toString();
	    }

	    for (Jugador j : jugadors) {
	        Map<TipusRecurs, Integer> rep = rebuts.get(j.getId());
	        sb.append(j.getNom()).append(": ");
	        if (rep.isEmpty()) {
	            sb.append("no ha rebut res");
	        } else {
	            sb.append("ha rebut ").append(rep);
	        }
	        sb.append("\n");
	    }
	    return sb.toString();
	}
	
	@Override
	public void comprovarVictoria(int idPartida) {
		Partida partida = session.get(Partida.class, idPartida);
		List<Jugador> jugadors = new ArrayList<>(partida.getJugadors());
			for (Jugador j : jugadors) {
				String punts = calcularPunts(j.getId());
				int puntsTotals = Integer.parseInt(punts);
				if (puntsTotals >= 100) {
					j.setGuanyador = true; // Tinc que veure la variable que determina el guanyador
					partida.setEstat(EstatPartida.FINALITZADA);
					System.out.println("El jugador " + j.getNom() + " Es el guanyador!! ");
				}
			}
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
		// TINC QUE FER QUE ES CALCULIN ELS PUNTS
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
