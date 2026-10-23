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
import jakarta.persistence.Table;

@Entity
@Table(name = "HexagonVertex")
public class HexagonVertex {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer hexagon_vertex_id;
	
	@Column(name = "posicio", nullable=false, length = 5)
	private Integer posicio;

	public HexagonVertex() {

	}

	public HexagonVertex(Integer hexagon_vertex_id, Integer posicio) {
		this.hexagon_vertex_id = hexagon_vertex_id;
		this.posicio = posicio;
	}

	public Integer getHexagon_vertex_id() {
		return hexagon_vertex_id;
	}

	public Integer getPosicio() {
		return posicio;
	}

	public void setPosicio(Integer posicio) {
		this.posicio = posicio;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "hexagon_id")
	private Hexagon hexagon;
	
	@OneToMany(mappedBy="Vertex")
	private List<Vertex> vertexs = new ArrayList<>();	
	
	
}
