package com.chinaroad.bubble.adapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import com.chinaroad.foundation.utils.RandomUtils;

public class SessionAdapterTest {

	private Set<Integer> ids = new HashSet<Integer>();
	
	@Test
	public void test() {
		final String forWho = "visitor";
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < 1000; i++) {
					int id = SessionAdapter.generate(forWho);
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
				for (int i = 0; i < 1000; i++) {
					Iterator<Integer> iter = ids.iterator();
					for (;iter.hasNext();) {
						int id = iter.next();
						if (RandomUtils.randomRange(1, 9) > 7) {
							System.err.println("Remove: " + id);
							SessionAdapter.release(id, forWho);
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
		
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
