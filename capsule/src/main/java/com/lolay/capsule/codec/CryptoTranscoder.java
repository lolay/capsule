package com.eharmony.capsule.codec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.eharmony.capsule.CapsuleException;
import com.eharmony.capsule.KeyManager;

/**
 * A transcoder that encrypts and decrypts a byte stream.  The algorithm is fixed as AES for now.
 * 
 * @author jtuberville
 *
 */
public class CryptoTranscoder implements Transcoder<byte[], byte[]> {
	private final Cipher encrypter;
	private final Cipher decrypter;
	private static final KeyManager keyManager = new KeyManager();
	
	/**
	 * Constructs a new CryptoTranscoder using the default key
	 * 
	 * @throws CapsuleException if there are any exceptions while initializing crypto system
	 */
	public CryptoTranscoder() {
		this(keyManager.getLatestVersion());
	}
	
	/**
	 * Exposing this property so we can use it to construct the cookie
	 * header.
	 * @return
	 */
	public short getLatestVersion() {
		return keyManager.getLatestVersion();
	}
	
	/**
	 * Constructs a new CryptoTranscoder using the specified key
	 * 
	 * @param key
	 * 
	 * @throws CapsuleException if there are any exceptions while initializing crypto system
	 */
	public CryptoTranscoder(short keyVersion) {
		SecretKey key = keyManager.getKeyForVersion(keyVersion);
		try {
			encrypter = Cipher.getInstance(key.getAlgorithm());
			encrypter.init(Cipher.ENCRYPT_MODE, key);
			decrypter = Cipher.getInstance(key.getAlgorithm());
			decrypter.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			throw new CapsuleException(e);
		} 
	}
	
	/**
	 * Decrypts a byte stream
	 * 
	 * @throws CapsuleException if there are any exceptions while attempting to decrypt 
	 */
	public byte[] decode(byte[] ciphertext) {
		byte[] cleartext;
		try {
			cleartext = decrypter.doFinal(ciphertext);
		} catch (Exception e) {
			throw new CapsuleException(e);
		}
		return cleartext;
	}

	/**
	 * Encrypts a byte stream
	 * 
	 * @throws CapsuleException if there are any exceptions while attempting to encrypt 
	 */
	public byte[] encode(byte[] cleartext) {
		byte[] ciphertext;
		try {
			ciphertext = encrypter.doFinal(cleartext);
		} catch (Exception e) {
			throw new CapsuleException(e);
		}
		return ciphertext;
	}
	


}
