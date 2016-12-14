package suncertify.network.rmi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import suncertify.db.DatabaseAccess;
import suncertify.db.RecordNotFoundException;
import suncertify.network.Server;

public class RMIDatabaseAccessTest {

	private static final int PORT = 11111;
	private Server server;
	private DatabaseAccess client;

	@Before
	public void before() throws Exception {
		// Start server thread
		File orig = new File(RMIDatabaseAccessConcurrencyTest.class.getResource("/").getPath(), "db-1x1.db");
		File file = new File(RMIDatabaseAccessConcurrencyTest.class.getResource("/").getPath(), "db-1x1.db.test");
		FileUtils.copyFile(orig, file);
		server = new RMIServer(PORT, file.getAbsolutePath());
		server.start();
		assertTrue(server.isRunning());
		client = RMIRegistrator.getClient("localhost", PORT);
	}

	@After
	public void after() throws Exception {
		server.stop();
	}

	@Test
	public void testRead() throws Exception {
		String[] data = client.read(0);
		client.closeConnections();
		assertEquals("[Palace, Smallville, 2, Y, $150.00, 2005/07/27, ]", Arrays.toString(data));
	}

	@Test
	public void testReadWithRecordNotFoundException() throws Exception {
		try {
			client.read(-1);
			fail("RecordNotFoundException expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
		client.closeConnections();
	}

	@Test
	public void testCreate() throws Exception {
		String[] data = { "Palace2", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		int recNo = client.create(data);
		client.closeConnections();

		DatabaseAccess client2 = RMIRegistrator.getClient("localhost", PORT);
		String[] read = client2.read(recNo);
		client2.closeConnections();
		assertArrayEquals(read, data);
	}

	// @Test
	// public void testCreateWithDuplicateKeyException() throws Exception {
	// try {
	// String[] data = { "Palace", "Smallville", "2", "Y", "$150.00",
	// "2005/07/27", "" };
	// client.create(data);
	// fail("DuplicateKeyException expected");
	// } catch (DuplicateKeyException e) {
	// // yippie
	// }
	// client.closeConnections();
	// }

	@Test
	public void testFind() throws Exception {
		String[] data = { "Palace", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		int[] recNos = client.find(data);
		client.closeConnections();
		assertEquals(0, recNos[0]);
	}

	@Test
	public void testDelete() throws Exception {
		client.delete(0);
		client.closeConnections();

		DatabaseAccess client2 = RMIRegistrator.getClient("localhost", PORT);
		try {
			client2.read(0);
			fail("RecordNotFoundException expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
		client2.closeConnections();
	}

	@Test
	public void testDeleteWithRecordNotFoundException() throws Exception {
		try {
			client.delete(-9999);
			fail("RecordNotFoundException expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
		client.closeConnections();
	}

	@Test
	public void testUpdate() throws Exception {
		String[] data = { "Palace3", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		client.update(0, data);
		client.closeConnections();

		DatabaseAccess client2 = RMIRegistrator.getClient("localhost", PORT);
		String[] read = client2.read(0);
		client2.closeConnections();
		assertArrayEquals(read, data);
	}

	@Test
	public void testUpdateWithRecordNotFoundException() throws Exception {
		String[] data = { "Palace3", "Smallville", "2", "Y", "$150.00", "2005/07/27", "" };
		try {
			client.update(-9999, data);
			fail("RecordNotFoundException expected");
		} catch (RecordNotFoundException e) {
			// yippie
		}
		client.closeConnections();
	}

}
