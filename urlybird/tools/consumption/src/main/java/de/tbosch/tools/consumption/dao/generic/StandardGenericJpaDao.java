package de.tbosch.tools.consumption.dao.generic;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public abstract class StandardGenericJpaDao<K, E> implements GenericJpaDao<K, E> {

	@PersistenceContext
	protected EntityManager em;

	private final Class<E> genericType;

	public StandardGenericJpaDao(Class<E> genericType) {
		this.genericType = genericType;
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#findAll()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<E> findAll() {
		Query query = em.createQuery("from " + genericType.getSimpleName());
		return query.getResultList();
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#create(de.tbosch.tools.consumption.model.Entry)
	 */
	@Override
	public void create(E entity) {
		em.persist(entity);
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#read(long)
	 */
	@Override
	public E read(K key) {
		return em.find(genericType, key);
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#update(de.tbosch.tools.consumption.model.Entry)
	 */
	@Override
	public void update(E entity) {
		em.merge(entity);
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#delete(de.tbosch.tools.consumption.model.Entry)
	 */
	@Override
	public void delete(E entity) {
		em.remove(entity);
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.generic.GenericJpaDao#flush()
	 */
	@Override
	public void flush() {
		em.flush();
	}

}