package com.eharmony.capsule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

/**
 * A servlet designed to work closely with test.jsp. It handles form params from
 * the jsp. It loads the session with dummy data for test. And it nicely creates 
 * the request attributes expected by test.jsp.
 *
 */
public class TestServlet extends HttpServlet {
	
	private static final long   serialVersionUID           = 47L;
	private static final String LOAD_PREFIX                = "LOAD__";
	private static final String LOAD_ATTRIBUTE_NAME_FORMAT = LOAD_PREFIX + "%1$d";
	private static final int    DEFAULT_LOAD               = 512;
	private static final String NO_SESSION_EXISTS          = "No session exists";

	private final static Log doGetLog = LogFactory.getLog(TestServlet.class.getName() + ".doGet");
	private final static Log doPostLog = LogFactory.getLog(TestServlet.class.getName() + ".doPost");
	private final static Log doForwardLog = LogFactory.getLog(TestServlet.class.getName() + ".doForward");
	private final static Log dumpSessionLog = LogFactory.getLog(TestServlet.class.getName() + ".dumpSession");
	private static final Log newSessionLog = LogFactory.getLog(TestServlet.class.getName() + "processNewSessionData");
	private static final Log existingSessionLog = LogFactory.getLog(TestServlet.class.getName() + "processExistingSessionData");
	private static final Log createSessionDataLog = LogFactory.getLog(TestServlet.class.getName() + "createSessionData");
	private static final Log clearLog = LogFactory.getLog(TestServlet.class.getName() + "clearLoadAttributes");
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log log = doGetLog;
		LogHelper.debug(log, "**** ENTER ****");
		processExistingSessionData(request, response);
		processCookies(request);
		processNewSessionData(request, response);
		LogHelper.debug(log, "**** EXIT ****");
		doForward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log log = doPostLog;
		LogHelper.debug(log, "**** ENTER ****");
		processExistingSessionData(request, response);
		processCookies(request);
		createSessionData(request, response);
		processNewSessionData(request, response);
		LogHelper.debug(log, "**** EXIT ****");
		doForward(request, response);
	}
	
	private void doForward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log log = doForwardLog;
		LogHelper.debug(log, "Dispatching to JSP [isCommitted={0}]", response.isCommitted());
		request.getRequestDispatcher("/jsp/test.jsp").forward(request, response);
		LogHelper.debug(log, "Exit service [isCommitted={0}]", response.isCommitted());
	}
	
	/* Gets all the cookies from the request, truncates long cookie values and
	   puts the data in a List for easy display by the JSP. */
	private void processCookies(HttpServletRequest request) {
		List<NamedAttribute> list = new ArrayList<NamedAttribute>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				list.add(new NamedAttribute(
					cookie.getName(), 
					String.format("value=%4$s; domain=[%1$s], path=[%2$s], maxAge=[%3$s]", cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), StringUtils.left(cookie.getValue(), 25)))
				);
			}
		}
		request.setAttribute("CookieList", list);
	}
	
	/* Extract existing session data and set as a request attribute. This is
	   to record session data before any new values are set. */
	private void processExistingSessionData(HttpServletRequest request, HttpServletResponse response) {
		Log log = existingSessionLog;
		HttpSession session = request.getSession(false);
		List<NamedAttribute> existingSessionData;
		if (session != null) {
			existingSessionData = dumpSession(session);
			request.setAttribute("ExistingSessionData", existingSessionData);
		}
		else {
			LogHelper.debug(log, "No session exists!");
			existingSessionData = new ArrayList<NamedAttribute>();
			existingSessionData.add(new NamedAttribute("message", NO_SESSION_EXISTS));
			request.setAttribute("ExistingSessionData", existingSessionData);
		}
	}
	
	/* Set session attribute from HTML form. Create special load attributes. */
	private void createSessionData(HttpServletRequest request, HttpServletResponse response) {
		Log log = createSessionDataLog;

		HttpSession session;
		
		String invalidateSession = request.getParameter("InvalidateSession");
		LogHelper.debug(log, "InvalidateSession request parameter = %1&s", invalidateSession);
		
		if (Boolean.valueOf(invalidateSession)) {
			// Invalidate previous session
			session  = request.getSession(false);
			if (session != null) {
				session.invalidate();
				LogHelper.debug(log, "HttpSession invalidated");
			}
		}

		// Create a new session and populate with data
		session = request.getSession(true);
		
		// Handle params from HTML form
		String attributeName = request.getParameter("SessionAttributeName");
		String attributeValue = request.getParameter("SessionAttributeValue");
		if (!StringUtils.isBlank(attributeName) && !StringUtils.isBlank(attributeValue)) {
			session.setAttribute(attributeName, attributeValue);
		}
		
		clearLoadAttributes(session);

		// Load session with dummy data to simulate large sessions
		int sessionSize;
		String ss = request.getParameter("SessionSize");
		if (StringUtils.isBlank(ss)) {
			LogHelper.debug(log, "SessionSize request parameter is null. Setting sessionSize to default value [{0}]", DEFAULT_LOAD);
			sessionSize = DEFAULT_LOAD;
		}
		else {
			try {
				sessionSize = Integer.valueOf(ss);
				if (sessionSize > 100*1024) {
					LogHelper.warn(log, "SessionSize parameter is too big. Reducing it to 1024.");
					sessionSize = DEFAULT_LOAD;
				}
			}
			catch (java.lang.NumberFormatException e) {
				LogHelper.warn(log, "Could not parse SessionSize. Setting to default [{0}]", DEFAULT_LOAD);
				sessionSize = DEFAULT_LOAD;
			}
		}
		LogHelper.debug(log, "sessionSize set to {0}", sessionSize);
		int numberOfLoadAttributes = sessionSize / DEFAULT_LOAD;
		LogHelper.debug(log, "numberOfLoadAttributes={0}", numberOfLoadAttributes);
		for (int i = 0; i < numberOfLoadAttributes; i++) {
			String name = String.format(LOAD_ATTRIBUTE_NAME_FORMAT, i);
			String load = RandomStringUtils.random(DEFAULT_LOAD);
			session.setAttribute(name, load);
			LogHelper.debug(log, "Set Session load attribute: {0}", name);
		}

	}
	
	/* Dump session data on the way out. */
	private void processNewSessionData(HttpServletRequest request, HttpServletResponse response) {
		Log log = newSessionLog;
		List<NamedAttribute> newSessionData;
		HttpSession session = request.getSession(false);
		if (session == null) {
			newSessionData = new ArrayList<NamedAttribute>();
			newSessionData.add(new NamedAttribute("message", NO_SESSION_EXISTS));
		}
		else {
			newSessionData = dumpSession(session);
		}
		request.setAttribute("NewSessionData", newSessionData);
	}
	
	/* Removes all load attributes and values from the session. */
	private void clearLoadAttributes(HttpSession session) {
		Log log = clearLog;
		List<String> list = new ArrayList<String>();
		for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			if (name.startsWith(LOAD_PREFIX)) {
				list.add(name);
			}
		}
		for (String name : list) {
			session.removeAttribute(name);
			LogHelper.debug(clearLog, "Attribute [{0}] removed from session", name);
		}
	}
	
	/* A convenience method for dumping session attributes and values to a list.
	   Handles special load attributes. */
	@SuppressWarnings("unchecked")
	private List<NamedAttribute> dumpSession(HttpSession session) {
		Log log = dumpSessionLog;
		List<NamedAttribute> list = new ArrayList<NamedAttribute>();
		for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			LogHelper.debug(log, "Processing session attribute [{0}]", name);
			String value;
			if (name.startsWith(LOAD_PREFIX)) {
				value = String.format("size: %1$d KB", DEFAULT_LOAD);
			}
			else {
				value = String.format("%1$s", session.getAttribute(name));
			}
			list.add(new NamedAttribute(name, value));
		}
		LogHelper.debug(log, "Returning [{0}] session entries", list.size());
		return list;
	}

}
