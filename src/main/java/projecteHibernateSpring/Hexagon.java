package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="Hexagon")
public class Hexagon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hexagon_id", nullable = false)
	private Integer hexagon_id;
	@Column(name = "codi", nullable = false)
	private Integer codi;
	@Column(name = "terreny", nullable = false)
	private TipusTerreny terreny;
	@Column(name = "numero", nullable = false)
	private Integer numero;
	@Column(name = "q", nullable = false)
	private Integer q;
	@Column(name = "r", nullable = false)
	private Integer r;
	@Column(name = "hexagon.partida_id")
	private Integer hexagon_partida_id;
	
	
	public Hexagon() {

	}

	

	public Hexagon(Integer hexagon_id, Integer codi, TipusTerreny terreny, Integer numero, Integer q, Integer r,
			Integer hexagon_partida_id, Partida partida) {
		this.hexagon_id = hexagon_id;
		this.codi = codi;
		this.terreny = terreny;
		this.numero = numero;
		this.q = q;
		this.r = r;
		this.hexagon_partida_id = hexagon_partida_id;
		this.partida = partida;
	}
	

	public Integer getHexagon_id() {
		return hexagon_id;
	}


	public Integer getCodi() {
		return codi;
	}



	public void setCodi(Integer codi) {
		this.codi = codi;
	}



	public TipusTerreny getTerreny() {
		return terreny;
	}



	public void setTerreny(TipusTerreny terreny) {
		this.terreny = terreny;
	}



	public Integer getNumero() {
		return numero;
	}



	public void setNumero(Integer numero) {
		this.numero = numero;
	}



	public Integer getQ() {
		return q;
	}



	public void setQ(Integer q) {
		this.q = q;
	}



	public Integer getR() {
		return r;
	}



	public void setR(Integer r) {
		this.r = r;
	}



	public Integer getHexagon_partida_id() {
		return hexagon_partida_id;
	}



	public void setHexagon_partida_id(Integer hexagon_partida_id) {
		this.hexagon_partida_id = hexagon_partida_id;
	}



	public Partida getPartida() {
		return partida;
	}



	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
}
