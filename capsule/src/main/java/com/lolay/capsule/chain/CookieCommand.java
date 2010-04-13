package com.lolay.capsule.chain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.capsule.util.Assert;
import com.lolay.logging.LogHelper;
import com.lolay.oreo.OreoCookie;

/**
 * Responsible for handling the processing of cookies.
 * 
 * <p>We are going to log a warning if the session size is greater than <em>capsule.warningThreshold</em>.</p>
 * 
 * <p>We are going to log an error and refrain from setting cookies if the 
 * session size is greater than <em>capsule.errorThreshold</em>.</p>
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class CookieCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(CookieCommand.class.getName() + ".execute");
	private final static Log readCookiesLog = LogFactory.getLog(CookieCommand.class.getName() + ".readCookies");
	private final static Log removeStaleCookiesLog = LogFactory.getLog(CookieCommand.class.getName() + ".removeStaleCookies");
	private final static Log addCookiesLog = LogFactory.getLog(CookieCommand.class.getName() + ".addCookies");
	private final static Log setWarningThresholdLog = LogFactory.getLog(CookieCommand.class.getName() + ".setWarningThreshold");
	private final static Log setErrorThresholdLog = LogFactory.getLog(CookieCommand.class.getName() + ".setErrorThreshold");
	private final static Log applyPoliciesLog = LogFactory.getLog(CookieCommand.class.getName() + ".applyPolicies");
	private final static Log sessionAttribsLog = LogFactory.getLog(CookieCommand.class.getName() + ".applyPolicies.sessionAttributes");
	private final static Log capsuleDataLog = LogFactory.getLog(CookieCommand.class.getName() + ".applyPolicies.capsuleData");
	private final static Log getValidCapsuleCookiesLog = LogFactory.getLog(CookieCommand.class.getName() + ".getValidCapsuleCookies");
	
	/** The number of bytes allowed per cookie by default: 4000 bytes. */
	public final static int DEFAULT_COOKIE_SIZE = 4000;

	/** The number of session data bytes at which we log a warning by default: 10KB. */
	public final static int DEFAULT_WARNING_THRESHOLD = 10*1024;

	/** The number of session data bytes at which we abort processing by default: 20KB. */
	public final static int DEFAULT_ERROR_THRESHOLD = 20*1024;

	/** Each cookie name will be constructed from this prefix and an index number. */
	private final static String COOKIE_NAME_PREFIX = "capsule";
	
	/** I am using String.format() to create the cookie name, so it was convenient to create a format expression here. */
	private final static String COOKIE_NAME_PREFIX_FRMT = COOKIE_NAME_PREFIX + "%1$s";
	
	private int cookieSize;
	private int warningThreshold;
	private int errorThreshold;
	private String cookieDomain;
	private int cookieExpiry;
	private String cookiePath;
	private Pattern cookieNamePattern;
	
	public CookieCommand() {
		cookieSize        = DEFAULT_COOKIE_SIZE;
		warningThreshold  = DEFAULT_WARNING_THRESHOLD;
		errorThreshold    = DEFAULT_ERROR_THRESHOLD;
		cookieDomain      = null;
		cookieExpiry      = -1;
		cookiePath        = "/";
		cookieNamePattern = Pattern.compile(COOKIE_NAME_PREFIX + "(\\d+)");
	}
	
	public CookieCommand(CapsuleConfig config) {
		this();
		if (config.getSize() != null) {
			this.cookieSize = config.getSize();
		}
		if (config.getDomain() != null) {
			this.cookieDomain = config.getDomain();
		}
		if (config.getPath() != null) {
			this.cookiePath = config.getPath();
		}
		if (config.getMaxAge() != null) {
			this.cookieExpiry = config.getMaxAge();
		}
		if (config.getWarningThreshold() != null) {
			this.warningThreshold = config.getWarningThreshold();
		}
		if (config.getErrorThreshold() != null) {
			this.errorThreshold = config.getErrorThreshold();
		}
	}
	
	/**
	 * The number of bytes of serialized, encrypted and encoded session data 
	 * at which we will log a warning message.
	 * 
	 * @param size
	 */
	public void setWarningThreshold(int size) {
		LogHelper.info(setWarningThresholdLog, "Warning threshold set to {0}", size);
		this.warningThreshold = size;
	}
	
	public int getWarningThreshold() {
		return warningThreshold;
	}
	
	/**
	 * The number of bytes of serialized, encrypted and encoded session data 
	 * at which we will log an error message and abort processing. Aborting
	 * will not affect the user experience. It will just mean that the user's
	 * session may not survive a switch to another server.
	 * 
	 * @param size
	 */
	public void setErrorThreshold(int size) {
		LogHelper.info(setErrorThresholdLog, "Error threshold set to {0}", size);
		this.errorThreshold = size;
	}
	
	public int getErrorThreshold() {
		return errorThreshold;
	}

	public boolean execute(Context context) throws Exception {
		boolean status = Chain.CONTINUE_PROCESSING;
		Mode mode = (Mode) context.get("mode");
		try {
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					status = readCookies(context);
					break;
				
				case OUT:
					addCookies(context);
					break;
				
				default:
					LogHelper.warn(executeLog, "Unexpected mode: {0}", mode);
					break;
			}
			return status;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to process cookies [mode={0}]", e, mode);
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	/**
	 * Gets all cookies from the HttpServletRequest, matches the cookie names
	 * against a regular expression, puts each cookie in a TreeMap keyed by
	 * name (which will sort the cookies by index number in case they arrive
	 * in this order in the request), and finally writes all the cookie data
	 * to a ByteArrayOutputStream, which constructs the byte array to put in 
	 * the context for further processing by other Commands in the Chain.
	 * 
	 * @param context
	 * @return A boolean value indicating whether processing should continue.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected boolean readCookies(Context context) throws IOException {
		Log log = readCookiesLog;
		boolean status = Chain.PROCESSING_COMPLETE;
		HttpServletRequest request = (HttpServletRequest) context.get("request");
		Assert.notNull(request, "request not found in context");
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			Map<CookieHeader, String> capsuleCookies = new TreeMap<CookieHeader, String>();
			LogHelper.debug(log, "Processing {0} cookies in request", cookies.length);
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				Matcher matcher = cookieNamePattern.matcher(cookieName);
				if (matcher.matches()) {
					Integer cookieIndex = Integer.parseInt(matcher.group(1));
					CookieHeader header = new CookieHeader(cookieName, cookieIndex);
					LogHelper.debug(log, "Adding cookie to capsuleCookies: {0}", header);
					capsuleCookies.put(header, cookie.getValue());
				}
			}
			Map<CookieHeader, String> cookiesToProcess = getValidCapsuleCookies(capsuleCookies);
			context.put("requestCookies", capsuleCookies.keySet());
			if (cookiesToProcess.size() > 0) {
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				for (Map.Entry<CookieHeader, String> entry : cookiesToProcess.entrySet()) {
					bytesOut.write(entry.getValue().getBytes());
				}
				context.put("bytes", bytesOut.toByteArray());
				status = Chain.CONTINUE_PROCESSING;
			}
			else {
				LogHelper.debug(log, "No capsule cookies in request [requestURI={0}]", request.getRequestURI());
			}
		}
		else {
			LogHelper.debug(log, "No cookies found in request [requestURI={0}]", request.getRequestURI());
		}
		return status;
	}
	
	/**
	 * This method makes sure that capsule cookies appear in order without any gaps.
	 * @param capsuleCookies
	 */
	private Map<CookieHeader, String> getValidCapsuleCookies(Map<CookieHeader, String> capsuleCookies) {
		Log log = getValidCapsuleCookiesLog;
		Map<CookieHeader, String> validCookies = new TreeMap<CookieHeader, String>();
		Integer index = 0;
		for (Map.Entry<CookieHeader, String> entry : capsuleCookies.entrySet()) {
			CookieHeader header = entry.getKey();
			if (!index.equals(header.getIndex())) {
				LogHelper.warn(log, "Ignoring out-of-sequence capsule cookie [{0}]", header);
			}
			else {
				LogHelper.debug(log, "Adding valid capsule cookie to validCookies [{0}]", header);
				validCookies.put(entry.getKey(), entry.getValue());
				index++;
			}
		}
		return validCookies;
	}
	
	/**
	 * Splits the serialized, encrypted and encoded session data into blocks
	 * of cookieSize length, creates a Cookie with each block of data, and
	 * adds the Cookie to the HttpServletResponse.
	 * 
	 * @param context
	 */
	protected void addCookies(Context context) throws IOException {
		Log log = addCookiesLog;
		byte[] sessionData = (byte[]) context.get("bytes");
		Assert.notNull(sessionData, "bytes not found in context");

		boolean proceed = applyPolicies(sessionData, context);
		if (!proceed) {
			/*
			Session data violates cookie policy. Delete old cookies from 
			browser, otherwise cookie data will be out of sync with Session. By
			passing an empty Set, removeStaleCookies will consider all cookies
			stale.
			*/
			Set<CookieHeader> emptySet = Collections.emptySet();
			removeStaleCookies(context, emptySet);
			
			return;
		}
	
		HttpServletResponse response = (HttpServletResponse) context.get("response");
		Assert.notNull(response, "response not found in context");
		
		HttpServletRequest request = (HttpServletRequest) context.get("request");
		Assert.notNull(request, "request not found in context");
		
		LogHelper.debug(log, "Creating cookies from session data [length={0}, cookieSize={1}]", sessionData.length, cookieSize);

		ByteArrayInputStream bytesIn = new ByteArrayInputStream(sessionData);
		byte[] readBuffer = new byte[cookieSize];
		int bytesRead, cookieIndex = -1;
		Set<CookieHeader> responseCookies = new HashSet<CookieHeader>();
		while ((bytesRead = bytesIn.read(readBuffer)) > -1) {
			String name = String.format(COOKIE_NAME_PREFIX_FRMT, ++cookieIndex);
			String value = new String(readBuffer, 0, bytesRead);
			Cookie cookie = createCookie(name, value, request);
			response.addCookie(cookie);
			CookieHeader header = new CookieHeader(cookie.getName(), cookieIndex);
			responseCookies.add(header);
			LogHelper.debug(log, "added cookie [name={0}, domain={1}, path={2}, header={3}]", cookie.getName(), cookie.getDomain(), cookie.getPath(), header);
		}
		removeStaleCookies(context, responseCookies);
	}
	
	/**
	 * The session size may shrink from one request to another, but the session
	 * cookies from a previous interaction may remain on the browser. We need
	 * to remove the stale cookies. This method attempts to do this by 
	 * comparing how many capsule cookies were received in the request with
	 * how many were created for the response. If the number of request cookies
	 * exceeds the number of response cookies, then create cookies for the 
	 * unused cookie names with an expiration of zero. This is supposed to 
	 * delete the cookie on the browser.
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void removeStaleCookies(Context context, Set<CookieHeader> responseCookies) {
		Log log = removeStaleCookiesLog;
		try {
			Set<CookieHeader> requestCookies = (Set<CookieHeader>) context.get("requestCookies");
			if (requestCookies != null) {
				HttpServletResponse response = (HttpServletResponse) context.get("response");
				HttpServletRequest request = (HttpServletRequest) context.get("request");
				for (CookieHeader header : requestCookies) {
					if (!responseCookies.contains(header)) {
						String cookieName = header.getName();
						Cookie cookie = createCookie(cookieName, "", request);
						cookie.setMaxAge(0);
						response.addCookie(cookie);
						LogHelper.debug(log, "Deleting cookie [name={0}, domain={1}, path={2}, maxAge={3}]", cookie.getName(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge());
					}
				}
			}
			else {
				LogHelper.debug(log, "requestCookies not found in context");
			}
		}
		catch (Exception e) {
			// I don't expect exceptions in this method, and I don't want
			// processing to change if there is an exception, so I am just
			// logging the exception.
			LogHelper.error(log, "Exception in removeStaleCookies [uri={0}]", e, context.get("uri"));
		}
	}
	
	private Cookie createCookie(String name, String value, HttpServletRequest request) {
		Cookie cookie = new Cookie(name, value);
		String domain = getCookieDomain(request);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setMaxAge(getCookieExpiry());
		if (getCookiePath() != null) {
			cookie.setPath(getCookiePath());
		}
		return cookie;
	}
	
	/**
	 * We want to monitor the session size. If it gets too big, saving it to 
	 * cookies will become unwieldy if not impossible.
	 * 
	 * TODO We might want to JMX-enable the session size.
	 * 
	 * @param sessionData
	 */
	protected boolean applyPolicies(byte[] sessionData, Context context) {
		Log log = applyPoliciesLog;
		boolean passes = true;
		if (sessionData.length > errorThreshold) {
			LogHelper.error(log, "Session data exceeds max allowable size: [max={0}, actual={1}, uri={2}]", errorThreshold, sessionData.length, context.get("uri"));
			passes = false;
			analyzeData(sessionData, context);
		}
		else if (sessionData.length > warningThreshold) {
			LogHelper.warn(log, "Session data exceeds ideal size: [threshold={0}, actual={1}, uri={2}]", warningThreshold, sessionData.length, context.get("uri"));
			analyzeData(sessionData, context);
		}
		return passes;
	}
	
	/**
	 * Log session and capsule data to help debug large sessions.
	 */
	private void analyzeData(byte[] sessionData, Context context) {
		if (sessionAttribsLog.isTraceEnabled()) {
			try {
				HttpServletRequest request = (HttpServletRequest) context.get("request");
				Assert.notNull(request, "request not found in context");
				HttpSession session = request.getSession(false);
				if (session != null) {
					int count = 0;
					Map<String,String> items = new HashMap<String, String>();
					for (@SuppressWarnings("unchecked")Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
						count++;
						String name = e.nextElement();
						Object value = session.getAttribute(name);
						items.put(name, value.getClass().getName());
					}
					LogHelper.trace(sessionAttribsLog, "Session contains {0} items: {1}", count, items);
				}
			}
			catch (Exception e) {
				LogHelper.warn(sessionAttribsLog, "Error creating trace log of Session data", e);
			}
		}
		
		if (capsuleDataLog.isTraceEnabled()) {
			try {
				LogHelper.trace(capsuleDataLog, new String(sessionData));
			}
			catch (Exception e) {
				LogHelper.warn(capsuleDataLog, "Could not log capsule data", e);
			}
		}

	}

	public int getCookieSize() {
		return cookieSize;
	}

	public void setCookieSize(int cookieSize) {
		this.cookieSize = cookieSize;
	}

	/**
	 * This method first uses the OreoCookie class from the oreo project to 
	 * handle the various eHarmony domains. If OreoCookie returns null, this
	 * method attempts to get the domain from the web.xml or system property
	 * configuration. Returns null if no domain is found.
	 *
	 * @return The cookie domain
	 */
	public String getCookieDomain(HttpServletRequest request) {
		String validDomain = new OreoCookie(request).getValidDomain();
		if (validDomain == null) {
			validDomain = cookieDomain;
		}
		return validDomain;
	}

	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}

	public int getCookieExpiry() {
		return cookieExpiry;
	}

	/**
	 * The number of seconds that the cookie will live. A value of -1 means
	 * the cookie will last only as long as the browser session.
	 * 
	 * @param cookieExpiry
	 */
	public void setCookieExpiry(int cookieExpiry) {
		this.cookieExpiry = cookieExpiry;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

}
