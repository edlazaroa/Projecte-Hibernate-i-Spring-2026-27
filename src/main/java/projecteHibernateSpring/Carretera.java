package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Carretera")
public class Carretera {
	
	@Column(name = "aresta", nullable = false, unique = true)
	private Aresta aresta;
}
