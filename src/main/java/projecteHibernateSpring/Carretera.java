package projecteHibernateSpring;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Carretera")
public class Carretera extends Construccio {
	
	public void setAresta(Aresta aresta) {
		this.aresta = aresta;
	}
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)  
	@JoinColumn(name = "vertex_id", nullable = false, unique = true)
	private Vertex vertex;
	
	@OneToOne
	@JoinColumn(name = "aresta_id", unique = true)
	private Aresta aresta;

	
}
