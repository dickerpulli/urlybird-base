package de.tbosch.tools.consumption.dao.generic;

import java.util.List;

public interface EntryDao<K, E> {

	List<E> findAll();

	void create(E entity);

	E read(K key);

	void update(E entity);

	void delete(E entity);

}