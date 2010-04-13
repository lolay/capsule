package com.eharmony.capsule.chain;

/**
 * Each Command in the com.eharmony.capsule.chain package handles a specific
 * task in the processing chain. The work it does before the filter passes
 * control to the web app is the mirror image of the work it must do after 
 * the web app has completed its processing. This simple enum serves as a
 * marker to distinguish between the two modes.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public enum Mode {
	IN  ("in"),
	OUT ("out");
	
	private String name;
	
	Mode(String s) {
		this.name = s;
	}
	
	public String toString() {
		return name;
	}
}
