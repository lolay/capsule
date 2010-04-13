package com.eharmony.capsule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.eharmony.capsule.chain.CapsuleConfig;

public class CapsuleConfigTest {
	
	@Test
	public void enabledTest() {
		CapsuleConfig config = new CapsuleConfig();
		config.setFilterEnabled("true");
		assertTrue(config.isFilterEnabled());
	}
	
	@Test
	public void defaultTest() {
		CapsuleConfig config = new CapsuleConfig();
		assertFalse(config.isFilterEnabled());
	}
	
	@Test
	public void disabledTest() {
		CapsuleConfig config = new CapsuleConfig();
		config.setFilterEnabled("false");
		assertFalse(config.isFilterEnabled());
	}
	
}