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
@Table(name="Construccio")
public abstract class Construccio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "construccio_id", nullable = false)
	private Integer construccio_id;

	public Construccio() {

	}

	public Construccio(Integer construccio_id) {
		this.construccio_id = construccio_id;
	}

	public Integer getConstruccio_id() {
		return construccio_id;
	}
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "jugador_id", nullable = false)
	private Jugador jugador;
	
}
