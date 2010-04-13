package com.lolay.capsule.chain;

/**
 * Thrown when the session data size exceeds the maximum allowable
 * size.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class CookiePolicyException extends Exception {
	
	private static final long serialVersionUID = -6879889816711034549L;

	public CookiePolicyException() {
		super();
	}
	
	public CookiePolicyException(String message) {
		super(message);
	}

}
