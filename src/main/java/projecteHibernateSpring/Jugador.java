package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Jugador")
public class Jugador {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "id", nullable = false)
	private Integer id;
	@Column(name = "nom", nullable = false)
	private String nom;
	@Column(name = "color", nullable = false)
	private ColorJugador color;
	@Column(name = "ordreTorn", nullable = false)
	private Integer ordreTorn;
	@Column(name = "cavallersJugats", nullable = false)
	private Integer cavallersJugats;
}
