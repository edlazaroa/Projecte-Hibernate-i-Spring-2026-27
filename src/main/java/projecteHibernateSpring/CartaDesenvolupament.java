package projecteHibernateSpring;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="CartaDesenvolupament")
public class CartaDesenvolupament {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "id", nullable = false)
	private Integer id;
	@Column(name = "tipus", nullable = false)
	private TipusCarta tipus;
	@Column(name = "usada", nullable = false)
	private boolean usada;
	@Column(name = "rondaCompra", nullable = false)
	private Integer rondaCompra;
}
