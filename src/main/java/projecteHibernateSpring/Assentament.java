package projecteHibernateSpring;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Assentament")
public abstract class Assentament extends Construccio {
	
	public Assentament() {

	}

	public Assentament(Vertex vertex) {
		this.vertex = vertex;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	
	 @OneToOne
	 @JoinColumn(name = "vertex_id", unique = true)
	 private Vertex vertex;
	
}
