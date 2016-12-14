package de.tbosch.tools.consumption.dao;

import java.util.List;

import org.joda.time.LocalDate;

import de.tbosch.tools.consumption.dao.generic.GenericJpaDao;
import de.tbosch.tools.consumption.model.Entry;

public interface EntryDao extends GenericJpaDao<Long, Entry> {

	List<Entry> findBetween(LocalDate from, LocalDate to);

}