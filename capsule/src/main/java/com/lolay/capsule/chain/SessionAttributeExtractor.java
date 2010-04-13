package com.eharmony.capsule.chain;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.capsule.NamedAttribute;
import com.eharmony.capsule.util.Assert;
import com.eharmony.logging.LogHelper;

/**
 * A Command responsible for extracting attributes out of the 
 * HttpSession and saving them as POJOs. This eliminates the
 * need for subsequent Commands that process these attributes
 * to have a dependency on the Servlet API.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class SessionAttributeExtractor implements Command {
	
	private final static Log executeLog = LogFactory.getLog(SessionAttributeExtractor.class.getName() + ".execute");
	private final static Log setAttributesInSessionLog = LogFactory.getLog(SessionAttributeExtractor.class.getName() + ".setAttributesInSession");
	private final static Log getSessionLog = LogFactory.getLog(SessionAttributeExtractor.class.getName() + ".getSession");
	private final static Log extractLog = LogFactory.getLog(SessionAttributeExtractor.class.getName() + ".extractAttributesFromSession");
	
	public SessionAttributeExtractor() {
		super();
	}
	
	public SessionAttributeExtractor(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context context) {
		boolean retval = Chain.CONTINUE_PROCESSING;
		Mode mode = (Mode) context.get("mode");
		try {
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					retval = setAttributesInSession(context);
					break;
				
				case OUT:
					retval = extractAttributesFromSession(context);
					break;
				
				default:
					LogHelper.warn(executeLog, "Unexpected mode: {0}", mode);
					break;
			}
			return retval;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to extract session attributes [mode={0}, uri={1}]", e, mode, context.get("uri"));
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	/**
	 * Sets an attribute in the session only if it does not already exist.
	 * 
	 * <p>After discussing the issue with Joshua, we decided to overwrite
	 * session attributes if they already exist. The alternative is to treat
	 * existing sessions as the system of record. But we'll overwrite 
	 * attributes for now.</p>
	 * 
	 * @param context
	 * @return A boolean value indicating whether processing should continue.
	 */
	@SuppressWarnings("unchecked")
	protected boolean setAttributesInSession(Context context) {
		Log log = setAttributesInSessionLog;
		Collection<NamedAttribute> attributes = (Collection<NamedAttribute>) context.get("attributes");
		if (attributes != null) {
			HttpSession session = getSession(context, true);
			if (session == null) {
				return Chain.PROCESSING_COMPLETE;
			}
			for (NamedAttribute attribute : attributes) {
				String name = attribute.getName();
				if (session.getAttribute(name) == null) {
					session.setAttribute(name, attribute.getValue());
					if (log.isDebugEnabled()) {
						LogHelper.debug(log, "Attribute set in Session [name={0}, type={1}]", name, attribute.getValue().getClass().getName());
					}
				}
			}
		}
		else {
			LogHelper.debug(log, "no cookie attributes to set in session");
		}
		return Chain.CONTINUE_PROCESSING;
	}
	
	
	@SuppressWarnings("unchecked")
	protected boolean extractAttributesFromSession(Context context) {
		Log log = extractLog;
		HttpSession session = getSession(context, false);
		if (session == null) {
			return Chain.PROCESSING_COMPLETE;
		}
		Collection<NamedAttribute> attrs = new LinkedList<NamedAttribute>();
		Enumeration<String> names = session.getAttributeNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement();
			LogHelper.debug(log, "Extracting session attribute [{0}]", name);
			NamedAttribute attr = new NamedAttribute(name, session.getAttribute(name));
			attrs.add(attr);
		}
		context.put("attributes", attrs);
		if (attrs.size() > 0) {
			return Chain.CONTINUE_PROCESSING;
		}
		else {
			LogHelper.debug(log, "Session contains no attributes: PROCESSING COMPLETE");
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	protected HttpSession getSession(Context context, boolean create) {
		HttpServletRequest request = (HttpServletRequest) context.get("request");
		Assert.notNull(request, "request not found in context");
		HttpSession session = request.getSession(create);
		if (session == null) {
			LogHelper.info(getSessionLog, "No session associated with request [requestURI={0}]. Creating new session.", request.getRequestURI());
		}
		return session;
	}
	
}