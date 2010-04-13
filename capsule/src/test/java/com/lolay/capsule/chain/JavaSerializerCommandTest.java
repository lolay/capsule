package com.eharmony.capsule.chain;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eharmony.capsule.NamedAttribute;

public class JavaSerializerCommandTest {
	
	private static Collection<NamedAttribute> attributes;
	private static Context context;
	private static Map<String, String> testMap;
	private static java.util.Date testDate;
	
	@SuppressWarnings({ "serial" })
	@BeforeClass
	public static void init() {
		attributes = new LinkedList<NamedAttribute>();
		testMap = new HashMap<String, String>(){{put("test", "test"); put("test2", "test2");}};
		attributes.add(new NamedAttribute("testMap", testMap));
		testDate = new java.util.Date();
		attributes.add(new NamedAttribute("testDate", testDate));
		context = new ContextBase();
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testExecute1() {
		try {
			Command command = new JavaSerializerCommand();
			context.put("attributes", attributes);
			context.put("mode", Mode.OUT);
			command.execute(context);
			byte[] serializedBytes = (byte[]) context.get("bytes");
			assertNotNull("serializedBytes is null", serializedBytes);
			assertTrue("serializedBytes is zero length", serializedBytes.length > 0);
		}
		catch (Exception e) {
			fail(String.format("Exception in testExecuteIn: %1$s", e));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecute2() {
		try {
			Command command = new JavaSerializerCommand();
			assertNotNull("context attribute bytes is null", context.get("bytes"));
			context.remove("attributes");
			context.put("mode", Mode.IN);
			command.execute(context);
			Collection<NamedAttribute> attribs = (Collection<NamedAttribute>)context.get("attributes");
			assertNotNull("attributes is null", attribs);
			assertTrue("attributes size is not 2", (attribs.size() == 2));
			for (NamedAttribute attrib : attribs) {
				if (attrib.getName().equals("testMap")) {
					assertEquals("testMap attribute does not match", testMap, attrib.getValue());
				}
				else if (attrib.getName().equals("testDate")) {
					assertEquals("testDate attribute does not match", testDate, attrib.getValue());
				}
			}
		}
		catch (Exception e) {
			fail(String.format("Exception in testExecuteIn: %1$s", e));
		}
	}

}
