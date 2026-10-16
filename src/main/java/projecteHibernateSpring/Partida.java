package projecteHibernateSpring;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Partida")
public class Partida {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;
	@Column(name = "estat", nullable = false)
	private EstatPartida estat;
	@Column(name = "dataInici", nullable = false)
	private Date dataInici;
	@Column(name = "ronda", nullable = false)
	private Integer ronda;
	@Column(name = "ultimaTirada", nullable = false)
	private Integer ultimaTirada;
	@Column(name = "puntsPerGuanyar", nullable = false)
	private Integer puntsPerGuanyar;
	@Column(name = "dausTirats", nullable = false)
	private boolean dausTirats;
	@Column(name = "lladrePendent", nullable = false)
	private boolean lladrePendent;	
}
