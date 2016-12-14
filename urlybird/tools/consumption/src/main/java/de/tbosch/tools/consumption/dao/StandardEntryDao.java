package de.tbosch.tools.consumption.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import de.tbosch.tools.consumption.dao.generic.StandardGenericJpaDao;
import de.tbosch.tools.consumption.model.Entry;

@Repository
public class StandardEntryDao extends StandardGenericJpaDao<Long, Entry> implements EntryDao {

	public StandardEntryDao() {
		super(Entry.class);
	}

	/**
	 * @see de.tbosch.tools.consumption.dao.EntryDao#findBetween(org.joda.time.LocalDate, org.joda.time.LocalDate)
	 */
	@Override
	public List<Entry> findBetween(LocalDate from, LocalDate to) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Entry> createQuery = builder.createQuery(Entry.class);
		Root<Entry> root = createQuery.from(Entry.class);
		Path<Date> path = root.get("date");
		createQuery.select(root).where(builder.between(path, from.toDate(), to.toDate()));
		return em.createQuery(createQuery).getResultList();
	}
}