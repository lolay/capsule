package com.eharmony.capsule.codec;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * Bytestream transcoder to represent binary streams as String representable streams.
 * @see Section 6.8. Base64 Content-Transfer-Encoding from RFC 2045 Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies by Freed and Borenstein.
 * 
 * @author jtuberville
 *
 */
public class Base64Transcoder implements Transcoder<byte[], byte[]> {

	public byte[] decode(byte[] sequence1) {
		return Base64.decodeBase64(sequence1);
	}

	public byte[] encode(byte[] sequence1) {
		return Base64.encodeBase64(sequence1);
	}
	
}
