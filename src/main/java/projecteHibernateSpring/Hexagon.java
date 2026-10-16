package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Hexagon")
public class Hexagon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;
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
}
