package com.eharmony.capsule.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSessionAnalyzer implements ServletRequestListener {

	public static boolean isEnabled() {
		String property = System.getProperty("capsule.session.analysis.enabled","false");
		return Boolean.valueOf(property);
	}
	
	public static String statusString() {
		return isEnabled() ? "enabled" : "disabled";
	}
	
	public final void requestInitialized(ServletRequestEvent arg0) {
		
		; // No op
		
	}
	
	public final void requestDestroyed(ServletRequestEvent sre) {
		ServletRequest request = sre.getServletRequest();
		if (isEnabled() && hasSession(request)) {
			analyzeSession(getSession(request));
		}
		
	}

	public abstract void analyzeSession(HttpSession session);

	private boolean hasSession(ServletRequest request) {
		return request instanceof HttpServletRequest && getSession(request) != null;
	}

	private HttpSession getSession(ServletRequest request) {
		return ((HttpServletRequest)request).getSession(false);
	}
	
	

}
