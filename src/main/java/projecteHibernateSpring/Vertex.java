package projecteHibernateSpring;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Vertex")
public class Vertex {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer vertex_id;
	
	@Column(name = "codi", nullable=false)
	private Integer codi;

	public Vertex() {

	}

	public Vertex(Integer vertex_id, Integer codi) {
		this.vertex_id = vertex_id;
		this.codi = codi;
	}

	public Integer getVertex_id() {
		return vertex_id;
	}


	public Integer getCodi() {
		return codi;
	}

	public void setCodi(Integer codi) {
		this.codi = codi;
	}
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "hexagon_id", nullable = false)
	private Hexagon hexagon;
	
	@OneToMany(mappedBy = "vertexA")
	private List<Aresta> arestesA = new ArrayList<>();

	@OneToMany(mappedBy = "vertexB")
	private List<Aresta> arestesB = new ArrayList<>();
	
	@OneToOne(mappedBy = "Vertex")
	private Assentament assentament;
	
	
	
}
