/**
 * UrlyBird/suncertify.db.DataTest.java
 * @author (c) 2009, Harald R. Haberstroh
 * 13.03.2009
 */
package suncertify.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Logger;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Harald R. Haberstroh (hh)
 * 
 */
public class Test extends TestCase {

	private static final String DATABASEPATH = Test.class.getResource("/db-1x1.db").getFile();

	private static File database;

	private static final String LOGURL = "at.haberstroh.urlybird.test";

	private Logger log = Logger.getLogger(LOGURL);

	private Data data;

	String[][] newdata = { { "Hotel Mama", "Waidhofen/Ybbs", "2", "N", "$100", "2008/03/23", "" },
			{ "Hotel Claudia", "Waidhofen/Ybbs", "2", "N", "$100", "2008/03/23", "" }, };

	private static Random rd = new Random();

	/**
	 * @param name
	 */
	public Test(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// check if database is available
		database = new File(DATABASEPATH);
		if (!(database.canRead() && database.canWrite())) {
			log.severe("database not available");
			throw new FileNotFoundException("database not available");
		}
		// save data file
		File db = new File(DATABASEPATH);
		File dbsav = new File(DATABASEPATH + ".sav");
		try {
			copyFile(db, dbsav);
		} catch (IOException e) {
			log.severe("could not save database");
		}
		data = new Data(database);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		data = null;
		Runtime r = Runtime.getRuntime();
		r.gc(); // run garbage collector to free memory
		// restore original data file
		File db = new File(DATABASEPATH);
		File dbsav = new File(DATABASEPATH + ".sav");
		try {
			copyFile(dbsav, db);
		} catch (IOException e) {
			log.severe("could not restore database");
		} finally {
			log.info("database restored");
		}
	}

	/**
	 * Test method for {@link suncertify.db.Data#Data()}.
	 */
	public void _testData() {
		// TODO: check what happens, when data file does not exist
		// fail("Not yet implemented");
	}

	/**
	 * show all records
	 * 
	 * @throws RemoteException
	 */
	public void test1showData() {
		for (int i = 0; i < 31; i++) {
			try {
				String[] rec = data.read(i);
				System.out.printf("%2d: ", i);
				for (String field : rec) {
					System.out.printf(" '%s'", field.trim());
				}
				System.out.println();
			} catch (suncertify.db.RecordNotFoundException e) {
				System.out.println(" deleted");
			}
		}
	}

	/**
	 * delet one reecord incl. locking
	 * 
	 * @param recNo record to be deleted
	 * @throws RecordNotFoundException
	 */
	private void delete(int recNo) throws RecordNotFoundException, SecurityException {
		long lockcookie = data.lock(recNo);
		data.delete(recNo, lockcookie);
		data.unlock(recNo, lockcookie);
	}

	/**
	 * Test method for {@link suncertify.db.Data#create(java.lang.String[])}.
	 * 
	 * Data should be written to file. Deleted records should be reused. Duplicates should cause an
	 * DuplicateKeyException.
	 */
	public void testCreate() {
		System.out.print("testCreate");
		// create new record to be appended
		int recNo = -1;
		try {
			recNo = data.create(newdata[0]);
		} catch (DuplicateKeyException e) {
			fail("testCreate: should not have thrown DuplicateKeyException");
		}
		assertEquals(31, recNo);

		// delete two records and create 2 records - deleted records should be
		// reused
		try {
			delete(10);
			delete(13);
			try {
				recNo = data.create(newdata[1]);
			} catch (DuplicateKeyException e) {
				fail("testCreate: should not have thrown DuplicateKeyException");
			}
			assertTrue("testCreate: should be one of 10 or 13", 10 == recNo || 13 == recNo);
		} catch (RecordNotFoundException e) {
			fail("testCreate: should not have thrown RecordNotFoundException");
		} catch (SecurityException e) {
			fail("testCreate: should not have thrown SecurityException");
		}
		System.out.println("...passed");
	}

