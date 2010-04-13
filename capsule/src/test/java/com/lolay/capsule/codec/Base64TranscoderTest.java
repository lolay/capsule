package com.eharmony.capsule.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.eharmony.capsule.codec.Base64Transcoder;
import com.eharmony.capsule.codec.Transcoder;


public class Base64TranscoderTest {
	Transcoder<byte[],byte[]>transcoder = new Base64Transcoder();
	String testString = "testThis";

	
	@Test public void testEncode() {
		
		byte[] serialForm = transcoder.encode(testString.getBytes());
		
		assertTrue(serialForm.length > 0);
		
		System.out.printf("Serialized %d bytes\n",serialForm.length);
		
	}
	
	@Test public void testDecode() {
		
		byte[] serialForm = transcoder.encode(testString.getBytes());
		
		String output = new String(transcoder.decode(serialForm));
		
		assertEquals(testString, output);
		System.out.printf("Deserialized %d bytes\n",serialForm.length);
		
	}

}
