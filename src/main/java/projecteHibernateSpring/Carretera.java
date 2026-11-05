package projecteHibernateSpring;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Carretera")
public class Carretera extends Construccio {

    @OneToOne
    @JoinColumn(name = "aresta_id", unique = true)
    private Aresta aresta;

    public Carretera() {
    }

    public Aresta getAresta() {
        return aresta;
    }

    public void setAresta(Aresta aresta) {
        this.aresta = aresta;
    }
}
