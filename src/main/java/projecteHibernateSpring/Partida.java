package projecteHibernateSpring;

import java.sql.Date;
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
@Table(name="Partida")
public class Partida {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "partida_id", nullable = false)
	private Integer partida_id;
	@Column(name = "dataInici", nullable = false)
	private Date dataInici;
	@Column(name = "ronda", nullable = false)
	private Integer ronda;
	@Column(name = "ultimaTirada", nullable = false)
	private Integer ultimaTirada;
	@Column(name = "puntsPerGuanyar", nullable = false)
	private Integer puntsPerGuanyar;
	@Column(name = "dausTirats", nullable = false)
	private boolean dausTirats;
	@Column(name = "lladrePendent", nullable = false)
	private boolean lladrePendent;
	
	public Partida() {
		
	}

	public Partida(Integer partida_id, EstatPartida estat, Date dataInici, Integer ronda, Integer ultimaTirada,
			Integer puntsPerGuanyar, boolean dausTirats, boolean lladrePendent) {
		this.partida_id = partida_id;
		this.estat = estat;
		this.dataInici = dataInici;
		this.ronda = ronda;
		this.ultimaTirada = ultimaTirada;
		this.puntsPerGuanyar = puntsPerGuanyar;
		this.dausTirats = dausTirats;
		this.lladrePendent = lladrePendent;
	}

	public Integer getId() {
		return partida_id;
	}

	public EstatPartida getEstat() {
		return estat;
	}

	public void setEstat(EstatPartida estat) {
		this.estat = estat;
	}

	public Date getDataInici() {
		return dataInici;
	}

	public void setDataInici(Date dataInici) {
		this.dataInici = dataInici;
	}

	public Integer getRonda() {
		return ronda;
	}

	public void setRonda(Integer ronda) {
		this.ronda = ronda;
	}

	public Integer getUltimaTirada() {
		return ultimaTirada;
	}

	public void setUltimaTirada(Integer ultimaTirada) {
		this.ultimaTirada = ultimaTirada;
	}

	public Integer getPuntsPerGuanyar() {
		return puntsPerGuanyar;
	}

	public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {
		this.puntsPerGuanyar = puntsPerGuanyar;
	}

	public boolean isDausTirats() {
		return dausTirats;
	}

	public void setDausTirats(boolean dausTirats) {
		this.dausTirats = dausTirats;
	}

	public boolean isLladrePendent() {
		return lladrePendent;
	}

	public void setLladrePendent(boolean lladrePendent) {
		this.lladrePendent = lladrePendent;
	}
	
	public Jugador getActiu() {
	    return actiu;
	}

	public void setActiu(Jugador actiu) {
	    this.actiu = actiu;
	}
	
	@OneToMany(mappedBy="Hexagon")
	private List<Hexagon> hexagons = new ArrayList<>();
	
	@OneToMany(mappedBy="partida")
	private List<Jugador> jugadors = new ArrayList<>();
	
	@OneToMany(mappedBy="CartaDesenvolupament")
	private List<CartaDesenvolupament> cartes = new ArrayList<>();
	
	@OneToMany(mappedBy="Vertex")
	private List<Vertex> vertexs = new ArrayList<>();
	
	@OneToMany(mappedBy="Aresta")
	private List<Aresta> arestes = new ArrayList<>();
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "giu_id")
	private Jugador actiu;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "guanyador_id")
	private Jugador guanyador;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "lladre_id")
	private Hexagon lladre;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", nullable = false)
	private EstatPartida estat;	
}
