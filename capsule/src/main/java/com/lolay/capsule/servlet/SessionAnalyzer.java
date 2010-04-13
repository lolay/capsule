package com.eharmony.capsule.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

public class SessionAnalyzer extends AbstractSessionAnalyzer {

	protected final static Log staticInitLog = LogFactory.getLog(SessionAnalyzer.class.getName() + ".staticInitlog"); 
	protected final static Log analyzeSessionLog = LogFactory.getLog(SessionAnalyzer.class.getName() + ".analyzeSession"); 

	DryRunAnalyzer analyzer = new DryRunAnalyzer();
	
	static {
		LogHelper.info(staticInitLog, "SessionAnalyzer ("+statusString()+")");
	}

	@Override
	public void analyzeSession(HttpSession session) {
		int count = 0;
		Map<String,String> items = new HashMap<String, String>();
		
		for (@SuppressWarnings("unchecked")Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
			count++;
			String name = e.nextElement();
			Object value = session.getAttribute(name);
			items.put(name, value.getClass().getName());
			
		}
		
		LogHelper.info(analyzeSessionLog, "Session contains "+count+" items: '"+items+"'");
		
		if (isDryRunEnabled()) {
			analyzer.analyzeSession(session);
		}
		
		
	}

	private static boolean isDryRunEnabled() {
		String performDryRun = System.getProperty("capsule.session.perform.dry.run","true");
		return Boolean.valueOf(performDryRun);
	}


}
