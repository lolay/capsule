package com.eharmony.capsule;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/**
 * KeyManager is a factory for versioned keys used in Capsule
 * 
 * @author jtuberville
 *
 */
public class KeyManager {
	private static final int KEY_STRENGTH = 128; // 192 and 256 bits may not be available
	private static final String ALGORITHM = "AES";
	private static final SecretKey DEFAULT_KEY = new SecretKeySpec(new byte[]{-17, -97, 91, -82, -123, -35, 124, 71, -45, -80, 76, 99, 7, -10, 2, 41},ALGORITHM);
	private static final Map<Short, SecretKey> keyring = new HashMap<Short, SecretKey>(){{ put((short) 1,DEFAULT_KEY);}} ;
	
	
	public short getLatestVersion() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	/**
	 * 
	 * Convenience method to generate a valid key.
	 * 
	 * @return key
	 * @throws CapsuleException if there are any exceptions while attempting to encrypt 
	 */
	public SecretKey createKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new CapsuleException(e);
		}
	       kgen.init(KEY_STRENGTH); 
	
	
	       // Generate the secret key specs.
	       SecretKey skey = kgen.generateKey();
	       byte[] raw = skey.getEncoded();
	
	       SecretKey key = new SecretKeySpec(raw, ALGORITHM);
		return key;
	}

	public SecretKey getKeyForVersion(short keyVersion) {
		return keyring.get(keyVersion);
	}

}
