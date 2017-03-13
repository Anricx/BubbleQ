package com.chinaroad.bubble.context;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.chinaroad.bubble.context.SessionManager;
import com.chinaroad.foundation.utils.RandomUtils;

public class SessionManagerTest {

	private Set<Integer> ids = new HashSet<Integer>();
	
	@Test
	public void test() {
		final String forWho = "visitor";
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					int id = SessionManager.generate(forWho);
					ids.add(id);
					System.out.println("Get:" + id);
					try {
						Thread.sleep(RandomUtils.randomRange(500, 1000));
					} catch (InterruptedException e) { }
				}
			}
			
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					Iterator<Integer> iter = ids.iterator();
					for (;iter.hasNext();) {
						int id = iter.next();
						if (RandomUtils.randomRange(1, 9) > 7) {
							System.err.println("Remove: " + id);
							SessionManager.release(id, forWho);
							iter.remove();
							break;
						}
					}
					System.out.println(ids);
					try {
						Thread.sleep(RandomUtils.randomRange(500, 1000));
					} catch (InterruptedException e) { }
				}
			}
			
		}).start();
	}

}
