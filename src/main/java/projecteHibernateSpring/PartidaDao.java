package projecteHibernateSpring;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.hibernate.Hibernate;
import org.hibernate.Session;

public class PartidaDao extends GenericDao<Partida, Integer> implements IPartida {

	private static final boolean PART_AVANCADA = false;

	public PartidaDao() {
		super();
	}

	private String acabar(Session session, String missatge) {
		session.getTransaction().commit();
		return missatge;
	}

	@Override
	public int iniciarPartida(List<String> nomsJugadors) {
		if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
			throw new IllegalArgumentException("Hi ha d'haver entre 2 i 4 jugadors");
		}
		if (new HashSet<>(nomsJugadors).size() != nomsJugadors.size()) {
			throw new IllegalArgumentException("Els noms dels jugadors no es poden repetir");
		}

		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			// 1. Partida (dataInici és java.sql.Date a la teva entitat)
			Partida partida = new Partida();
			partida.setEstat(EstatPartida.PREPARACIO);
			partida.setDataInici(new Date(System.currentTimeMillis()));
			partida.setRonda(1);
			partida.setPuntsPerGuanyar(10);
			partida.setDausTirats(false);
			partida.setLladrePendent(false);
			partida.setUltimaTirada(0);
			session.persist(partida); // AQUI HAY FALLO

			// 2. Jugadors: ordre aleatori, color segons l'ordre, inventari a 0
			List<String> noms = new ArrayList<>(nomsJugadors);
			Collections.shuffle(noms);
			Jugador primer = null;
			for (int i = 0; i < noms.size(); i++) {
				Jugador j = new Jugador();
				j.setNom(noms.get(i));
				j.setOrdreTorn(i + 1);
				j.setColor(ColorJugador.values()[i]);
				j.setCavallersJugats(0);
				j.setPartida(partida);
				session.persist(j);
				if (i == 0) {
					primer = j;
				}

				for (TipusRecurs tipus : TipusRecurs.values()) {
					RecursJugador r = new RecursJugador();
					r.setJugador(j);
					r.setTipus(tipus);
					r.setQuantitat(0);
					session.persist(r);
				}
			}
			// Comença el jugador d'ordreTorn 1 (sense actiu, tirarDaus i passarTorn no funcionarien)
			partida.setActiu(primer);

			// 3. Topologia des dels CSV (hexàgons, vèrtexs, arestes, hexagon_vertex)
			List<Hexagon> hexagons = carregarTopologia(session, partida);

			// 4. Terrenys, números i lladre
			assignarTerrenysINumeros(hexagons, partida);

			// 5. Pila de cartes (només per a la part avançada)
			crearPilaCartes(session, partida);

