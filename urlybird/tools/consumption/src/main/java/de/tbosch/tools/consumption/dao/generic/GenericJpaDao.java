package de.tbosch.tools.consumption.dao.generic;

public interface GenericJpaDao<K, E> extends EntryDao<K, E> {

	void flush();

}