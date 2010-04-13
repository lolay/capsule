package com.eharmony.capsule.codec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.eharmony.capsule.NamedAttribute;


public class InstrumentedTranscoderTest extends BaseTranscoderTest {
	private static final String ENCODE = "encodeTime";
	private static final String DECODE = "decodeTime";
	
	private InstrumentedTranscoder<Collection<NamedAttribute>,byte[]> transcoder = new InstrumentedTranscoder<Collection<NamedAttribute>, byte[]>(new JavaSerializer<Collection<NamedAttribute>>(), ENCODE, DECODE);
	private HashMap<String, Long> stats;

	@Before public void init() {
		super.init();
		stats = new HashMap<String, Long>();

	}
	
	@Test public void testMeasureSerialization() {
//		HttpServletRequest request = createMock(HttpServletRequest.class);
//		HttpSession session = createMock(HttpSession.class);
//		expect(request.getSession(false)).andReturn(session);
		
		byte[] serialForm = transcoder.encode(input,stats);
		
		assertTrue(stats.get(ENCODE) >= 0);
		assertTrue(serialForm.length > 0);
		
		System.out.printf("Deserialized %d bytes in %d millis\n",serialForm.length,stats.get(ENCODE)/10000000);
		
	}
	
	@Test public void testMeasureDeserialization() {
		
		byte[] serialForm = transcoder.encode(input, stats);
		
		Collection<NamedAttribute> output = transcoder.decode(serialForm, stats);
		
		assertEquals(input, output);
		assertTrue(stats.get(DECODE) >= 0);
		System.out.printf("Deserialized %d bytes in %d millis\n",serialForm.length,stats.get(DECODE)/10000000);
		
	}

}
