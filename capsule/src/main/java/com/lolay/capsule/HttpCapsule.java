package com.eharmony.capsule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.capsule.chain.CapsuleConfig;
import com.eharmony.capsule.codec.Base64Transcoder;
import com.eharmony.capsule.codec.Transcoder;
import com.eharmony.capsule.util.Assert;
import com.eharmony.logging.LogHelper;
import com.eharmony.oreo.OreoCookie;

/**
 * 
 * Represents a Capsule that can be integrated into a HttpRequest as Session 
 * attributes and be integrated in to a HttpResponse as a series of cookies.
 * 
 * @author jtuberville
 *
 */
public class HttpCapsule {

	private static Pattern cookieNamePattern;
	private final static Log httpCapsuleLog = LogFactory.getLog(HttpCapsule.class.getName() + ".HttpCapsule(request, config)");
	private final static Log addToResponseLog = LogFactory.getLog(HttpCapsule.class.getName() + ".addToResponse");
	private final static Log removeObsoleteCookiesLog = LogFactory.getLog(HttpCapsule.class.getName() + ".removeObsoleteCookies");
	private final static Log applyPoliciesLog = LogFactory.getLog(HttpCapsule.class.getName() + ".applyPolicies");
	private final static Log createCookieLog = LogFactory.getLog(HttpCapsule.class.getName() + ".createCookie");

	/** The number of bytes allowed per cookie by default: 4KB. */
	public final static int DEFAULT_COOKIE_SIZE = 4*1024;

	/** The number of session data bytes at which we log a warning by default: 10KB. */
	public final static int DEFAULT_WARNING_THRESHOLD = 10*1024;

	/** The number of session data bytes at which we abort processing by default: 20KB. */
	public final static int DEFAULT_ERROR_THRESHOLD = 20*1024;

	/** Each cookie name will be constructed from this prefix and an index number. */
	private final static String COOKIE_NAME_PREFIX = "capsule";
	
	/** I am using String.format() to create the cookie name, so it was convenient to create a format expression here. */
	private final static String COOKIE_NAME_PREFIX_FRMT = COOKIE_NAME_PREFIX + "%1$s";
	
	private Capsule capsule;
	private int cookieSize = DEFAULT_COOKIE_SIZE;
	private int warningThreshold = DEFAULT_WARNING_THRESHOLD;
	private int errorThreshold = DEFAULT_ERROR_THRESHOLD;
	private String cookieDomain;
	private int cookieExpiry = -1;
	private String cookiePath;
	
	static {
		cookieNamePattern = Pattern.compile("capsule" + "\\d+");
	}
	
	public static class build {
		public HttpCapsule from(HttpSession session, CapsuleConfig config) {
			return new HttpCapsule(session, config);
		}
		
		public HttpCapsule from(HttpServletRequest request, CapsuleConfig config) throws Exception {
			return new HttpCapsule(request, config);
		}
	}
	
	private HttpCapsule(HttpSession session, CapsuleConfig config) {
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
		Collection<NamedAttribute> attributes = extractAttributes(session);
		capsule = new Capsule(attributes);

	}
	
	@SuppressWarnings("unchecked")
	private Collection<NamedAttribute> extractAttributes(HttpSession session) {
		Collection<NamedAttribute> attrs = new LinkedList<NamedAttribute>();
		Enumeration<String> names = session.getAttributeNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement();
			NamedAttribute attr = new NamedAttribute(name, session.getAttribute(name));
			attrs.add(attr);
		}
		
