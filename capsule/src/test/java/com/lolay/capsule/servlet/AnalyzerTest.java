package com.eharmony.capsule.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.eharmony.capsule.servlet.DryRunAnalyzer;

import static org.easymock.EasyMock.*;

public class AnalyzerTest {
	
	private HttpServletRequest request;
	private HttpSession session;
	private Map<String, Object> attrs;
	private ServletRequestEvent event;
		
	@Before public void init() {
		System.setProperty("capsule.session.analysis.enabled", "true");
		attrs = new HashMap<String, Object>();
		attrs.put("key1", "value1xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		attrs.put("key2", Integer.valueOf(87343234));
		
		
		session = new MockSession();
		for (String key : attrs.keySet()) {
			session.setAttribute(key, attrs.get(key));
		}
		
		request = createMock(HttpServletRequest.class);
		expect(request.getSession(false)).andReturn(session).times(2);
		replay(request);
		ServletContext context = createMock(ServletContext.class);
		event = new ServletRequestEvent(context,request);
		
	}
	
	@Test public void testMonitor() {
		DryRunAnalyzer analyzer = new DryRunAnalyzer();
		System.setProperty("enable.session.measurement","true");
		analyzer.requestDestroyed(event);
	}

	@Test public void testBasicAnalyzer() {
		SessionAnalyzer analyzer = new SessionAnalyzer();
		System.setProperty("enable.session.measurement","true");
		analyzer.requestDestroyed(event);
	}


}
