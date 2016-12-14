package suncertify.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataTest {

	private File backup;
	private File file;

	private Data data;

	@BeforeClass
	public static void beforeAll() {
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.ALL);
		logger.getHandlers()[0].setLevel(Level.ALL);
	}

	@Before
	public void before() throws IOException {
		file = new File(this.getClass().getResource("/db-1x1.db").getFile());
		backup = new File(this.getClass().getResource("/").getFile(), "db-1x1.db.bak");
		FileUtils.copyFile(file, backup);
		data = new Data(file);
	}

	@After
	public void after() throws IOException {
		FileUtils.copyFile(backup, file);
	}

	@Test
	public void testRead() throws RecordNotFoundException {
		String[] record = data.read(0);
		assertEquals("Palace", record[0]);
		assertEquals("Smallville", record[1]);
		assertEquals("2", record[2]);
		assertEquals("Y", record[3]);
		assertEquals("$150.00", record[4]);
		assertEquals("2005/07/27", record[5]);
		assertEquals("", record[6]);

		record = data.read(1);
		assertEquals("Castle", record[0]);
		assertEquals("Smallville", record[1]);
		assertEquals("6", record[2]);
		assertEquals("Y", record[3]);
		assertEquals("$220.00", record[4]);
		assertEquals("2005/11/19", record[5]);
		assertEquals("", record[6]);

		record = data.read(30);
		assertEquals("Grandview", record[0]);
		assertEquals("Lendmarch", record[1]);
		assertEquals("4", record[2]);
		assertEquals("N", record[3]);
		assertEquals("$170.00", record[4]);
		assertEquals("2003/11/12", record[5]);
		assertEquals("", record[6]);
	}

	@Test
	public void testReadNotFound() throws RecordNotFoundException {
		try {
			data.read(999999);
			fail("Exception was expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
	}

	@Test
	public void testUpdate() throws Exception {
		// The number to update
		int recNo = 0;

		// First read the precondition
		String[] read = data.read(recNo);
		assertEquals("Palace", read[0]);
		assertEquals("Smallville", read[1]);
		assertEquals("2", read[2]);
		assertEquals("Y", read[3]);
		assertEquals("$150.00", read[4]);
		assertEquals("2005/07/27", read[5]);
		assertEquals("", read[6]);

		// Update the record
		String[] newData = { "Palace", "Bigville", "2", "Y", "$250.00", "2005/07/27", "" };
		long lockCookie = data.lock(recNo);
		data.update(recNo, newData, lockCookie);

		// Read the postcondition
		read = data.read(recNo);
		assertEquals("Palace", read[0]);
		assertEquals("Bigville", read[1]);
		assertEquals("2", read[2]);
		assertEquals("Y", read[3]);
		assertEquals("$250.00", read[4]);
		assertEquals("2005/07/27", read[5]);
		assertEquals("", read[6]);
	}

	@Test
	public void testUpdateFails() throws Exception {
		long lockCookie = 74637856347856873L;
		try {
			data.update(0, new String[] { "" }, lockCookie);
			fail("Exception expected here");
		} catch (SecurityException e) {
			// yippie
		}
	}

	@Test
	public void testUpdateFails2() throws Exception {
		int recNo = 0;

		// First lock the record by another client
		data.lock(recNo);

		long lockCookie = 74637856347856873L;
		try {
			data.update(recNo, new String[] { "" }, lockCookie);
			fail("Exception expected here");
		} catch (SecurityException e) {
			// yippie
		}
	}

	@Test
	public void testCreate() throws Exception {
		// Cretae the record
		String[] newData = { "New Palace", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		int recNo = data.create(newData);
		assertEquals(31, recNo);

		// Read the postcondition
		String[] record = data.read(recNo);
		assertEquals("New Palace", record[0]);
		assertEquals("Smallville", record[1]);
		assertEquals("2", record[2]);
		assertEquals("Y", record[3]);
		assertEquals("$150.00", record[4]);
		assertEquals("2005/07/27", record[5]);
		assertEquals("", record[6]);
	}

	@Test
	public void testCreateUndelete() throws Exception {
		// First delete the record
		int recNo = 30;
		long lockCookie = data.lock(recNo);
		data.delete(recNo, lockCookie);

		// Create an new record on the deleted position
		String[] newData = { "Grandview", "Lendmarch", "4", "N", "$190.00", "2003/11/12", "" };

		// Create fully new record
		int recNoNew = data.create(newData);
		assertEquals(recNo, recNoNew);

		// Read the postcondition
		String[] record = data.read(recNoNew);
		assertEquals("Grandview", record[0]);
		assertEquals("Lendmarch", record[1]);
		assertEquals("4", record[2]);
		assertEquals("N", record[3]);
		assertEquals("$190.00", record[4]);
		assertEquals("2003/11/12", record[5]);
		assertEquals("", record[6]);
	}

	@Test
	public void testCreateFailAlreadyExists() throws Exception {
		// Update the record
		String[] newData = { "Palace", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		try {
			data.create(newData);
		} catch (DuplicateKeyException e) {
			fail("Exception should never been thrown here");
		}
	}

	@Test
	public void testDelete() throws Exception {
		// First delete the record
		int recNo = 30;
		long lockCookie = data.lock(recNo);
		data.delete(recNo, lockCookie);

		// Try to read deleted record
		try {
			data.read(recNo);
			fail("Exception expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
	}

	@Test
	public void testDeleteFailAlreadyDeleted() throws Exception {
		// First delete the record
		int recNo = 30;
		long lockCookie = data.lock(recNo);
		data.delete(recNo, lockCookie);

		// Second try to delete
		try {
			data.delete(recNo, lockCookie);
			fail("Exception expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
	}

	@Test
	public void testDeleteFailNotOwnerOfLock() throws Exception {
		// Delete with wrong lock cookie
		int recNo = 30;
		data.lock(recNo);
		long lockCookie = 678623746781L;
		try {
			data.delete(recNo, lockCookie);
			fail("Exception expected");
		} catch (SecurityException e) {
			// yippie
		}
	}

	@Test
	public void testDeleteFailWrongLock() throws Exception {
		// Delete with wrong lock cookie
		int recNo = 30;
		long lockCookie = 1237264752347L;
		try {
			data.delete(recNo, lockCookie);
			fail("Exception expected");
		} catch (SecurityException e) {
			// yippie
		}
	}

	@Test
	public void testFind() {
		// Find all records
		int[] result = data.find(new String[] { null, null, null, null, null, null, null });
		assertEquals(31, result.length);

		// Find beginning with ...
		result = data.find(new String[] { "Grandview", null, null, null, null, null, null });
		assertEquals(5, result.length);

		// Find beginning with ...
		result = data.find(new String[] { "Grand", null, null, null, null, null, null });
		assertEquals(5, result.length);

		// Find beginning with ...
		result = data.find(new String[] { "Grand", "Whoville", null, null, null, null, null });
		assertEquals(1, result.length);

		// Find beginning with ...
		result = data.find(new String[] { "Grand", "Whoville", "6", "Y", null, null, null });
		assertEquals(1, result.length);
	}

}
