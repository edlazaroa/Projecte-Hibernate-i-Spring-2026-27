package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="Jugador")
public class Jugador {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "jugador_id", nullable = false)
	private Integer jugador_id;
	@Column(name = "nom", nullable = false)
	private String nom;
	@Column(name = "ordreTorn", nullable = false)
	private Integer ordreTorn;
	@Column(name = "cavallersJugats", nullable = false)
	private Integer cavallersJugats;
	
	public Jugador() {

	}

	public Jugador(Integer jugador_id, String nom, ColorJugador color, Integer ordreTorn, Integer cavallersJugats) {
		this.jugador_id = jugador_id;
		this.nom = nom;
		this.color = color;
		this.ordreTorn = ordreTorn;
		this.cavallersJugats = cavallersJugats;
	}

	public Integer getId() {
		return jugador_id;
	}
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public ColorJugador getColor() {
		return color;
	}

	public void setColor(ColorJugador color) {
		this.color = color;
	}

	public Integer getOrdreTorn() {
		return ordreTorn;
	}

	public void setOrdreTorn(Integer ordreTorn) {
		this.ordreTorn = ordreTorn;
	}

	public Integer getCavallersJugats() {
		return cavallersJugats;
	}

	public void setCavallersJugats(Integer cavallersJugats) {
		this.cavallersJugats = cavallersJugats;
	}
	
	public Partida getPartida() {
	    return partida;
	}
	
	public void setPartida(Partida partida) {
	    this.partida = partida;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "partida_id")
	private Partida partida;
	
	@OneToMany(mappedBy="Partida")
	private List<Partida> actius = new ArrayList<>();
	
	@OneToMany(mappedBy="Partida")
	private List<Partida> guanyadors = new ArrayList<>();
	
	@Enumerated(EnumType.STRING)
	@Column(name = "color", nullable = false)
	private ColorJugador color;	
	
	@OneToMany(mappedBy="CartaDesenvolupament")
	private List<CartaDesenvolupament> mans = new ArrayList<>();
	
	@OneToMany(mappedBy="Construccio")
	private List<Construccio> construccions = new ArrayList<>();
	
	@OneToMany(mappedBy="RecursJugador")
	private List<RecursJugador> recursos = new ArrayList<>();
}
