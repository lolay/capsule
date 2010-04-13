package com.eharmony.capsule.chain;

import static org.junit.Assert.assertEquals;

import java.util.TreeMap;

import org.junit.Test;

public class CookieHeaderTest {
	
	@Test
	public void testCompareTo() {
		CookieHeader header1 = new CookieHeader("capsule0", 0);
		CookieHeader header2 = new CookieHeader("capsule1", 1);
		assertEquals("", -1, header1.compareTo(header2));
	}
	
	@Test
	public void testSortedMap() {
		TreeMap<CookieHeader, String> map = new TreeMap<CookieHeader, String>();
		map.put(new CookieHeader("capsule3", 3), "3");
		map.put(new CookieHeader("capsule2", 2), "2");
		map.put(new CookieHeader("capsule1", 1), "1");
		map.put(new CookieHeader("capsule0", 0), "0");
		Integer expected[] = new Integer[]{0,1,2,3};
		int index = 0;
		for (CookieHeader header : map.keySet()) {
			assertEquals(expected[index++], header.getIndex());
		}
	}

}