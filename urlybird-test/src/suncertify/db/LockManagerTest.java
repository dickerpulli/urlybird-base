package suncertify.db;

import org.junit.Test;

public class LockManagerTest {

	@Test
	public void testLockAndUnlock() {
		final LockManager lockManager = new LockManager();

		Thread thread1 = new Thread(new Runnable() {

			@Override
			public void run() {
				lockManager.lock(1);
				lockManager.lock(1);
			}
		});
		thread1.setDaemon(false);
		thread1.start();

		System.out.println("wait");

		Thread thread2 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					lockManager.unlock(1, 9999);
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}

		});
		thread2.setDaemon(false);
		thread2.start();
	}

}
