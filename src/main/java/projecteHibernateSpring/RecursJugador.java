package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="RecursJugador")
public class RecursJugador {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "recurs_jugador_id", nullable = false)
	private Integer recurs_jugador_id;
	@Column(name = "id", nullable = false)
	private Integer quantitat;
	@Column(name = "jugador_id", nullable = false, unique = true)
	private Jugador jugador;
	public RecursJugador() {

	}
	public RecursJugador(Integer recurs_jugador_id, TipusRecurs tipus, Integer quantitat, Jugador jugador) {
		this.recurs_jugador_id = recurs_jugador_id;
		this.tipus = tipus;
		this.quantitat = quantitat;
		this.jugador = jugador;
	}
	public Integer getRecurs_jugador_id() {
		return recurs_jugador_id;
	}
	public TipusRecurs getTipus() {
		return tipus;
	}
	public void setTipus(TipusRecurs tipus) {
		this.tipus = tipus;
	}
	public Integer getQuantitat() {
		return quantitat;
	}
	public void setQuantitat(Integer quantitat) {
		this.quantitat = quantitat;
	}
	public Jugador getJugador() {
		return jugador;
	}
	public void setJugador(Jugador jugador) {
		this.jugador = jugador;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false)
	private TipusRecurs tipus;	
	
	
	
}
