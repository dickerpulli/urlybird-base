package suncertify.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

	private File backup;
	private File file;

	private Database database;

	@Before
	public void before() throws Exception {
		file = new File(this.getClass().getResource("/db-1x1.db").getFile());
		backup = new File(this.getClass().getResource("/").getFile(), "db-1x1.db.bak");
		FileUtils.copyFile(file, backup);
		database = new Database(file);
	}

	@After
	public void after() throws IOException {
		FileUtils.copyFile(backup, file);
	}

	@Test
	public void testGetRecordBytesFields() throws Exception {
		byte[] record = "Name    other data      ".getBytes("US-ASCII");
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(0, 8);
		map.put(1, 16);

		Method method = Database.class.getDeclaredMethod("getRecordFields", byte[].class, Map.class);
		method.setAccessible(true);
		String[] fields = (String[]) method.invoke(database, record, map);

		assertEquals(2, fields.length);
		assertEquals("Name", fields[0]);
		assertEquals("other data", fields[1]);
	}

	@Test
	public void testGetRecordBytes() throws Exception {
		String[] recordFields = { "Name", "other data" };
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(0, 8);
		map.put(1, 16);

		Method method = Database.class.getDeclaredMethod("getRecordBytes", String[].class, Map.class);
		method.setAccessible(true);
		byte[] record = (byte[]) method.invoke(database, recordFields, map);

		assertArrayEquals("Name\0\0\0\0other data\0\0\0\0\0\0".getBytes("US-ASCII"), Arrays.copyOfRange(record, 0, 24));
	}

	@Test
	public void testIsLike() throws Exception {
		String[] text = { "Name", "other data", "something" };

		Method method = Database.class.getDeclaredMethod("isLike", String[].class, String[].class);
		method.setAccessible(true);

		String[] criteria = { "Name", "other data", "something" };
		boolean like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);

		criteria = new String[] { null, null, null };
		like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);

		criteria = new String[] { "Name", null, null };
		like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);

		criteria = new String[] { "Na", null, null };
		like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);

		criteria = new String[] { "me", null, null };
		like = (Boolean) method.invoke(database, text, criteria);
		assertFalse(like);

		criteria = new String[] { "Name", "xxxxxxx", null };
		like = (Boolean) method.invoke(database, text, criteria);
		assertFalse(like);

		criteria = new String[] { "Name", "", "s" };
		like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);

		criteria = new String[] { "", "", "" };
		like = (Boolean) method.invoke(database, text, criteria);
		assertTrue(like);
	}

}
