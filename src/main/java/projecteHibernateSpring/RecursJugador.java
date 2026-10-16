package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="RecursJugador")
public class RecursJugador {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "id", nullable = false)
	private Integer id;
	@Column(name = "tipus", nullable = false, unique = true)
	private TipusRecurs tipus;
	@Column(name = "id", nullable = false)
	private Integer quantitat;
	@Column(name = "id", nullable = false, unique = true)
	private Jugador jugador;
}
