package suncertify.network.rmi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import suncertify.URLyBird;
import suncertify.db.DatabaseAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.network.Server;

public class RMIDatabaseAccessConcurrencyTest {

	private static final int PORT = 11111;
	private Server server;
	private List<Integer> recNos;
	private int count = 0;
	private int created;
	private boolean testFails;

	@Before
	public void before() throws Exception {
		new URLyBird();
		// Start server thread
		File orig = new File(RMIDatabaseAccessConcurrencyTest.class.getResource("/").getPath(), "db-1x1.db");
		File file = new File(RMIDatabaseAccessConcurrencyTest.class.getResource("/").getPath(), "db-1x1.db.test");
		FileUtils.copyFile(orig, file);
		server = new RMIServer(PORT, file.getAbsolutePath());
		server.start();
		assertTrue(server.isRunning());
		recNos = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
				19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30));
		testFails = false;
	}

	@After
	public void after() throws Exception {
		server.stop();
		assertFalse(testFails);
	}

	@Test
	public void testRun() throws Exception {
		List<Thread> list = new ArrayList<Thread>();
		for (int i = 0; i < 100; i++) {
			ClientThread client = new ClientThread();
			client.start();
			list.add(client);
		}
		while (!list.isEmpty()) {
			for (Iterator<Thread> it = list.iterator(); it.hasNext();) {
				if (!it.next().isAlive()) {
					it.remove();
				}
			}
		}
	}

	@Test
	public void testRunMultipleCreates() throws Exception {
		List<Thread> list = new ArrayList<Thread>();
		for (int i = 0; i < 500; i++) {
			CreateThread client = new CreateThread();
			client.start();
			list.add(client);
		}
		while (!list.isEmpty()) {
			for (Iterator<Thread> it = list.iterator(); it.hasNext();) {
				if (!it.next().isAlive()) {
					it.remove();
				}
			}
		}
	}

	private class ClientThread extends Thread {
		@Override
		public void run() {
			try {
				DatabaseAccess client = RMIRegistrator.getClient("localhost", PORT);
				for (int i = 0; i < 20; i++) {
					double job = new Random().nextDouble();
					synchronized (ClientThread.class) {
						Collections.shuffle(recNos);
						if (job < 0.30) {
							// read
							int read = recNos.iterator().next();
							client.read(read);
						} else if (job >= 0.30 && job < 0.60) {
							// find
							client.find(new String[] { "", "", "", "", "", "", "" });
						} else if (job >= 0.60 && job < 0.80) {
							// create
							int data = client.create(new String[] { getName() + "-" + i, "", "", "", "", "", "" });
							recNos.add(data);
						} else if (job >= 0.80 && job < 0.90) {
							// update
							int update = recNos.iterator().next();
							client.update(update, new String[] { "", "", "", "", "", "", "" });
						} else {
							// delete
							int delete = recNos.iterator().next();
							recNos.remove((Integer) delete);
							client.delete(delete);
						}
					}
				}
				client.closeConnections();
			} catch (IOException e) {
				e.printStackTrace();
				testFails = true;
			} catch (RecordNotFoundException e) {
				e.printStackTrace();
				testFails = true;
			} catch (DuplicateKeyException e) {
				e.printStackTrace();
				testFails = true;
			}
		}
	}

	private class CreateThread extends Thread {
		@Override
		public void run() {
			try {
				count++;
				DatabaseAccess client = RMIRegistrator.getClient("localhost", PORT);
				System.err.println("create");
				try {
					created = client.create(new String[] { "test", "", "", "", "", "", "" });
					recNos.add(created);
					if (count != 1 && count != 100) {
						// System.err.println("DuplicateKeyException expected");
						// testFails = true;
					}
					if (count == 100) {
						client.delete(created);
					}
				} catch (DuplicateKeyException e) {
					// yippie
				} catch (RecordNotFoundException e) {
					e.printStackTrace();
					testFails = true;
				}
				client.closeConnections();
			} catch (IOException e) {
				e.printStackTrace();
				testFails = true;
			}
		}
	}

}