		return attrs;
	}

	private HttpCapsule(HttpServletRequest request, CapsuleConfig config) throws Exception {
		Assert.notNull(config, "CapsuleConfig is null!");
		if (config.getSize() != null) {
			this.cookieSize = config.getSize();
		}
		// override domain value obtained from CapsuleConfig (if any)
		// with the one returned by OreoCookie
		this.cookieDomain = config.getDomain();
		String validDomain = new OreoCookie(request).getValidDomain();
		if (validDomain != null) {
			this.cookieDomain = validDomain;
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

		Cookie[] cookies = request.getCookies();
		try {
			if (cookies != null) {
				Map<String, String> capsuleCookies = getCapsuleCookies(cookies);
				Assert.notNull(capsuleCookies, "capsuleCookies is null!");
				if (capsuleCookies.size() > 0) {
					ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
					for (Map.Entry<String, String> entry : capsuleCookies.entrySet()) {
						bytesOut.write(entry.getValue().getBytes());
					}
					request.setAttribute("requestCookies", capsuleCookies.keySet());
					byte[] cookieBytes = bytesOut.toByteArray();
					Transcoder<byte[], byte[]> base64 = new Base64Transcoder();
					byte[] decodedBytes = base64.decode(cookieBytes);
					capsule = new Capsule.SerialForm().parse(decodedBytes);
				}
			}
			else {
				LogHelper.debug(httpCapsuleLog, "No cookies to process!");
			}
		}
		catch (Exception e) {
			LogHelper.error(httpCapsuleLog, "Exception creating HttpCapsule from request {0}", e.toString());
			throw e;
		}
	}
	
	private Map<String, String> getCapsuleCookies(Cookie[] cookies) {
		Map<String, String> capsuleCookies = new TreeMap<String, String>();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				Matcher matcher = cookieNamePattern.matcher(cookieName);
				if (matcher.matches()) {
					capsuleCookies.put(cookieName, cookie.getValue());
				}
			}
		}
		return capsuleCookies;
	}
	
	public void addToSession(HttpServletRequest request) {
		if (capsule == null) {
			return;
		}
		HttpSession session = request.getSession(true);
		Collection<NamedAttribute> attributes = capsule.getAttributes();
		for (NamedAttribute attr : attributes) {
			session.setAttribute(attr.getName(), attr.getValue());
		}
	}
	
	public void addToResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// override domain value previously set using CapsuleConfig (if any)
		// with the one returned by OreoCookie
		String validDomain = new OreoCookie(request).getValidDomain();
		if (validDomain != null) {
			this.cookieDomain = validDomain;
		}
		try {
			byte[] sessionData = capsule.toBytes();
			Transcoder<byte[], byte[]> base64 = new Base64Transcoder();
			sessionData = base64.encode(sessionData);
			applyPolicies(sessionData);
			LogHelper.debug(addToResponseLog, "Creating cookies from session data of length={0}", sessionData.length);
			if (sessionData.length < 3000) {
				LogHelper.debug(addToResponseLog, new String(sessionData));
			}
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(sessionData);
			byte[] readBuffer = new byte[cookieSize];
			int bytesRead, cookieIndex = -1;
			while ((bytesRead = bytesIn.read(readBuffer)) > -1) {
				String name = String.format(COOKIE_NAME_PREFIX_FRMT, ++cookieIndex);
				String value = new String(readBuffer, 0, bytesRead);
				Cookie cookie = createCookie(name, value);
				response.addCookie(cookie);
			}
			cookieIndex++;
			removeObsoleteCookies(request, response, cookieIndex);
		}
		catch (Exception e) {
			LogHelper.error(addToResponseLog, e);
			throw e;
		}
	}
	
	/**
	 * The session size may shrink from one request to another, but the session
	 * cookies from a previous interaction may remain on the browser. We need
	 * to remove the obsolete cookies. This method attempts to do this by 
	 * comparing how many capsule cookies were received in the request with
	 * how many were created for the response. If the number of request cookies
	 * exceeds the number of response cookies, then create cookies for the 
	 * unused cookie names with an expiration of zero. This is supposed to 
	 * delete the cookie on the browser.
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	private void removeObsoleteCookies(HttpServletRequest request, HttpServletResponse response, int responseCookieIndex) {
		try {
			Set<String> requestCookies = (Set<String>) request.getAttribute("requestCookies");
			if (requestCookies == null) {
				LogHelper.debug(removeObsoleteCookiesLog, "requestCookies request attribute is null -- constructing from request");
			}
			Map<String, String> capsuleCookies = getCapsuleCookies(request.getCookies());
			requestCookies = capsuleCookies.keySet();
			if (requestCookies != null) {
				for (int i = responseCookieIndex; i < requestCookies.size(); i++) {
					String cookieName = String.format(COOKIE_NAME_PREFIX_FRMT, i);
					Cookie cookie = new Cookie(cookieName, "");
					if (getCookieDomain() != null) {
						cookie.setDomain(getCookieDomain());
					}
					cookie.setMaxAge(0);
					if (getCookiePath() != null) {
						cookie.setPath(getCookiePath());
					}
					response.addCookie(cookie);
					LogHelper.debug(removeObsoleteCookiesLog, "Deleting cookie {0}", cookieName);
				}
			}
			else {
				LogHelper.debug(removeObsoleteCookiesLog, "requestCookies not found in context");
			}
		}
		catch (Exception e) {
			// I don't expect exceptions in this method, and I don't want
			// processing to change if there is an exception, so I am just
			// logging the exception.
			LogHelper.error(removeObsoleteCookiesLog, e);
		}
	}
	
	/**
	 * We want to monitor the session size. If it gets too big, saving it to 
	 * cookies will become unwieldy if not impossible.
	 * 
	 * TODO We might want to JMX-enable the session size.
	 * 
	 * @param sessionData
	 */
	protected void applyPolicies(byte[] sessionData) {
		if (sessionData.length > errorThreshold) {
			throw new RuntimeException(String.format("Session data exceeds max allowable size: [max=%1$s, actual=%2$s]", errorThreshold, sessionData.length));
		}
		else if (sessionData.length > warningThreshold) {
			LogHelper.warn(applyPoliciesLog, "Session data exceeds ideal size: [threshold={0}, actual={1}]", warningThreshold, sessionData.length);
		}
	}

	private Cookie createCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		if (getCookieDomain() != null) {
			cookie.setDomain(getCookieDomain());
		}
		cookie.setMaxAge(getCookieExpiry());
		if (getCookiePath() != null) {
			cookie.setPath(getCookiePath());
		}
		LogHelper.debug(createCookieLog, "Cookie created: [name={0}, value.length={1}, domain={2}, maxAge={3}, path={4}]", 
					cookie.getName(), 
					value.length(), 
					cookie.getDomain(),
					cookie.getMaxAge(),
					cookie.getPath());
		return cookie;
	}
	
	public int getCookieSize() {
		return cookieSize;
	}

	public void setCookieSize(int cookieSize) {
		this.cookieSize = cookieSize;
	}

	public String getCookieDomain() {
		return cookieDomain;
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
