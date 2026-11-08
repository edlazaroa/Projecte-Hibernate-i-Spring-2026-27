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
	        Partida partida = new Partida();
	        partida.setEstat(EstatPartida.EN_CURS);
	        partida.setDataInici(new Date(System.currentTimeMillis()));
	        partida.setRonda(1);
	        partida.setPuntsPerGuanyar(10);
	        partida.setDausTirats(false);
	        partida.setLladrePendent(false);
	        partida.setUltimaTirada(0);
	        session.persist(partida);
	        List<String> noms = new ArrayList<>(nomsJugadors);
	        Collections.shuffle(noms);
	        Jugador primer = null;
	        List<Jugador> jugadorsCreats = new ArrayList<>();

	        for (int i = 0; i < noms.size(); i++) {
	            Jugador j = new Jugador();
	            j.setNom(noms.get(i));
	            j.setOrdreTorn(i + 1);
	            j.setColor(ColorJugador.values()[i]);
	            j.setCavallersJugats(0);
	            j.setPartida(partida);
	            session.persist(j);
	            jugadorsCreats.add(j);

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
	        
	        partida.setActiu(primer);

	        List<Hexagon> hexagons = carregarTopologia(session, partida);
	        assignarTerrenysINumeros(hexagons, partida);
	        List<Vertex> totsVertexs = session.createQuery(
	                "FROM Vertex v WHERE v.partida = :partida ORDER BY v.codi", Vertex.class)
	            .setParameter("partida", partida)
	            .getResultList();

	        List<Aresta> totesArestes = session.createQuery(
	                "FROM Aresta a WHERE a.partida = :partida ORDER BY a.codi", Aresta.class)
	            .setParameter("partida", partida)
	            .getResultList();

	        int[] vertexIndices = {0, 9, 19, 29}; 
	        for (int i = 0; i < jugadorsCreats.size(); i++) {
	            Jugador jug = jugadorsCreats.get(i);
	            Vertex v = totsVertexs.get(vertexIndices[i]);

	            Poblat poblat = new Poblat();
	            poblat.setJugador(jug);
	            poblat.setVertex(v);
	            session.persist(poblat);

	            for (Aresta ar : totesArestes) {
	                if (ar.getVertexA().equals(v) || ar.getVertexB().equals(v)) {
	                    Carretera carretera = new Carretera();
	                    carretera.setJugador(jug);
	                    carretera.setAresta(ar);
	                    session.persist(carretera);
	                    break;
	                }
	            }
	        }

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
	            h.setNumero(0);
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
	private List<Hexagon> carregarTopologia(Session session, Partida partida) {
	    List<Hexagon> hexagons = new ArrayList<>();
	    Map<Integer, Hexagon> mapHexagons = new HashMap<>();
	    Map<Integer, Vertex> mapVertices = new HashMap<>();

	    try {
	    	try (BufferedReader br = obrirCSV("dades/hexagons.csv")) {
	    	    String line;
	    	    while ((line = br.readLine()) != null) {
	    	        String trimmed = line.trim();
	    	        if (trimmed.isEmpty() || trimmed.startsWith("codi") || trimmed.startsWith("id_hexagon") || trimmed.startsWith("hexagon_id")) {
	    	            continue;
	    	        }
	    	        
	    	        String[] parts = trimmed.split(",");
	    	        int codi = Integer.parseInt(parts[0].trim());
	    	        int q = Integer.parseInt(parts[1].trim());
	    	        int r = Integer.parseInt(parts[2].trim());

	    	        Hexagon h = new Hexagon();
	    	        h.setCodi(codi);
	    	        h.setQ(q);
	    	        h.setR(r);
	    	        h.setPartida(partida);
	    	        h.setTerreny(TipusTerreny.BOSC); 
	    	        h.setNumero(0);

	    	        session.persist(h);
	    	        hexagons.add(h);
	    	        mapHexagons.put(codi, h);
	    	    }
	    	}

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

	private BufferedReader obrirCSV(String ruta) throws IOException {
	    InputStream is = getClass().getClassLoader().getResourceAsStream(ruta);
	    if (is == null) {
	        throw new FileNotFoundException("No s'ha trobat el fitxer en el classpath: " + ruta);
	    }
	    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
			Partida partida = session.get(Partida.class, idPartida);
			String error = validarTirada(partida);
			if (error != null) {
				return acabar(session, error);
			}
			int tirada = tirarDosDaus();
			partida.setUltimaTirada(tirada);
			partida.setDausTirats(true);
			List<Jugador> jugadors = jugadorsDeLaPartida(session, partida);
			Map<Integer, Map<TipusRecurs, Integer>> rebuts = new LinkedHashMap<>();
			Map<Integer, Integer> descartats = new LinkedHashMap<>();
			for (Jugador j : jugadors) {
				rebuts.put(j.getId(), new LinkedHashMap<>());
			}
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
			if (recurs == null) continue;

			for (Assentament a : assentamentsDelHexagon(session, h)) {
				int quantitat = (Hibernate.unproxy(a) instanceof Ciutat) ? 2 : 1;
				Jugador propietari = a.getJugador();
				afegirRecurs(session, propietari, recurs, quantitat);
				rebuts.get(propietari.getId()).merge(recurs, quantitat, Integer::sum);
			}
		}
	}

	private void aplicarDescart(Session session, List<Jugador> jugadors, Map<Integer, Integer> descartats) {
		for (Jugador j : jugadors) {
			List<RecursJugador> files = session.createQuery(
					"FROM RecursJugador r WHERE r.jugador = :jugador", RecursJugador.class)
				.setParameter("jugador", j)
				.getResultList();

			int total = 0;
			List<RecursJugador> bossa = new ArrayList<>();
			for (RecursJugador fila : files) {
				total += fila.getQuantitat();
				for (int i = 0; i < fila.getQuantitat(); i++) {
					bossa.add(fila);
				}
			}
			if (total > 7) {
				int adescartar = total / 2;
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
	private List<Jugador> jugadorsDeLaPartida(Session session, Partida partida) {
		return session.createQuery(
				"FROM Jugador j WHERE j.partida = :partida ORDER BY j.ordreTorn", Jugador.class)
			.setParameter("partida", partida)
			.getResultList();
	}

	private List<Assentament> assentamentsDelHexagon(Session session, Hexagon hexagon) {
		return session.createQuery(
				"SELECT a FROM Assentament a WHERE a.vertex IN "
				+ "(SELECT hv.vertex FROM HexagonVertex hv WHERE hv.hexagon = :hexagon) "
				+ "ORDER BY a.vertex.codi", Assentament.class)
			.setParameter("hexagon", hexagon)
			.getResultList();
	}

	private long comptar(Session session, String entitat, Jugador jugador) {
		return session.createQuery(
				"SELECT COUNT(c) FROM " + entitat + " c WHERE c.jugador = :jugador", Long.class)
			.setParameter("jugador", jugador)
			.getSingleResult();
	}

	private int calcularPunts(Session session, Jugador jugador) {
		int punts = (int) (comptar(session, "Poblat", jugador) + 2 * comptar(session, "Ciutat", jugador));

		if (PART_AVANCADA) {
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
			default:        return null;
		}
	}

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
	public String passarTorn(int idJugador) {
	    Session session = sessionFactory.getCurrentSession();
	    try {
	        session.beginTransaction();

	        Jugador jugador = session.get(Jugador.class, idJugador);
	        if (jugador == null) {
	            return acabar(session, "El jugador no existeix.");
	        }

	        Partida partida = jugador.getPartida();
	        if (partida == null || partida.getEstat() == EstatPartida.FINALITZADA) {
	            return acabar(session, "La partida no està disponible.");
	        }

	        Jugador actiu = partida.getActiu();
	        if (actiu == null || !actiu.getId().equals(jugador.getId())) {
	            return acabar(session, "No és el torn d'aquest jugador.");
	        }

	        partida.setDausTirats(false);

	        List<Jugador> jugadors = jugadorsDeLaPartida(session, partida);
	        int posicioActual = jugadors.indexOf(jugador);
	        int posicioSeguent = (posicioActual + 1) % jugadors.size();
	        Jugador seguentJugador = jugadors.get(posicioSeguent);

	        partida.setActiu(seguentJugador);

	        if (posicioSeguent == 0) {
	            partida.setRonda(partida.getRonda() + 1);
	        }

	        session.getTransaction().commit();
	        return "Turno pasado. Ahora juega " + seguentJugador.getNom() + ".";

	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error en passar el torn.";
	    }
	}
	
	@Override
	public String vertexsDisponibles(int idJugador) {
	    Session session = sessionFactory.getCurrentSession();
	    try {
	        session.beginTransaction();

	        Jugador jugador = session.get(Jugador.class, idJugador);
	        if (jugador == null) {
	            return acabar(session, "El jugador no existeix.");
	        }
	        Partida partida = jugador.getPartida();

	        List<Vertex> disponibles;
	        if (partida.getEstat() == EstatPartida.PREPARACIO) {
	            disponibles = session.createQuery(
	                "SELECT v FROM Vertex v WHERE v.partida = :partida " +
	                "AND NOT EXISTS (SELECT a FROM Assentament a WHERE a.vertex = v) " +
	                "AND NOT EXISTS (" +
	                "  SELECT a2 FROM Assentament a2, Aresta ar " +
	                "  WHERE (ar.vertexA = v AND a2.vertex = ar.vertexB) " +
	                "     OR (ar.vertexB = v AND a2.vertex = ar.vertexA)" +
	                ")", Vertex.class)
	                .setParameter("partida", partida)
	                .getResultList();
	        } else {
	            disponibles = session.createQuery(
	                "SELECT DISTINCT v FROM Vertex v WHERE v.partida = :partida " +
	                "AND NOT EXISTS (SELECT a FROM Assentament a WHERE a.vertex = v) " +
	                "AND NOT EXISTS (" +
	                "  SELECT a2 FROM Assentament a2, Aresta ar " +
	                "  WHERE (ar.vertexA = v AND a2.vertex = ar.vertexB) " +
	                "     OR (ar.vertexB = v AND a2.vertex = ar.vertexA)" +
	                ") " +
	                "AND EXISTS (" +
	                "  SELECT c FROM Carretera c WHERE c.jugador = :jugador " +
	                "  AND (c.aresta.vertexA = v OR c.aresta.vertexB = v)" +
	                ")", Vertex.class)
	                .setParameter("partida", partida)
	                .setParameter("jugador", jugador)
	                .getResultList();
	        }

	        StringBuilder sb = new StringBuilder("Vértices disponibles:\n");
	        if (disponibles.isEmpty()) {
	            sb.append("No hay vértices disponibles.");
	        } else {
	            for (Vertex v : disponibles) {
	                sb.append("Vértice ID: ").append(v.getVertex_id()).append("\n");
	            }
	        }

	        session.getTransaction().commit();
	        return sb.toString();

	    } catch (RuntimeException e) {
	        handleException(session, e);
	        return "Error en consultar els vèrtexs disponibles.";
	    }
	}

	private void crearPilaCartes(Session session, Partida partida) {
		crearCartes(session, partida, TipusCarta.CAVALLER, 14);
		crearCartes(session, partida, TipusCarta.PUNT_VICTORIA, 5);
		crearCartes(session, partida, TipusCarta.MONOPOLI, 2);
	}

	private void crearCartes(Session session, Partida partida, TipusCarta tipus, int quantitat) {
		for (int i = 0; i < quantitat; i++) {
			CartaDesenvolupament carta = new CartaDesenvolupament();
			carta.setTipus(tipus);
			carta.setUsada(false);
			carta.setPartida(partida);
			session.persist(carta);
		}
	}

	private String acabar(Session session, String missatge) {
		session.getTransaction().commit();
		return missatge;
	}
	private RecursJugador buscarRecurs(Session session, int idJugador, TipusRecurs tipus) {
		return session.createQuery(
				"FROM RecursJugador r "
				+ "WHERE r.jugador.jugador_id = :idJugador "
				+ "AND r.tipus = :tipus",
				RecursJugador.class)
			.setParameter("idJugador", idJugador)
			.setParameter("tipus", tipus)
			.uniqueResult();
	}

	private String motiuNoPotActuar(Jugador jugador, boolean exigirDausIsenseLladre) {
		Partida partida = jugador.getPartida();
		if (partida == null) return "El jugador no pertenece a ninguna partida.";
		if (partida.getEstat() == EstatPartida.FINALITZADA) return "La partida ya ha finalizado.";
		if (partida.getEstat() != EstatPartida.EN_CURS) return "La partida no está en curso.";

		Jugador actiu = partida.getActiu();
		if (actiu == null || !actiu.getId().equals(jugador.getId())) return "No es el turno de este jugador.";

		if (exigirDausIsenseLladre) {
			if (!partida.isDausTirats()) return "Primero debes tirar los dados.";
			if (partida.isLladrePendent()) return "Debes mover el ladrón antes de hacer ninguna acción.";
		}
		return null;
	}

	private void comprovarVictoriaDespres(int idPartida) {
		if (idPartida > 0) {
			new PartidaDao().comprovarVictoria(idPartida);
		}
	}

	@Override
	public String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
		Session session = sessionFactory.getCurrentSession();
		try {
			session.beginTransaction();

			Jugador jugador = session.get(Jugador.class, idJugador);
			if (jugador == null) {
				return acabar(session, "El jugador no existe.");
			}

			String error = motiuNoPotActuar(jugador, false);
			if (error != null) {
				return acabar(session, error);
			}

			Partida partida = jugador.getPartida();
			if (!partida.isLladrePendent()) {
				return acabar(session, "No hay ningún movimiento de ladrón pendiente.");
			}

			Hexagon hexagon = session.get(Hexagon.class, idHexagon);
			if (hexagon == null) {
				return acabar(session, "El hexágono no existe.");
			}
			if (!hexagon.getPartida().getId().equals(partida.getId())) {
				return acabar(session, "El hexágono pertenece a otra partida.");
			}
			Hexagon actual = partida.getLladreId();
			if (actual != null && actual.getHexagon_id().equals(hexagon.getHexagon_id())) {
				return acabar(session, "El ladrón ya está en este hexágono: debes moverlo a otro.");
			}

			List<Jugador> candidats = victimesPossibles(session, jugador, hexagon);
			Jugador victima = null;
			if (candidats.isEmpty()) {
				if (idJugadorVictima != null) {
					return acabar(session, "No hay ninguna víctima válida en este hexágono: idJugadorVictima debe ser null.");
				}
			} else {
				if (idJugadorVictima == null) {
					return acabar(session, "Debes elegir una víctima entre los jugadores con construcciones en este hexágono.");
				}
				for (Jugador candidat : candidats) {
					if (candidat.getId().equals(idJugadorVictima)) {
						victima = candidat;
					}
				}
				if (victima == null) {
					return acabar(session, "La víctima no es válida: debe ser otro jugador con una construcción en ese hexágono y con recursos.");
				}
			}
			partida.setLladreId(hexagon);
			partida.setLladrePendent(false);

			String missatge = "Ladrón movido al hexágono " + hexagon.getCodi() + ".";
			if (victima != null) {
				RecursJugador robat = recursAleatori(session, victima);
				RecursJugador meu = buscarRecurs(session, idJugador, robat.getTipus());
				robat.setQuantitat(robat.getQuantitat() - 1);
				meu.setQuantitat(meu.getQuantitat() + 1);
				missatge += " Robas 1 " + robat.getTipus() + " a " + victima.getNom() + ".";
			} else {
				missatge += " No se roba ningún recurso.";
			}
			return acabar(session, missatge);

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al mover el ladrón.";
		}
	}

	private List<Jugador> victimesPossibles(Session session, Jugador actor, Hexagon hexagon) {
		return session.createQuery(
				"SELECT DISTINCT a.jugador FROM Assentament a "
				+ "WHERE a.vertex IN (SELECT hv.vertex FROM HexagonVertex hv WHERE hv.hexagon = :hexagon) "
				+ "AND a.jugador <> :actor "
				+ "AND EXISTS (SELECT r FROM RecursJugador r WHERE r.jugador = a.jugador AND r.quantitat > 0)",
				Jugador.class)
			.setParameter("hexagon", hexagon)
			.setParameter("actor", actor)
			.getResultList();
	}

	private RecursJugador recursAleatori(Session session, Jugador jugador) {
		List<RecursJugador> files = session.createQuery(
				"FROM RecursJugador r WHERE r.jugador = :jugador AND r.quantitat > 0", RecursJugador.class)
			.setParameter("jugador", jugador)
			.getResultList();

		List<RecursJugador> bossa = new ArrayList<>();
		for (RecursJugador fila : files) {
			for (int i = 0; i < fila.getQuantitat(); i++) {
				bossa.add(fila);
			}
		}
		return bossa.get(ThreadLocalRandom.current().nextInt(bossa.size()));
	}

	@Override
	public String comprarCarta(int idJugador) {
		Session session = sessionFactory.getCurrentSession();
		int idPartida = -1;
		TipusCarta tipus = null;
		try {
			session.beginTransaction();

			Jugador jugador = session.get(Jugador.class, idJugador);
			if (jugador == null) {
				return acabar(session, "El jugador no existe.");
			}

			String error = motiuNoPotActuar(jugador, true);
			if (error != null) {
				return acabar(session, error);
			}
			Partida partida = jugador.getPartida();

			RecursJugador blat = buscarRecurs(session, idJugador, TipusRecurs.BLAT);
			RecursJugador llana = buscarRecurs(session, idJugador, TipusRecurs.LLANA);
			RecursJugador mineral = buscarRecurs(session, idJugador, TipusRecurs.MINERAL);
			if (blat == null || blat.getQuantitat() < 1
					|| llana == null || llana.getQuantitat() < 1
					|| mineral == null || mineral.getQuantitat() < 1) {
				return acabar(session, "No tienes recursos para comprar una carta (1 trigo, 1 lana y 1 mineral).");
			}
			
			List<CartaDesenvolupament> pila = session.createQuery(
					"FROM CartaDesenvolupament c WHERE c.partida = :partida AND c.jugador IS NULL",
					CartaDesenvolupament.class)
				.setParameter("partida", partida)
				.getResultList();
			if (pila.isEmpty()) {
				return acabar(session, "La pila de cartas está vacía.");
			}

			CartaDesenvolupament carta = pila.get(ThreadLocalRandom.current().nextInt(pila.size()));
			blat.setQuantitat(blat.getQuantitat() - 1);
			llana.setQuantitat(llana.getQuantitat() - 1);
			mineral.setQuantitat(mineral.getQuantitat() - 1);
			carta.setJugador(jugador);
			carta.setRondaCompra(partida.getRonda());

			idPartida = partida.getId();
			tipus = carta.getTipus();
			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al comprar la carta.";
		}

		comprovarVictoriaDespres(idPartida);
		return "Has comprado una carta de desarrollo: " + tipus + ".";
	}

	@Override
	public String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
		Session session = sessionFactory.getCurrentSession();
		int idPartida = -1;
		String missatge = null;
		try {
			session.beginTransaction();

			Jugador jugador = session.get(Jugador.class, idJugador);
			if (jugador == null) {
				return acabar(session, "El jugador no existe.");
			}

			String error = motiuNoPotActuar(jugador, true);
			if (error != null) {
				return acabar(session, error);
			}
			Partida partida = jugador.getPartida();

			CartaDesenvolupament carta = session.get(CartaDesenvolupament.class, idCarta);
			if (carta == null) {
				return acabar(session, "La carta no existe.");
			}
			if (carta.getJugador() == null || !carta.getJugador().getId().equals(idJugador)) {
				return acabar(session, "Esta carta no está en la mano del jugador.");
			}
			if (carta.isUsada()) {
				return acabar(session, "Esta carta ya se ha jugado.");
			}
			if (carta.getRondaCompra() == null || carta.getRondaCompra() >= partida.getRonda()) {
				return acabar(session, "No puedes jugar una carta en la misma ronda en la que la compraste.");
			}
			if (carta.getTipus() == TipusCarta.PUNT_VICTORIA) {
				return acabar(session, "Las cartas de punto de victoria no se juegan: cuentan automáticamente.");
			}
			if (carta.getTipus() == TipusCarta.MONOPOLI && recurs == null) {
				return acabar(session, "Debes indicar el tipo de recurso para el monopolio.");
			}

			if (carta.getTipus() == TipusCarta.CAVALLER) {
				jugador.setCavallersJugats(jugador.getCavallersJugats() + 1);
				partida.setLladrePendent(true);
				missatge = "Has jugado un caballero. Ahora debes mover el ladrón.";
			} else {
				int total = aplicarMonopoli(session, jugador, recurs);
				missatge = "Monopolio de " + recurs + ": has recibido " + total + " unidades.";
			}
			carta.setUsada(true);

			idPartida = partida.getId();
			session.getTransaction().commit();

		} catch (RuntimeException e) {
			handleException(session, e);
			return "Error al jugar la carta.";
		}

		comprovarVictoriaDespres(idPartida);
		return missatge;
	}

	private int aplicarMonopoli(Session session, Jugador jugador, TipusRecurs recurs) {
		List<Jugador> altres = session.createQuery(
				"FROM Jugador j WHERE j.partida = :partida AND j <> :jugador", Jugador.class)
			.setParameter("partida", jugador.getPartida())
			.setParameter("jugador", jugador)
			.getResultList();

		int total = 0;
		for (Jugador altre : altres) {
			RecursJugador fila = buscarRecurs(session, altre.getId(), recurs);
			if (fila != null && fila.getQuantitat() > 0) {
				total += fila.getQuantitat();
				fila.setQuantitat(0);
			}
		}

		RecursJugador meu = buscarRecurs(session, jugador.getId(), recurs);
		meu.setQuantitat(meu.getQuantitat() + total);
		return total;
	}


	@Override
	public String mostrarPartida() {
		return mostrarPartida(1);
	}

	@Override
	public String mostrarTauler() {
		return mostrarTauler(1);
	}

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