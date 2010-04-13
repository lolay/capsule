package com.eharmony.capsule.codec;

import static org.junit.Assert.assertEquals;

import javax.crypto.SecretKey;

import org.junit.Test;

import com.eharmony.capsule.KeyManager;
import com.eharmony.capsule.codec.CryptoTranscoder;


public class CryptoTranscoderTest {
	private CryptoTranscoder transcoder;
	private String TEST_STRING = "Package javax.crypto Description " +
							"Provides the classes and interfaces for cryptographic operations. The cryptographic operations " +
							"defined in this package include encryption, key generation and key agreement, and Message Authentication Code (MAC) generation." +
							"Support for encryption includes symmetric, asymmetric, block, and stream ciphers. This package also supports secure streams and sealed objects." +
							"Many of the classes provided in this package are provider-based. The class itself defines a programming interface to which " +
							"applications may write. The implementations themselves may then be written by independent third-party vendors and plugged in seamlessly " +
							"as needed. Therefore application developers may take advantage of any number of provider-based implementations without having to add or rewrite code.";
	public CryptoTranscoderTest() throws Exception {
	    
		transcoder = new CryptoTranscoder((short) 1);
	}

	@Test public void testAll() {
		byte[] ciphertext = transcoder.encode(TEST_STRING.getBytes());
		String cleartext = new String(transcoder.decode(ciphertext));
		assertEquals(TEST_STRING, cleartext);
	}
	
	

}
