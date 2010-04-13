package com.eharmony.capsule.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.CatalogBase;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.capsule.chain.Base64Command;
import com.eharmony.capsule.chain.CookieCommand;
import com.eharmony.capsule.chain.CapsuleConfig;
import com.eharmony.capsule.chain.CryptoTranscoderCommand;
import com.eharmony.capsule.chain.HeaderCommand;
import com.eharmony.capsule.chain.JavaSerializerCommand;
import com.eharmony.capsule.chain.Mode;
import com.eharmony.capsule.chain.NoopCommand;
import com.eharmony.capsule.chain.SessionAttributeExtractor;
import com.eharmony.capsule.util.StopWatch;
import com.eharmony.logging.LogHelper;

/**
 * Serializes all Session attributes to one or more cookies and adds the cookies
 * to the response. If the cookies are present in the incoming request, this 
 * class converts the serialized cookie data back to Java objects and sets them
 * in the Session.
 *
 * @author <a href="mailto:JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class SessionCookieFilter implements Filter {
	
	private final static Log doFilterLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".doFilter");
	private final static Log processInLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".processIn");
	private final static Log processOutLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".processOut");
	private final static Log initLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".init");
	private final static Log buildChainsLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".buildChains");
	private final static Log buildChainsFromStringLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".buildChainsFromString");
	private final static Log performanceLog = LogFactory.getLog(SessionCookieFilter.class.getName() + ".performance");
	
	private Catalog catalog;
	private Chain inputChain;
	private Chain outputChain;
	private CapsuleConfig config = null;

	public void destroy() {
		inputChain = null;
		outputChain = null;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
			
		// check if filter chain is null
		if (chain == null) {
			// log the error and return
			LogHelper.error(doFilterLog, "FilterChain is null.");
			return;
		}
		
		if (config.isFilterEnabled()) {
			Log perfLog = performanceLog;
			StopWatch stopWatch = null;

			try {
				if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
					HttpServletRequest httpRequest = (HttpServletRequest) request;
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					LogHelper.debug(doFilterLog, "Filtering request [requestURI={0}]", httpRequest.getRequestURI());

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch = new StopWatch("doFilter");
						stopWatch.start("processIn");
					}

					Context context = processIn(httpRequest, httpResponse);

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch.stop();
					}

					LogHelper.debug(doFilterLog, "Before wrappedResponse [isCommitted={0}]", httpResponse.isCommitted());

					BufferedResponseWrapper wrappedResponse = new BufferedResponseWrapper(httpResponse);

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch.start("webapp");
					}

					chain.doFilter(request, wrappedResponse);

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch.stop();
					}
					LogHelper.debug(doFilterLog, "After chain.doFilter [isCommitted={0}]", wrappedResponse.isCommitted());

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch.start("processOut");
					}

					processOut(httpRequest, wrappedResponse, context);
					wrappedResponse.finishResponse();

					// Performance Logging
					if (perfLog.isDebugEnabled()) {
						stopWatch.stop();
						perfLog.debug(stopWatch.toString());
					}

					LogHelper.debug(doFilterLog, "after finishResponse [isCommitted={0}]", response.isCommitted());
				}
				else {
					chain.doFilter(request, response);
				}
			}
			catch (Exception e) {
				LogHelper.error(doFilterLog, "Exception in doFilter()", e);
			}
		}
		else {
			LogHelper.debug(doFilterLog, "Filter is disabled.");
			chain.doFilter(request, response);
			return;
		}
			
	}
	
	/**
	 * Convert the cookies in the request to Session attributes.
	 */
	@SuppressWarnings("unchecked")
	private Context processIn(HttpServletRequest request, HttpServletResponse response) {
		Context context = new ContextBase();
		try {
			context.put("mode", Mode.IN);
			context.put("request", request);
			context.put("uri", request.getRequestURI());
			inputChain.execute(context);
			return context;
		}
		catch (Exception e) {
			LogHelper.error(processInLog, "Error in processIn [requestURI={0}]", e, request.getRequestURI());
		}
		return context;
	}
	
	/**
	 * Convert Session attributes to cookies.
	 */
	@SuppressWarnings("unchecked")
	private void processOut(HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		try {
			if (context == null) {
				context = new ContextBase();
			}
			context.remove("bytes");
			context.put("mode", Mode.OUT);
			context.put("request", request);
			context.put("uri", request.getRequestURI());
			context.put("response", response);
			outputChain.execute(context);
		}
		catch (Exception e) {
			LogHelper.error(processOutLog, "Error in processOut [requestURI={0}, exception={1}]", request.getRequestURI(), e.toString());
			throw e;
		}
	}
	
	/**
	 * There are three layers of configuration for the Filter: default (no-config),
	 * init-params in the web.xml, and system properties. If you elect to override
	 * the default configuration, you may set the following properties as init-params
	 * in the web.xml or system properties (or a combination of both). System 
	 * properties will take precedence over init-params. Any properties not set will
	 * be defaulted.
	 * 
	 * <table>
	 * <thead>
	 * <tr><th>Property</th>
	 * <th>Description</th></tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>capsule.response.chain</td>
	 * <td>A comma-separated list of commands to execute in sequence to process
	 * the response. The commands will be reverse automatically to handle the
	 * incoming request. Please consult the Foundation team or see the source 
	 * code for this class for valid names. This feature is not meant for 
	 * routine use.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.cookieSize</td>
	 * <td>This should be a integer value to set the number of bytes allowed
	 * in each cookie. The default is 4000 bytes.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.cookieDomain</td>
	 * <td>Sets the domain property of the session cookies that will 
	 * be created. Defaults to the domain of the server the application is 
	 * running on.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.cookiePath</td>
	 * <td>Sets the path property of the session cookies that will 
	 * be created. Defaults to /.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.cookieMaxAge</td>
	 * <td>Sets the maxAge property of the session cookies that will 
	 * be created. This should be an integer value representing the number of 
	 * seconds the cookie should remain valid. Defaults to -1 seconds (cookie
	 * lives only as long as the browser is open).</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.warningThreshold</td>
	 * <td>The number of session data bytes at which the Filter logs a warning 
	 * message. Defaults to 10K.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.errorThreshold</td>
	 * <td>The number of session data bytes at which the Filter logs an error
	 * message and aborts processing of session cookies. Defaults to 20K.</td>
	 * </tr>
	 * <tr>
	 * <td>capsule.enabled</td>
	 * <td>The Filter is disabled by default. You can enable it by setting
	 * this as a web.xml init param or (more likely) as a system property.
	 * Any other value besides true will be ignored, and the filter will
	 * be disabled.</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		Log log = initLog;
		config = new CapsuleConfig();

		// First get config from web.xml
		config.setSize(filterConfig.getInitParameter("capsule.cookieSize"));
		config.setDomain(filterConfig.getInitParameter("capsule.cookieDomain"));
		config.setPath(filterConfig.getInitParameter("capsule.cookiePath"));
		config.setMaxAge(filterConfig.getInitParameter("capsule.cookieMaxAge"));
		config.setWarningThreshold(filterConfig.getInitParameter("capsule.warningThreshold"));
		config.setErrorThreshold(filterConfig.getInitParameter("capsule.errorThreshold"));
		config.setErrorThreshold(filterConfig.getInitParameter("capsule.errorThreshold"));
		config.setFilterEnabled(filterConfig.getInitParameter("capsule.enabled"));
		
		// Then override with System properties
		config.setSize(System.getProperty("capsule.cookieSize"));
		config.setDomain(System.getProperty("capsule.cookieDomain"));
		config.setPath(System.getProperty("capsule.cookiePath"));
		config.setMaxAge(System.getProperty("capsule.cookieMaxAge"));
		config.setWarningThreshold(System.getProperty("capsule.warningThreshold"));
		config.setErrorThreshold(System.getProperty("capsule.errorThreshold"));
		config.setFilterEnabled(System.getProperty("capsule.enabled"));
		
		if (config.isFilterEnabled()) {
			config.setProcessingChain(filterConfig.getInitParameter("capsule.response.chain"));
			config.setProcessingChain(System.getProperty("capsule.response.chain"));
		}
		else {
			config.setProcessingChain("noop");
		}
		
		LogHelper.info(log, "init: {0}", config);

		catalog = new CatalogBase();
		// These are the possible values that may be present in the capsule.response.chain property
		catalog.addCommand("session", new SessionAttributeExtractor(config));
		catalog.addCommand("cookie", new CookieCommand(config));
		catalog.addCommand("base64", new Base64Command(config));
		catalog.addCommand("crypto", new CryptoTranscoderCommand(config));
		catalog.addCommand("header", new HeaderCommand(config));
		catalog.addCommand("javaser", new JavaSerializerCommand(config));
		catalog.addCommand("noop", new NoopCommand(config));
		
		buildChains();
	}
	
	protected void buildChains() throws ServletException {
		try {
			if (config.getProcessingChain() != null) {
				buildChainsFromString();
			}
			else {
				inputChain = new ChainBase();
				inputChain.addCommand(catalog.getCommand("cookie"));
				inputChain.addCommand(catalog.getCommand("base64"));
				inputChain.addCommand(catalog.getCommand("header"));			
				inputChain.addCommand(catalog.getCommand("crypto"));
				inputChain.addCommand(catalog.getCommand("javaser"));
				inputChain.addCommand(catalog.getCommand("session"));

				outputChain = new ChainBase();
				outputChain.addCommand(catalog.getCommand("session"));
				outputChain.addCommand(catalog.getCommand("javaser"));
				outputChain.addCommand(catalog.getCommand("crypto"));
				outputChain.addCommand(catalog.getCommand("header"));			
				outputChain.addCommand(catalog.getCommand("base64"));
				outputChain.addCommand(catalog.getCommand("cookie"));
			}
		}
		catch (Exception e) {
			String message = LogHelper.errorReturnFormatted(buildChainsLog, "Unable to build chains. Filter will be disabled. [config={0}]", e, config);
			inputChain = new ChainBase();
			outputChain = new ChainBase();
			inputChain.addCommand(catalog.getCommand("noop"));
			outputChain.addCommand(catalog.getCommand("noop"));
		}
	}
	
	protected void buildChainsFromString() {
		LogHelper.info(buildChainsFromStringLog, "buildChainsFromString [{0}]", config.getProcessingChain());
		String[] commands = config.getProcessingChain().split(",");
		List<String> commandList = Arrays.asList(commands);
		outputChain = new ChainBase();
		for (String name : commandList) {
			outputChain.addCommand(catalog.getCommand(name));
		}

		Collections.reverse(commandList);
		inputChain = new ChainBase();
		for (String name : commandList) {
			inputChain.addCommand(catalog.getCommand(name));
		}
	}
	
}
