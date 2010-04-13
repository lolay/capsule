package com.eharmony.capsule.chain;

import static org.junit.Assert.*;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.junit.Test;

public class Base64CommandTest {
	
	private String testString = "To be or not to be? That is the question.";
	private String testStringEncoded = "VG8gYmUgb3Igbm90IHRvIGJlPyBUaGF0IGlzIHRoZSBxdWVzdGlvbi4=";

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteIn() {
		try {
			Command command = new Base64Command();
			Context context = new ContextBase();
			context.put("mode", Mode.IN);
			context.put("bytes", testStringEncoded.getBytes());
			command.execute(context);
			byte[] decodedBytes = (byte[]) context.get("bytes");
			String s = new String(decodedBytes);
			assertEquals("", testString, s);
		}
		catch (Exception e) {
			fail(String.format("Exception in testExecuteIn: %1$s", e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteOut() {
		try {
			Command command = new Base64Command();
			Context context = new ContextBase();
			context.put("mode", Mode.OUT);
			context.put("bytes", testString.getBytes());
			command.execute(context);
			byte[] encodedBytes = (byte[]) context.get("bytes");
			String s = new String(encodedBytes);
			assertEquals("", testStringEncoded, s);
		}
		catch (Exception e) {
			fail(String.format("Exception in testExecuteOut: %1$s", e));
		}
	}

}
