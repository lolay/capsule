package com.lolay.capsule.chain;

/**
 * Used by CookieCommand to process capsule cookies.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class CookieHeader implements Comparable<CookieHeader> {
	private final String name;
	private final Integer index;
	public CookieHeader(String name, Integer index) {
		this.name = name;
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public Integer getIndex() {
		return index;
	}
	public int compareTo(CookieHeader header) {
		return this.index.compareTo(header.getIndex());
	}
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		boolean b = false;
		if (obj instanceof CookieHeader) {
			CookieHeader header = (CookieHeader) obj;
			b = this.name.equals(header.getName());
		}
		return b;
	}
	@Override
	public String toString() {
		return String.format("CookieHeader [name=%1$s, index=%2$s]", name, index);
	}
}
