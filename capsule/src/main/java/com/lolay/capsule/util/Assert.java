package com.eharmony.capsule.util;

/**
 * Stolen shamelessly from Spring's Assert class.
 * 
 * @author <a href="mailto:JonStefansson@eharmony.com">Jon Stefansson</a>
 *
 */
public class Assert {
	
	public static void notNull(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
	}

	public static void notNull(Object obj, String message) {
		if (obj == null) {
			throw new IllegalArgumentException(message);
		}
	}

}
