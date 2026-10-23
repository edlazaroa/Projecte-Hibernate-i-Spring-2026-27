package projecteHibernateSpring;

import java.io.Serializable;
import java.util.List;

/*
 * Creem interfície genèrica per implementar el patró
 * Com és genèrica funciona amb genèrics tant per les classes que rep com els ID
 * que seran serialitzables
 */
public interface IGenericDao<T, ID extends Serializable> {
	// Com a mínim configurarem el CRUD bàsic:

	/*
	 * INSERT i UPDATE
	 */
	void saveOrUpdate(T entity);

	/*
	 * SELECT per ID
	 */
	T get(ID id);

	/*
	 * DELETE per ID
	 */
	void delete(ID id);

	/*
	 * DELETE per objecte
	 */
	void delete(T entity);

	/*
	 * SELECT ALL
	 */
	List<T> list();
}
