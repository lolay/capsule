package com.eharmony.capsule.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

/**
 * Wraps the servlet response to prevent the response from being committed 
 * before we have a chance to add the session cookies.
 *
 * @author <a href="mailto:JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class BufferedResponseWrapper extends HttpServletResponseWrapper {
	
	private final static Log finishResponseLog = LogFactory.getLog(BufferedResponseWrapper.class.getName() + ".finishResponse");
	private final static Log flushBufferLog = LogFactory.getLog(BufferedResponseWrapper.class.getName() + ".flushBuffer");
	private final static Log setContentLengthLog = LogFactory.getLog(BufferedResponseWrapper.class.getName() + ".setContentLength");
	private final static Log addCookieLog = LogFactory.getLog(BufferedResponseWrapper.class.getName() + ".addCookie");
	private final static Log getBufferSizeLog = LogFactory.getLog(BufferedResponseWrapper.class.getName() + ".getBufferSize");

	protected HttpServletResponse origResponse = null;
	protected ServletOutputStream stream = null;
	protected PrintWriter writer = null;
	protected FilterServletOutputStream filterStream = null;

	public BufferedResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	private ServletOutputStream createOutputStream() throws IOException {
		filterStream = new FilterServletOutputStream(origResponse);
		return filterStream;
	}

	public void finishResponse() {
		LogHelper.debug(finishResponseLog, "Enter finishResponse() [isCommitted={0}]", origResponse.isCommitted());
		try {
			if (filterStream != null) {
				filterStream.releaseBuffer();
			}
		}
		catch (IOException e) {
			LogHelper.error(finishResponseLog, "Exception in finishResponse", e);
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		LogHelper.debug(flushBufferLog, "flushBuffer()");
		if (stream != null) {
			stream.flush();
		}
		if (writer != null) {
			writer.flush();
		}
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called!");
		}

		if (stream == null) {
			stream = createOutputStream();
		}
		return stream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return (writer);
		}

		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called!");
		}

		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
		return writer;
	}

	@Override
	public void setContentLength(int length) {
		LogHelper.debug(setContentLengthLog, "setContentLength [length={0}]", length);
		origResponse.setContentLength(length);
	}
	
	@Override
	public void addCookie(Cookie cookie) {
		LogHelper.debug(addCookieLog, "addCookie [name={0}, isCommitted={1}]", cookie.getName(), origResponse.isCommitted());
		origResponse.addCookie(cookie);
	}
	
	@Override
	public boolean isCommitted() {
		return origResponse.isCommitted();
	}
	
	@Override
	public int getBufferSize() {
		int retval = origResponse.getBufferSize();
		LogHelper.debug(getBufferSizeLog, "getBufferSize() {0}", retval);
		return retval;
	}
	
}
