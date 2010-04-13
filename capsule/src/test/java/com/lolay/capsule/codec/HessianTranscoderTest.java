package com.eharmony.capsule.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.eharmony.capsule.NamedAttribute;


public class HessianTranscoderTest extends BaseTranscoderTest {
	private HessianSerializer<Collection<NamedAttribute>> transcoder = new HessianSerializer<Collection<NamedAttribute>>();

	@Before public void init() {
		super.init();
	}
	
	@Test public void testEncode() {
		
		byte[] serialForm = transcoder.encode(input);
		
		assertTrue(serialForm.length > 0);
		
		System.out.printf("Serialized %d bytes\n",serialForm.length);
		
	}
	
	@Test public void testDecode() {
		
		byte[] serialForm = transcoder.encode(input);
		
		Collection<NamedAttribute> output = transcoder.decode(serialForm);
		
		assertEquals(input, output);
		System.out.printf("Deserialized %d bytes\n",serialForm.length);
		
	}

}
