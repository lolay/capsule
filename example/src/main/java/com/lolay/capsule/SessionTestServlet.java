package com.eharmony.capsule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

public class SessionTestServlet extends HttpServlet {
	
	private static final long serialVersionUID = 47L;

	private final static Log log = LogFactory.getLog(SessionTestServlet.class.getName());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Enter service");
		HttpSession session = request.getSession(true);
		session.setAttribute("attribDate", new java.util.Date());
		Map<String, String> map = new HashMap<String, String>();
		map.put("key1", "This is key1");
		map.put("key2", "This is key2");
		map.put("key3", "This is key3");
		session.setAttribute("attribMap", map);
		List existingSessionData = new ArrayList<NamedAttribute>();
		existingSessionData.add(new NamedAttribute("attribDate", session.getAttribute("attribDate")));
		existingSessionData.add(new NamedAttribute("attribMap", session.getAttribute("attribMap")));
		existingSessionData.add(new NamedAttribute("sessionId", session.getId()));
		request.setAttribute("existingSessionData", existingSessionData);
		request.getRequestDispatcher("/jsp/sessionTest.jsp").forward(request, response);
	}
	
}
