package com.lolay.capsule;


/**
 * 
 * Unchecked exception representing all capsule exceptions
 * 
 * @author jtuberville
 *
 */
public class CapsuleException extends RuntimeException {

	private static final long serialVersionUID = -1823092131512022632L;

	public CapsuleException(Exception e) {
		super(e);
	}

	public CapsuleException(String text, Exception e) {
		super(text, e);
	}

}
