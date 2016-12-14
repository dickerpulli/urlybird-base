package suncertify.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataConcurrencyTest {

	public static final double WAITING = 50;
	private File backup;
	private File file;

	private Data data;

	private String[][] records = new String[31][7];

	@BeforeClass
	public static void beforeAll() {
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.FINE);
		logger.getHandlers()[0].setLevel(Level.FINE);
		logger.getHandlers()[0].setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				StringBuffer sb = new StringBuffer();
				Date date = new Date(record.getMillis());
				sb = sb.append("[" + date + "] ");
				sb = sb.append(record.getLevel().getLocalizedName() + " (");
				sb = sb.append("Thread_" + record.getThreadID() + ":");
				sb = sb.append(record.getLoggerName() + ":");
				sb = sb.append(record.getSourceMethodName() + ") ");
				sb = sb.append(record.getMessage());
				sb = sb.append("\n");
				return sb.toString();
			}
		});
	}

	@Before
	public void before() throws Exception {
		file = new File(this.getClass().getResource("/db-1x1.db").getFile());
		backup = new File(this.getClass().getResource("/").getFile(), "db-1x1.db.bak");
		FileUtils.copyFile(file, backup);
		data = new Data(file);
		for (int i = 0; i < 31; i++) {
			records[i] = data.read(i);
		}
	}

	@After
	public void after() throws Exception {
		FileUtils.copyFile(backup, file);
	}

	@Test
	public void testBrutforce() throws Exception {
		Set<AssertionThread> threads = new HashSet<AssertionThread>();
		for (int i = 0; i < 100; i++) {
			ReadingThread readingThread = new ReadingThread("ReadingThread" + i, 0, 9);
			readingThread.start();
			threads.add(readingThread);
		}
		for (int i = 0; i < 100; i++) {
			UpdatingThread updatingThread = new UpdatingThread("UpdatingThread" + i, 10, 19);
			updatingThread.start();
			threads.add(updatingThread);
		}
		for (int i = 0; i < 100; i++) {
			CreatingThread creatingThread = new CreatingThread("CreatingThread" + i, 20, 25);
			creatingThread.start();
			threads.add(creatingThread);
		}
		for (int i = 0; i < 10; i++) {
			DeletingThread deletingThread = new DeletingThread("DeletingThread" + i, 26, 31);
			deletingThread.start();
			threads.add(deletingThread);
		}
		while (!threads.isEmpty()) {
			AssertionThread thread = threads.iterator().next();
			thread.join();
			if (!thread.getExceptions().isEmpty()) {
				for (Throwable t : thread.getExceptions()) {
					t.printStackTrace(System.err);
				}
				fail("Exceptions found in thread");
			}
			threads.remove(thread);
		}
	}

	/**
	 * A thread that reads ...
	 */
	private class ReadingThread extends AssertionThread {

		private int from;
		private int to;

		public ReadingThread(String name, int from, int to) {
			super(name);
			this.from = from;
			this.to = to;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				for (int i = from; i < to; i++) {
					sleep((int) (WAITING * new Random().nextDouble()));
					String[] recordFields = data.read(i);
					String[] originalFields = records[i];
					assertArrayEquals(recordFields, originalFields);
				}
			} catch (Throwable e) {
				exceptions.add(e);
			}
		}
	}

	/**
	 * A thread that updates ...
	 */
	private class UpdatingThread extends AssertionThread {

		private int from;
		private int to;

		public UpdatingThread(String name, int from, int to) {
			super(name);
			this.from = from;
			this.to = to;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				for (int i = from; i < to; i++) {
					sleep((int) (WAITING * new Random().nextDouble()));
					synchronized (UpdatingThread.class) {
						long lock = data.lock(i);
						String[] record = data.read(i);
						record[6] = String.valueOf(new Random().nextInt(9999));
						data.update(i, record, lock);
						data.unlock(i, lock);

						String[] recordFields = data.read(i);
						assertArrayEquals(recordFields, record);
					}
				}
			} catch (Throwable e) {
				exceptions.add(e);
			}
		}
	}

	/**
	 * A thread that updates ...
	 */
	private class DeletingThread extends AssertionThread {

		private int from;
		private int to;

		public DeletingThread(String name, int from, int to) {
			super(name);
			this.from = from;
			this.to = to;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				for (int i = from; i < to; i++) {
					sleep((int) (WAITING * new Random().nextDouble()));
					synchronized (DeletingThread.class) {
						long lock = data.lock(i);
						String[] record = data.read(i);
						data.delete(i, lock);
						data.unlock(i, lock);

						try {
							data.read(i);
							fail("Exception expected here");
						} catch (Exception e) {
							// yippie
						}

						data.create(record);
					}
				}
			} catch (Throwable e) {
				exceptions.add(e);
			}
		}
	}

	/**
	 * A thread that updates ...
	 */
	private class CreatingThread extends AssertionThread {

		private int from;
		private int to;

		public CreatingThread(String name, int from, int to) {
			super(name);
			this.from = from;
			this.to = to;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				for (int i = from; i < to; i++) {
					sleep((int) (WAITING * new Random().nextDouble()));
					synchronized (CreatingThread.class) {
						// String name = "Hotel" + i + new Random().nextInt();
						// String[] record = { name, "City", "4", "N", "$1",
						// "2010/01/01", "" };
						// data.create(record);
						//
						// String[] recordFields = data.read(i);
						// assertArrayEquals(recordFields, record);
					}
				}
			} catch (Throwable e) {
				exceptions.add(e);
			}
		}
	}

	/**
	 * Assertion thread
	 */
	private abstract class AssertionThread extends Thread {
		protected Set<Throwable> exceptions = new HashSet<Throwable>();

		public AssertionThread(String name) {
			super(name);
		}

		public Set<Throwable> getExceptions() {
			return exceptions;
		}
	}

}