			session.getTransaction().commit();
			return partida.getId();
		} catch (RuntimeException e) {
			handleException(session, e);
			return -1;
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
			    h.setNumero(0); // Usa 0 en lugar de null si la columna es NOT NULL
			    partida.setLladreId(h);
			} else {
			    h.setNumero(numeros.get(idxNumero++));
			}
		}
	}

	private void afegir(List<TipusTerreny> llista, TipusTerreny t, int vegades) {
		for (int i = 0; i < vegades; i++) {
			llista.add(t);
		}
	}

	// a la partida (session.persist de cadascun) i tornar la llista dels 19 hexàgons
	private List<Hexagon> carregarTopologia(Session session, Partida partida) {
	    List<Hexagon> hexagons = new ArrayList<>();
	    Map<Integer, Hexagon> mapHexagons = new HashMap<>();
	    Map<Integer, Vertex> mapVertices = new HashMap<>();

	    try {
	        // 1. Cargar Hexágonos (dades/hexagons.csv)
	    	// 1. Cargar Hexágonos (dades/hexagons.csv)
	    	try (BufferedReader br = obrirCSV("dades/hexagons.csv")) {
	    	    String line;
	    	    while ((line = br.readLine()) != null) {
	    	        String trimmed = line.trim();
	    	        // Saltar líneas vacías o cabeceras que contengan texto no numérico
	    	        if (trimmed.isEmpty() || trimmed.startsWith("codi") || trimmed.startsWith("id_hexagon") || trimmed.startsWith("hexagon_id")) {
	    	            continue;
	    	        }
	    	        
	    	        // Cambiar el separador de ';' a ','
	    	        String[] parts = trimmed.split(",");
	    	        int codi = Integer.parseInt(parts[0].trim());
	    	        int q = Integer.parseInt(parts[1].trim());
	    	        int r = Integer.parseInt(parts[2].trim());

	    	        Hexagon h = new Hexagon();
	    	        h.setCodi(codi);
	    	        h.setQ(q);
	    	        h.setR(r);
	    	        h.setPartida(partida);
	    	        
	    	        // Valores provisionales obligatorios
	    	        h.setTerreny(TipusTerreny.BOSC); 
	    	        h.setNumero(0);

	    	        session.persist(h);
	    	        hexagons.add(h);
	    	        mapHexagons.put(codi, h);
	    	    }
	    	}

	    	// 2. Cargar Vértices (dades/vertexs.csv)
	    	try (BufferedReader br = obrirCSV("dades/vertexs.csv")) {
	    	    String line;
	    	    while ((line = br.readLine()) != null) {
	    	        String trimmed = line.trim();
	    	        if (trimmed.isEmpty() || trimmed.startsWith("codi") || trimmed.startsWith("id_vertex") || trimmed.startsWith("vertex_id")) {
	    	            continue;
	    	        }
	    	        int codi = Integer.parseInt(trimmed.split(",")[0].trim());

	    	        Vertex v = new Vertex();
	    	        v.setCodi(codi);
	    	        v.setPartida(partida);

	    	        session.persist(v);
	    	        mapVertices.put(codi, v);
	    	    }
	    	}

	    	// 3. Cargar Aristas (dades/arestes.csv)
	    	try (BufferedReader br = obrirCSV("dades/arestes.csv")) {
	    	    String line;
	    	    while ((line = br.readLine()) != null) {
	    	        String trimmed = line.trim();
	    	        if (trimmed.isEmpty() || trimmed.startsWith("codi") || trimmed.startsWith("id_aresta") || trimmed.startsWith("aresta_id")) {
	    	            continue;
	    	        }
	    	        String[] parts = trimmed.split(",");
	    	        int codi = Integer.parseInt(parts[0].trim());
	    	        int codiVertexA = Integer.parseInt(parts[1].trim());
	    	        int codiVertexB = Integer.parseInt(parts[2].trim());

	    	        Aresta a = new Aresta();
	    	        a.setCodi(codi);
	    	        a.setVertexA(mapVertices.get(codiVertexA));
	    	        a.setVertexB(mapVertices.get(codiVertexB));
	    	        a.setPartida(partida);

	    	        session.persist(a);
	    	    }
	    	}

	    	// 4. Cargar HexagonVertex (dades/hexagon_vertex.csv)
	    	try (BufferedReader br = obrirCSV("dades/hexagon_vertex.csv")) {
	    	    String line;
	    	    while ((line = br.readLine()) != null) {
	    	        String trimmed = line.trim();
	    	        if (trimmed.isEmpty() || trimmed.startsWith("codi") || trimmed.startsWith("id_hexagon") || trimmed.startsWith("hexagon_id")) {
	    	            continue;
	    	        }
	    	        String[] parts = trimmed.split(",");
	    	        int codiHexagon = Integer.parseInt(parts[0].trim());
	    	        int codiVertex = Integer.parseInt(parts[1].trim());
	    	        int posicio = Integer.parseInt(parts[2].trim());

	    	        HexagonVertex hv = new HexagonVertex();
	    	        hv.setHexagon(mapHexagons.get(codiHexagon));
	    	        hv.setVertex(mapVertices.get(codiVertex));
	    	        hv.setPosicio(posicio);

	    	        session.persist(hv);
	    	    }
	    	}

	    } catch (IOException e) {
	        throw new RuntimeException("Error en carregar la topologia des dels fitxers CSV", e);
	    }

	    return hexagons;
	}

	/** Método auxiliar para abrir los CSV desde el Classpath (src/main/resources) */
	private BufferedReader obrirCSV(String ruta) throws IOException {
	    InputStream is = getClass().getClassLoader().getResourceAsStream(ruta);
	    if (is == null) {
	        throw new FileNotFoundException("No s'ha trobat el fitxer en el classpath: " + ruta);
	    }
	    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	}

	// TODO: 14 CAVALLER, 5 PUNT_VICTORIA, 2 MONOPOLI, sense propietari (només part avançada)
	private void crearPilaCartes(Session session, Partida partida) {
	}

	@Override
	public String mostrarPartida(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			Partida partida = session.get(Partida.class, idPartida);
			if (partida == null) {
				return acabar(session, "La partida no existeix");
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Estat de la partida: ").append(partida.getEstat()).append("\n");
			sb.append("Ronda: ").append(partida.getRonda()).append("\n");

			Jugador actiu = partida.getActiu();
			sb.append("Jugador actiu: ").append(actiu != null ? actiu.getNom() : "cap").append("\n");

			Integer tirada = partida.getUltimaTirada();
			sb.append("Última tirada: ").append(tirada != null ? tirada : "encara no hi ha tirada").append("\n");

			for (Jugador j : jugadorsDeLaPartida(session, partida)) {
				// Només el total de recursos: els tipus són secrets
				Long totalRecursos = session.createQuery(
						"SELECT COALESCE(SUM(r.quantitat), 0) FROM RecursJugador r WHERE r.jugador = :jugador",
						Long.class)
					.setParameter("jugador", j)
					.getSingleResult();

				sb.append("\n").append(j.getNom()).append(" (").append(j.getColor()).append(")\n");
				sb.append("  Punts de victòria: ").append(calcularPunts(session, j)).append("\n");
				sb.append("  Poblats: ").append(comptar(session, "Poblat", j)).append("\n");
				sb.append("  Ciutats: ").append(comptar(session, "Ciutat", j)).append("\n");
				sb.append("  Carreteres: ").append(comptar(session, "Carretera", j)).append("\n");
				sb.append("  Total de recursos: ").append(totalRecursos).append("\n");
			}

			session.getTransaction().commit();
			return sb.toString();
		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error en consultar la partida.";
		}
	}

	@Override
	public String mostrarTauler(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			Partida partida = session.get(Partida.class, idPartida);
			if (partida == null) {
				return acabar(session, "La partida no existeix");
			}

			// El lladre és un camp de Partida (no un tipus de jugador)
			Integer idLladre = (partida.getLladreId() != null) ? partida.getLladreId().getHexagon_id() : null;

			List<Hexagon> hexagons = session.createQuery(
					"FROM Hexagon h WHERE h.partida = :partida ORDER BY h.codi", Hexagon.class)
				.setParameter("partida", partida)
				.getResultList();

			StringBuilder sb = new StringBuilder();
			for (Hexagon h : hexagons) {
				sb.append("Hexàgon ").append(h.getCodi()).append(": ").append(h.getTerreny());
				sb.append(" | número: ").append(h.getNumero() != null ? h.getNumero() : "-");
				if (h.getHexagon_id().equals(idLladre)) {
					sb.append(" | LLADRE");
				}
				sb.append("\n");

				// Construccions (poblats i ciutats) als vèrtexs d'aquest hexàgon, amb el propietari
				List<Assentament> assentaments = assentamentsDelHexagon(session, h);
				if (assentaments.isEmpty()) {
					sb.append("   Sense construccions\n");
				}
				for (Assentament a : assentaments) {
					String tipus = (Hibernate.unproxy(a) instanceof Ciutat) ? "Ciutat" : "Poblat";
					sb.append("   ").append(tipus)
					  .append(" al vèrtex ").append(a.getVertex().getCodi())
					  .append(" de ").append(a.getJugador().getNom())
					  .append(" (").append(a.getJugador().getColor()).append(")\n");
				}
			}

			session.getTransaction().commit();
			return sb.toString();
		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error en consultar el tauler.";
		}
	}

	@Override
	public String tirarDaus(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			// 1. Validacions
			Partida partida = session.get(Partida.class, idPartida);
			String error = validarTirada(partida);
			if (error != null) {
				return acabar(session, error);
			}

			// 2. Tirada: es guarda a la partida i es marca dausTirats
			int tirada = tirarDosDaus();
			partida.setUltimaTirada(tirada);
			partida.setDausTirats(true);

			// 3. Resum: id del jugador -> recursos rebuts (acumulats) i recursos descartats
			List<Jugador> jugadors = jugadorsDeLaPartida(session, partida);
			Map<Integer, Map<TipusRecurs, Integer>> rebuts = new LinkedHashMap<>();
			Map<Integer, Integer> descartats = new LinkedHashMap<>();
			for (Jugador j : jugadors) {
				rebuts.put(j.getId(), new LinkedHashMap<>());
			}

			// 4. Producció o 7
			if (tirada == 7) {
				if (PART_AVANCADA) {
					aplicarDescart(session, jugadors, descartats);
					partida.setLladrePendent(true);
				}
			} else {
				produir(session, partida, tirada, rebuts);
			}

			session.getTransaction().commit();
			return construirResum(tirada, jugadors, rebuts, descartats);
		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error en tirar els daus.";
		}
	}

	/** Torna null si es pot tirar, o el motiu pel qual no es pot. */
	private String validarTirada(Partida partida) {
		if (partida == null) return "La partida no existeix";
		if (partida.getEstat() == EstatPartida.FINALITZADA) return "La partida ja ha finalitzat";
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

		int idLladre = (partida.getLladreId() != null) ? partida.getLladreId().getHexagon_id() : -1;

		List<Hexagon> hexagons = session.createQuery(
				"FROM Hexagon h WHERE h.partida = :partida AND h.numero = :numero AND h.hexagon_id <> :idLladre",
				Hexagon.class)
			.setParameter("partida", partida)
			.setParameter("numero", tirada)
			.setParameter("idLladre", idLladre)
			.getResultList();

		for (Hexagon h : hexagons) {
			TipusRecurs recurs = recursDeTerreny(h.getTerreny());
			if (recurs == null) continue; // desert

			for (Assentament a : assentamentsDelHexagon(session, h)) {
				int quantitat = (Hibernate.unproxy(a) instanceof Ciutat) ? 2 : 1;
				Jugador propietari = a.getJugador();
				afegirRecurs(session, propietari, recurs, quantitat);
				rebuts.get(propietari.getId()).merge(recurs, quantitat, Integer::sum);
			}
		}
	}

	/** Part avançada: qui té més de 7 recursos descarta la meitat (cap avall), a l'atzar. */
	private void aplicarDescart(Session session, List<Jugador> jugadors, Map<Integer, Integer> descartats) {
		for (Jugador j : jugadors) {
			List<RecursJugador> files = session.createQuery(
					"FROM RecursJugador r WHERE r.jugador = :jugador", RecursJugador.class)
				.setParameter("jugador", j)
				.getResultList();

			int total = 0;
			List<RecursJugador> bossa = new ArrayList<>(); // una entrada per cada unitat
			for (RecursJugador fila : files) {
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

	// ========================================================================
	// comprovarVictoria i exercitMesGran
	// ========================================================================

	/**
	 * Comprova les condicions de victòria. Crida'l DESPRÉS del commit de l'acció
	 * (construir, millorar...), no des de dins de la transacció de JugadorDao.
	 */
	@Override
	public void comprovarVictoria(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			Partida partida = session.get(Partida.class, idPartida);
			if (partida != null && partida.getEstat() != EstatPartida.FINALITZADA) {
				for (Jugador j : jugadorsDeLaPartida(session, partida)) {
					if (calcularPunts(session, j) >= partida.getPuntsPerGuanyar()) {
						partida.setGuanyadorId(j);
						partida.setEstat(EstatPartida.FINALITZADA);
						System.out.println("El jugador " + j.getNom() + " és el guanyador!");
						break;
					}
				}
			}

			session.getTransaction().commit();
		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	/** Mostra el jugador amb l'exèrcit més gran (mínim 3 cavallers i sense empat). */
	@Override
	public void exercitMesGran(int idPartida) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			Partida partida = session.get(Partida.class, idPartida);
			if (partida == null) {
				System.out.println("La partida no existeix");
			} else {
				Jugador jugador = jugadorExercitMesGran(session, partida);
				if (jugador == null) {
					System.out.println("Cap jugador té l'exèrcit més gran");
				} else {
					System.out.println("El jugador amb l'exèrcit més gran és: " + jugador.getNom());
				}
			}

			session.getTransaction().commit();
		} catch (RuntimeException e) {
			handleException(session, e);
		}
	}

	// ========================================================================
	// Consultes i càlculs auxiliars (tots reben la Session de la transacció oberta)
	// ========================================================================

	private List<Jugador> jugadorsDeLaPartida(Session session, Partida partida) {
		return session.createQuery(
				"FROM Jugador j WHERE j.partida = :partida ORDER BY j.ordreTorn", Jugador.class)
			.setParameter("partida", partida)
			.getResultList();
	}

	/** Poblats i ciutats (poblat o ciutat) dels vèrtexs d'un hexàgon, ordenats per vèrtex. */
	private List<Assentament> assentamentsDelHexagon(Session session, Hexagon hexagon) {
		return session.createQuery(
				"SELECT a FROM Assentament a WHERE a.vertex IN "
				+ "(SELECT hv.vertex FROM HexagonVertex hv WHERE hv.hexagon = :hexagon) "
				+ "ORDER BY a.vertex.codi", Assentament.class)
			.setParameter("hexagon", hexagon)
			.getResultList();
	}

	/** Compta les construccions d'un jugador d'un tipus: "Poblat", "Ciutat" o "Carretera". */
	private long comptar(Session session, String entitat, Jugador jugador) {
		// "entitat" és sempre una constant del codi, mai un valor de l'usuari
		return session.createQuery(
				"SELECT COUNT(c) FROM " + entitat + " c WHERE c.jugador = :jugador", Long.class)
			.setParameter("jugador", jugador)
			.getSingleResult();
	}

	/** Punts: poblats 1, ciutats 2 (+ cartes PUNT_VICTORIA i exèrcit més gran a l'avançada). */
	private int calcularPunts(Session session, Jugador jugador) {
		int punts = (int) (comptar(session, "Poblat", jugador) + 2 * comptar(session, "Ciutat", jugador));

		if (PART_AVANCADA) {
			// Cal que CartaDesenvolupament tingui els atributs "jugador" i "tipus"
			Long cartesPunt = session.createQuery(
					"SELECT COUNT(c) FROM CartaDesenvolupament c "
					+ "WHERE c.jugador = :jugador AND c.tipus = :tipus", Long.class)
				.setParameter("jugador", jugador)
				.setParameter("tipus", TipusCarta.PUNT_VICTORIA)
				.getSingleResult();
			punts += cartesPunt.intValue();

			Jugador exercit = jugadorExercitMesGran(session, jugador.getPartida());
			if (exercit != null && exercit.getId().equals(jugador.getId())) {
				punts += 2;
			}
		}
		return punts;
	}

	/** Més cavallers jugats, amb un mínim de 3 i sense empat; si no, null. */
	private Jugador jugadorExercitMesGran(Session session, Partida partida) {
		Jugador millor = null;
		int max = 0;
		boolean empat = false;
		for (Jugador j : jugadorsDeLaPartida(session, partida)) {
			int cavallers = j.getCavallersJugats();
			if (cavallers > max) {
				max = cavallers;
				millor = j;
				empat = false;
			} else if (cavallers == max && cavallers > 0) {
				empat = true;
			}
		}
		return (max >= 3 && !empat) ? millor : null;
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

	/** Suma recursos a l'inventari (Hibernate desa el canvi en fer commit). */
	private void afegirRecurs(Session session, Jugador jugador, TipusRecurs tipus, int quantitat) {
		RecursJugador fila = session.createQuery(
				"FROM RecursJugador r WHERE r.jugador = :jugador AND r.tipus = :tipus",
				RecursJugador.class)
			.setParameter("jugador", jugador)
			.setParameter("tipus", tipus)
			.getSingleResult();
		fila.setQuantitat(fila.getQuantitat() + quantitat);
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
	@Override
	public String mostrarPartida() {
		return mostrarPartida(1);
	}

	@Override
	public String mostrarTauler() {
		return mostrarTauler(1);
	}

	// ========================================================================
	// CONSULTES DE JUGADOR (Implementades mitjançant consulta Hibernate)
	// ========================================================================

	@Override
	public String mostrarRecursos(int idJugador) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Jugador j = session.get(Jugador.class, idJugador);
			if (j == null) return acabar(session, "El jugador no existeix.");

			List<RecursJugador> recursos = session.createQuery(
					"FROM RecursJugador r WHERE r.jugador = :jugador", RecursJugador.class)
				.setParameter("jugador", j)
				.getResultList();

			StringBuilder sb = new StringBuilder("Recursos de " + j.getNom() + ":\n");
			for (RecursJugador r : recursos) {
				sb.append(r.getTipus()).append(": ").append(r.getQuantitat()).append("\n");
			}

			session.getTransaction().commit();
			return sb.toString();
		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error en consultar els recursos.";
		}
	}

	@Override
	public String calcularPunts(int idJugador) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();
			Jugador j = session.get(Jugador.class, idJugador);
			if (j == null) return acabar(session, "El jugador no existeix.");

			int punts = calcularPunts(session, j);
			session.getTransaction().commit();
			return "El jugador " + j.getNom() + " té " + punts + " punts.";
		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error en calcular els punts.";
		}
	}
	

}