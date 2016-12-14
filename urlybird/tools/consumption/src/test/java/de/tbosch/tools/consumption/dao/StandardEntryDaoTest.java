package de.tbosch.tools.consumption.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.tbosch.tools.consumption.dao.common.DaoTest;
import de.tbosch.tools.consumption.model.Entry;

public class StandardEntryDaoTest extends DaoTest {

	@Autowired
	private EntryDao dao;

	@PersistenceContext
	private EntityManager em;

	@Before
	public void before() {
		em.createNativeQuery("delete from " + Entry.class.getSimpleName()).executeUpdate();
		dao.create(new Entry(new LocalDate(2012, 10, 13), 1000));
		dao.create(new Entry(new LocalDate(2012, 11, 13), 1100));
		dao.create(new Entry(new LocalDate(2012, 12, 13), 1200));
		dao.create(new Entry(new LocalDate(2013, 1, 13), 2100));
		dao.create(new Entry(new LocalDate(2014, 2, 13), 3200));
	}

	@Test
	public void shouldFindAll() {
		List<Entry> all = dao.findAll();
		assertEquals(5, all.size());
	}

	@Test
	public void testFindBetween() throws Exception {
		List<Entry> list = dao.findBetween(new LocalDate(2012, 1, 1), new LocalDate(2012, 12, 31));
		assertEquals(3, list.size());
	}

}