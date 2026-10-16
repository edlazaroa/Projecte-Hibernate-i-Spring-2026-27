package projecteHibernateSpring;

@Entity
@Table(name= "Partida")
public class Partida {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Integer id;
		
}
