package com.eharmony.capsule.codec;

import java.util.Map;

/**
 * 
 * Instruments a specified Transcoder saving encoding/decoding time to specified map using specified keys.
 * 
 * @author jtuberville
 *
 * @param <S1> orginal type
 * @param <S2> encoded type
 */
public class InstrumentedTranscoder<S1, S2> {
	private Transcoder<S1, S2> transcoder;
	private String encodeKey;
	private String decodeKey;
	
	
	/**
	 * Creates an InstrumentedTranscoder
	 * 
	 * @param transcoder transcoder to be instrumented
	 * @param encodeKey key to record encoding time
	 * @param decodeKey key to record decoding time
	 */
	public InstrumentedTranscoder(Transcoder<S1,S2> transcoder, String encodeKey, String decodeKey) {
		if (transcoder == null) {
			throw new IllegalArgumentException("Transcoder was null");
		}
		
		this.transcoder = transcoder;
		this.encodeKey = encodeKey;
		this.decodeKey = decodeKey;
	}

	public S1 decode(S2 sequence2, Map<String,Long> stats) {
		long start = System.nanoTime();
		S1 result = transcoder.decode(sequence2);
		long decodeTime = System.nanoTime() - start;
		stats.put(decodeKey, decodeTime);
		return result;
	}

	public S2 encode(S1 sequence1, Map<String,Long> stats) {
		long start = System.nanoTime();
		S2 result = transcoder.encode(sequence1);
		long encodeTime = System.nanoTime() - start;
		stats.put(encodeKey, encodeTime);
		return result;
	}

}