	/**
	 * Test method for {@link suncertify.db.Data#delete(int, long)}.
	 * 
	 * Data should be written to file. Should throw RecordNotFoundException if invalid recNo.
	 */
	public void testDelete() {
		System.out.print("testDelete");
		// delete record, test if really deleted (read ->
		// RecordNotFoundException)
		try {
			delete(12);
		} catch (RecordNotFoundException e) {
			fail("testDelete: should not have thrown RecordNotFoundException");
		} catch (SecurityException e) {
			fail("testDelete: should not have thrown SecurityException");
		}
		try {
			@SuppressWarnings("unused")
			String[] strings = data.read(12);
			fail("testDelete: should have thrown RecordNotFoundException");
		} catch (RecordNotFoundException e) {
		}

		// try to delete invalid record -> RecordNotFoundException
		try {
			delete(100);
			fail("testDelete: should have thrown RecordNotFoundException");
		} catch (RecordNotFoundException e) {
		} catch (SecurityException e) {
		}

		// try to delete already deleted record -> RecordNotFoundException
		try {
			delete(12);
			fail("testDelete: should have thrown RecordNotFoundException");
		} catch (RecordNotFoundException e) {
		} catch (SecurityException e) {
		}

		System.out.println("...passed");
	}

	/**
	 * Test method for {@link suncertify.db.Data#find(java.lang.String[])}.
	 */
	public void testFind() {
		System.out.print("testFind");
		int[] result;
		// match all -> all records
		// find all with empty Strings
		String[] all = { "", "", "", "", "", "", "" };
		result = data.find(all);
		assertTrue(31 == result.length);
		// find all with null (Strings)
		String[] allnull = { null, null, null, null, null, null, null };
		result = data.find(allnull);
		assertTrue(31 == result.length);

		// no match -> no record
		String[] nomatch = { "XX", "", "", "", "", "", "" };
		result = data.find(nomatch);
		assertTrue(0 == result.length);

		// match
		// Hotel "Excel" -> 6 Hotels
		String[] excel = { "Excel", null, null, null, null, null, null };
		int[] resultset = { 2, 7, 14, 27 };
		result = data.find(excel);
		assertEquals(4, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(resultset[i], result[i]);
		}

		// Location "Xanadu" -> 2 Hotels
		String[] xanadu = { null, "Xanadu", null, null, null, null, null };
		int[] xanaduResult = { 21, 22 };
		resultset = xanaduResult;
		result = data.find(xanadu);
		assertEquals(2, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(resultset[i], result[i]);
		}

		// room size 5 -> not available, 0 hotels
		String[] roomsize = { null, null, "5", null, null, null, null };
		result = data.find(roomsize);
		assertEquals(0, result.length);

		// location "Atlant", size 4 -> 2 hotels
		String[] atlantis = { null, "Atlant", "4", null, null, null, null };
		int[] atlanisResult = { 13, 15 };
		resultset = atlanisResult;
		result = data.find(atlantis);
		assertEquals(2, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(resultset[i], result[i]);
		}

		System.out.println("...passed");
	}

