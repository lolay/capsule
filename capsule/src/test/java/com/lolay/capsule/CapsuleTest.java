package com.eharmony.capsule;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;


public class CapsuleTest {
	
	@Test public void testCapsule() {
		Collection<NamedAttribute> attrs = new LinkedList<NamedAttribute>();
		
		attrs.add(new NamedAttribute("one",1));
		attrs.add(new NamedAttribute("two","the string two"));
		
		Capsule capsule = new Capsule(attrs);
		
		byte[] bytes = capsule.toBytes();
		Capsule fromBytes = new Capsule.SerialForm().parse(bytes);
		
		assertEquals(capsule,fromBytes);
	}

}
