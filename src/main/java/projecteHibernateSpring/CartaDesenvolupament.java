package projecteHibernateSpring;

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
import jakarta.persistence.Table;

@Entity
@Table(name="CartaDesenvolupament")
public class CartaDesenvolupament {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "carta_desenvolupament_id", nullable = false)
	private Integer carta_desenvolupament_id;
	@Column(name = "usada", nullable = false)
	private boolean usada;
	@Column(name = "rondaCompra", nullable = false)
	private Integer rondaCompra;
	
	public CartaDesenvolupament() {
	
	}

	public CartaDesenvolupament(Integer carta_desenvolupament_id, TipusCarta tipus, boolean usada,
			Integer rondaCompra) {
		this.carta_desenvolupament_id = carta_desenvolupament_id;
		this.tipus = tipus;
		this.usada = usada;
		this.rondaCompra = rondaCompra;
	}

	public Integer getId() {
		return carta_desenvolupament_id;
	}


	public TipusCarta getTipus() {
		return tipus;
	}

	public void setTipus(TipusCarta tipus) {
		this.tipus = tipus;
	}

	public boolean isUsada() {
		return usada;
	}

	public void setUsada(boolean usada) {
		this.usada = usada;
	}

	public Integer getRondaCompra() {
		return rondaCompra;
	}

	public void setRondaCompra(Integer rondaCompra) {
		this.rondaCompra = rondaCompra;
	}
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false)
	private TipusCarta tipus;	
}
