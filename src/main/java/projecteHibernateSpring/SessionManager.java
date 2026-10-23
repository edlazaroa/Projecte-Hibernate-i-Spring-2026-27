package projecteHibernateSpring;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/*
 * Singleton per crear la SessionFactory.
 *
 * La SessionFactory és cara de crear (llegeix la configuració, valida les entitats,
 * crea les taules...). Se'n crea UNA per a tota l'aplicació i tots els DAO la comparteixen.
 */
public class SessionManager {

	private static SessionFactory sessionFactory;

	// Constructor privat: ningú pot fer new SessionManager()
	private SessionManager() {
	}

	public static synchronized SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				sessionFactory = new Configuration().configure("daoHibernate.cfg.xml").buildSessionFactory();
			} catch (HibernateException e) {
				System.err.println("Error al crear la SessionFactory: " + e.getMessage());
				throw new ExceptionInInitializerError(e);
			}
		}
		return sessionFactory;
	}

	// Cridar-lo al final del programa: allibera les connexions
	public static synchronized void tancar() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}
}
