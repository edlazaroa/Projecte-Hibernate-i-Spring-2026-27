package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Assentament")
public abstract class Assentament {
	
	@Column(name = "vertex", nullable = false, unique = true)
	private Vertex vertex;
}