	/**
	 * Test method for {@link suncertify.db.Data#lock(int)}.
	 * 
	 * Should test if thread does not use CPU when waiting for lock. Should throw RecordNotFoundException if invalid
	 * recNo.
	 */
	public void testLock() {
		System.out.print("testLock");
		// record not found
		try {
			@SuppressWarnings("unused")
			long cookie = data.lock(100);
			fail("testLock: should have thrown RecordNotFoundException");
		} catch (RecordNotFoundException e) {
		}
		// test locking
		try {
			long cookie = data.lock(1);
			assertTrue("testLock: cookie should not be 0", cookie != 0);
			data.unlock(1, cookie);
		} catch (RecordNotFoundException e) {
			fail("testLock: should not have thrown RecordNotFoundException");
		} catch (SecurityException e) {
			fail("testLock: should not have thrown SecurityException");
		}

		// Thread-Test with same Data-Object
		final RunnableCatch tr1 = new RunnableCatch(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("thread 1");
					try {
						Thread.sleep(500); // first wait
					} catch (InterruptedException e) {
					}
					log.info("thread 1 lock");
					long cookie = data.lock(1);
					assertTrue("testLock: cookie should not be 0", cookie != 0);
					for (int i = 0; i < 10000; i++) {
						@SuppressWarnings("unused")
						double y = Math.log((double) i);
					}
					log.info("thread 1 unlock");
					data.unlock(1, cookie);
				} catch (SecurityException e) {
					fail("testLock: should not have thrown RecordNotFoundException");
				} catch (RecordNotFoundException e) {
					fail("testLock: should not have thrown RecordNotFoundException");
				}
			}
		});
		final Thread t1 = new Thread(tr1, "thread 1");
		final RunnableCatch tr2 = new RunnableCatch(new Runnable() {
			@Override
			public void run() {
				log.info("thread 2");
				try {
					log.info("thread 2 lock");
					long cookie = data.lock(1); // first lock
					assertTrue(cookie != 0);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					// check if t1 is really waiting
					assertTrue("testLock: t1 should wait", t1.getState() == Thread.State.WAITING);
					for (int i = 0; i < 10000; i++) {
						@SuppressWarnings("unused")
						double y = Math.log((double) i);
					}
					// check if t1 is really waiting
					assertTrue("testLock: t1 should wait", t1.getState() == Thread.State.WAITING);
					log.info("thread 2 unlock");
					data.unlock(1, cookie);
				} catch (SecurityException e) {
					fail("testLock: should not have thrown RecordNotFoundException");
				} catch (RecordNotFoundException e) {
					fail("testLock: should not have thrown RecordNotFoundException");
				}
			}
		});
		final Thread t2 = new Thread(tr2, "thread 2");
		t1.start();
		t2.start();
		try {
			t1.join();
			assertNull("Exception in thread 1", tr1.getException());
			t2.join();
			assertNull("Exception in thread 2", tr2.getException());
		} catch (InterruptedException e) {
			System.err.println(e.getLocalizedMessage());
		}

		// more threads try to lock data
		int n = 100;
		final Thread[] threads = new Thread[n];
		final RunnableCatch[] runnables = new RunnableCatch[n];
		for (int i = 0; i < n; i++) {
			final String threadname = "thread_" + i;
			runnables[i] = new RunnableCatch(new Runnable() {

				@Override
				public void run() {
					long wait = rd.nextInt(10) * 100;
					try {
						Thread.sleep(wait);
					} catch (InterruptedException e1) {
					}
					long lockCookie;
					try {
						lockCookie = data.lock(1);
						String[] rec = data.read(1);
						// do something
						wait = rd.nextInt(10) * 1000;
						for (int i = 0; i < wait; i++) {
							@SuppressWarnings("unused")
							double y = Math.log((double) i);
						}
						rec[0] = threadname;
						data.update(1, rec, lockCookie);
						data.unlock(1, lockCookie);
					} catch (RecordNotFoundException e) {
						fail("shouldn't throw RecordNotFoundException");
					} catch (SecurityException e) {
						fail("shouldn't throw SecurityException");
					}
				}

			});
			threads[i] = new Thread(runnables[i]);
		}
		for (int i = 0; i < n; i++) {
			threads[i].start();
		}
		for (int i = 0; i < n; i++) {
			try {
				threads[i].join();
				assertNull("Exception in Subthread", runnables[i].getException());
			} catch (InterruptedException e) {
				fail("shouldn't be interrupted");
			}
		}

		System.out.println("...passed");
		// test1showData();
	}

	/**
	 * Test method for {@link suncertify.db.Data#read(int)}.
	 */
	public void testRead() {
		System.out.print("testRead");
		// try to read non existing record -> RecordNotFoundException
		try {
			@SuppressWarnings("unused")
			String[] strings = data.read(100);
			fail("testRead: should have thrown RecordNotFoundException");
		} catch (RecordNotFoundException e) {
		}
		// try to read valid record - check data
		try {
			String[] strings = data.read(0);
			assertTrue("Palace".equals(strings[0].trim()));
		} catch (RecordNotFoundException e) {
			fail("testRead: should not have thrown RecordNotFoundException");
		}
		System.out.println("...passed");
	}

	/**
	 * Test method for {@link suncertify.db.Data#unlock(int, long)}.
	 * 
	 * Should give CPU to one of waiting threads. Should throw SecurityException if lockCookie not correct. Should throw
	 * RecordNotFoundException if invalid recNo.
	 */
	public void testUnlock() {
		System.out.print("testUnlock");
		// simple (lock/)unlock
		try {
			long cookie = data.lock(1);
			assertTrue("testUnlock: cookie shold not be 0", cookie != 0);
			data.unlock(1, cookie);
		} catch (RecordNotFoundException e) {
			fail("testUnlock: should not have thrown RecordNotFoundException");
		} catch (SecurityException e) {
			fail("should not have thrown SecurityException");
		}
		// try to unlock with illegal cookie
		long cookie = 1234;
		try {
			data.unlock(1, cookie);
			fail("testUnlock: should have thrown SecurityException");
		} catch (SecurityException e) {
		} catch (RecordNotFoundException e) {
			fail("testUnlock: should not have thrown RecordNotFoundException");
		}
		System.out.println("...passed");
	}

	/**
	 * Test method for {@link suncertify.db.Data#update(int, java.lang.String[], long)}.
	 * 
	 * Should throw SecurityException if lockCookie not correct. Should throw RecordNotFoundException if invalid recNo.
	 * Data should be written to file.
	 * 
	 * @throws IOException
	 */
	public void testUpdate() throws IOException {
		System.out.print("testUpdate");
		// update without locking
		try {
			long cookie = 1234;
			String[] record = data.read(0);
			record[0] = "UPDATE no lock";
			data.update(28, record, cookie);
			fail("testUpdate: should have thrown SecurityException");
		} catch (SecurityException e) {
		} catch (RecordNotFoundException e) {
			fail("testUpdate: should not have thrown RecordNotFoundException");
		}

		// not existing record
		try {
			long cookie = 1234;
			String[] record = data.read(0);
			record[0] = "UPDATE not existing";
			data.update(100, record, cookie);
			fail("testUpdate: should have thrown SecurityException");
		} catch (SecurityException e) {
		} catch (RecordNotFoundException e) {
			fail("testUpdate: should not have thrown RecordNotFoundException");
		}

		// update with locking
		try {
			long cookie = data.lock(28);
			String[] record = data.read(28);
			record[0] = "UPDATE ok";
			data.update(28, record, cookie);
			data.unlock(28, cookie);
			record = data.read(28);
			assertTrue("testUpdate: shold have 'UPDATE ok'", "UPDATE ok".equals(record[0].trim()));
		} catch (SecurityException e) {
			fail("testUpdate: should not have thrown SecurityException");
		} catch (RecordNotFoundException e) {
			fail("testUpdate: should not have thrown RecordNotFoundException");
		}

		// check if data was written
		data = null;
		Runtime r = Runtime.getRuntime();
		r.gc(); // run garbage collector to free memory
		data = new Data(database);
		try {
			String[] record = data.read(28);
			assertTrue("testUpdate: should have 'UPDATE ok'", "UPDATE ok".equals(record[0].trim()));
		} catch (RecordNotFoundException e) {
			fail("testUpdate: should not have thrown RecordNotFoundException");
		}
		System.out.println("...passed");
	}

	/**
	 * 200 Threads versuchen gleichzeitig, Datensätze mit zufällig ermittelten Recordnummern zu upzudaten, zu löschen
	 * und neue zu erzeugen. Dabei sollen ca. 60% der Threads updaten, 30% neue erzeugen und der Rest löschen. Am Ende
	 * des Tests ist der neue Zustand des Datenbank-Files auf der Konsole auszugeben.
	 */
	public void testAll1() {
		System.out.print("testAll1");
		long time = new Date().getTime();
		int n = 2000; // Anzahl der Threads
		final Thread[] t = new Thread[n];
		final RunnableCatch[] rt = new RunnableCatch[n];
		final TreeMap<String, Integer> locks = new TreeMap<String, Integer>();

		for (int iThread = 0; iThread < n; iThread++) {
			final int tNum = iThread;
			// new String[data.getNumFields()] ... 7 * null
			final int[] recno = data.find(new String[7]);
			final int cntrec = recno.length;
			rt[iThread] = new RunnableCatch(new Runnable() {

				public void run() {
					try {
						int r = rd.nextInt(10);
						int rn = recno[rd.nextInt(cntrec)];
						if (r <= 6) { // 60%
							String[] f = data.read(rn);
							locks.put(getName(), rn);
							long lockCookie = data.lock(rn);
							f[6] = String.valueOf(rd.nextInt(9999));
							data.update(rn, f, lockCookie);
							data.unlock(rn, lockCookie);
							locks.remove(getName());
						} else if (r <= 9) { // 30%
							String name = t[tNum].getName();
							String[] f = { name, "City", "4", "N", "$1", "2010/01/01", "" };
							data.create(f);
						} else { // Rest
							locks.put(getName(), rn);
							long lockCookie = data.lock(rn);
							data.delete(rn, lockCookie);
							data.unlock(rn, lockCookie); // remove, if within
							// delete
							locks.remove(getName());
						}
					} catch (SecurityException e) {
						fail("SecurityException");
					} catch (RecordNotFoundException e) {
						fail("RecordNotFoundException"); // FIXME darf
						// passieren, zählen
					} catch (DuplicateKeyException e) {
						fail("DuplicateKeyException");
					}
				}
			});
		}
		for (int iThread = 0; iThread < n; iThread++) {
			t[iThread] = new Thread(rt[iThread]);
			t[iThread].start();
		}

		for (int i = 0; i < n; i++) {
			// System.out.println("join " + i);
			try {
				t[i].join();
				assertFalse("thread " + i, t[i].getState() == Thread.State.WAITING);
				if (rt[i].getException() != null) {
					rt[i].getException().printStackTrace();
				}
				assertNull("Exception in Subthread " + i + " " + rt[i].getException(), rt[i].getException());
			} catch (InterruptedException e) {
			}
		}
		System.out.printf("...passed (%dms)\n", new Date().getTime() - time);
		// test1showData();
	}

	/**
	 * copy a file
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * @return all Tests of this class
	 */
	public static TestSuite suite() {
		return new TestSuite(Test.class);
	}

	/**
	 * start Test as Application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		TestSuite suite = new TestSuite();
		int argi = 0;
		boolean gui = false;
		if (args.length > argi) {
			if (args[argi].equals("--gui") || args[argi].equals("-g")) {
				gui = true;
				argi++;
			}
			if (args.length > argi && (args[argi].equals("--help") || args[argi].equals("-h"))) {
				System.out.println("java suncertify.db.DataTest [-h|--help|-g|--gui] [test...]");
				System.out.println("  -h|--help ... this help");
				System.out.println("  -g|--gui  ... start with gui");
				System.out.println("  [test...] ... run just these tests (only textmode)");
				System.out.println("The database must be in actual directory.");
				return;
			}
		}
		if (args.length > argi) { // add tests from command line
			for (int i = argi; i < args.length; i++) {
				suite.addTest(new Test(args[i]));
			}
		} else { // add all tests
			suite.addTest(Test.suite());
		}
		if (gui) {
			// junit.swingui.TestRunner.run(DataTest.class);
		} else {
			junit.textui.TestRunner.run(suite);
		}
	}
}

/**
 * Catches exceptions thrown by a Runnable, so you can check/view them later.
 */
class RunnableCatch implements Runnable {

	/** Proxy we will run */
	private final Runnable _proxy;

	/** @guarded-by(this) */
	private Throwable _exception;

	public RunnableCatch(final Runnable proxy) {
		_proxy = proxy;
	}

	public void run() {
		try {
			_proxy.run();
		} catch (Throwable e) {
			synchronized (this) {
				_exception = e;
			}
		}
	}

	/** @return any exception that occured, or NULL */
	public synchronized Throwable getException() {
		return _exception;
	}
}
