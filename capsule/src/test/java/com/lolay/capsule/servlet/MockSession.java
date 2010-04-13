package com.eharmony.capsule.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MockSession implements HttpSession {
	private final Map<String, Object> attrs = new HashMap<String, Object>();

	public Object getAttribute(String key) {
		return attrs.get(key);
	}

	public Enumeration<String> getAttributeNames() {
		return new Enumeration<String>() {
			Iterator<String> keys = attrs.keySet().iterator();
			public boolean hasMoreElements() {
				return keys.hasNext();
			}
			public String nextElement() {
				return keys.next();
			}
		};
	}

	public long getCreationTime() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public String getId() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public long getLastAccessedTime() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public int getMaxInactiveInterval() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public ServletContext getServletContext() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public Object getValue(String arg0) {
		throw new UnsupportedOperationException("Not implmented");
	}

	public String[] getValueNames() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public void invalidate() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public boolean isNew() {
		throw new UnsupportedOperationException("Not implmented");
	}

	public void putValue(String key, Object value) {
		throw new UnsupportedOperationException("Not implmented");
	}

	public void removeAttribute(String arg0) {
		throw new UnsupportedOperationException("Not implmented");

	}

	public void removeValue(String arg0) {
		throw new UnsupportedOperationException("Not implmented");

	}

	public void setAttribute(String key, Object value) {
		attrs.put(key, value);
	}

	public void setMaxInactiveInterval(int arg0) {
		throw new UnsupportedOperationException("Not implmented");
	}

}
