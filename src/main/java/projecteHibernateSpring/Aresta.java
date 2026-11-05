package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Aresta")
public class Aresta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer aresta_id;
	
	@Column(name = "codi", nullable=false)
	private Integer codi;

	public Aresta() {

	}

	public Aresta(Integer aresta_id, Integer codi) {
		this.aresta_id = aresta_id;
		this.codi = codi;
	}

	public Integer getAresta_id() {
		return aresta_id;
	}

	public Integer getCodi() {
		return codi;
	}

	public void setCodi(Integer codi) {
		this.codi = codi;
	}
	
	public Carretera getCarretera() {
	    return carretera;
	}
	
	public Vertex getVertexA() {
	    return vertexA;
	}

	public void setVertexA(Vertex vertexA) {
	    this.vertexA = vertexA;
	}

	public Vertex getVertexB() {
	    return vertexB;
	}

	public void setVertexB(Vertex vertexB) {
	    this.vertexB = vertexB;
	}
	
	public Partida getPartida() {
	    return partida;
	}

	public void setPartida(Partida partida) {
	    this.partida = partida;
	}
	
	@ManyToOne
    @JoinColumn(name = "vertex_a_id")	    
	private Vertex vertexA;

    @ManyToOne	   
    @JoinColumn(name = "vertex_b_id")
    private Vertex vertexB;
    
    @OneToOne(mappedBy = "aresta")
    private Carretera carretera;
    
    @ManyToOne
    @JoinColumn(name = "partida_id")
    private Partida partida;
	
}
