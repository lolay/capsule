package com.eharmony.capsule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ConfigTest {
	
	@Test
	public void initTest() {
		try {
			Class configManagerClass = Class.forName("com.eharmony.configuration.SingletonConfigManagerImpl");
			java.lang.reflect.Method[] methods = configManagerClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if ("getInstance".equals(methods[i].getName())) {
					Object obj = methods[i].invoke(configManagerClass);
					assertNotNull("ConfigManager class is null", obj);
				}
			}
			/*
			com.eharmony.configuration.ConfigManager configManager = (com.eharmony.configuration.ConfigManager) obj;
			*/
		}
		catch (Throwable t) {
			System.out.println(String.format("Error in initTest: %1$s", t));
			fail("Could not initialize eh config");
		}
	}
	
}