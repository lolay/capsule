package com.eharmony.capsule.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

/**
 * Holds the full response in a buffer, making it possible to add cookies 
 * before the response is committed.
 * 
 * <p>I adapted this code from O'Reilly's GZIP filter code.</p>
 *
 * @author <a href="mailto:JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class FilterServletOutputStream extends ServletOutputStream {

	private final static Log closeLog = LogFactory.getLog(FilterServletOutputStream.class + ".close");
	private final static Log releaseBufferLog = LogFactory.getLog(FilterServletOutputStream.class + ".releaseBuffer");
	private final static Log flushLog = LogFactory.getLog(FilterServletOutputStream.class + ".flush");

	protected ByteArrayOutputStream baos = null;
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;

	public FilterServletOutputStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		baos = new ByteArrayOutputStream();
	}

	/**
	 * I don't want the stream to be closed until I get a chance to add the 
	 * session cookies to the response, so I have overidden this method and
	 * blocked the close. The actual close takes place in releaseBuffer().
	 */
	@Override
	public void close() throws IOException {
		LogHelper.debug(closeLog, "Enter close() but no action will be taken. Call releaseBuffer() instead.");
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
	}
	
	/**
	 * This is where the stream is actually closed. The BufferedResponseWrapper 
	 * calls this method after the cookies have been added to the response. This
	 * ensures that the response is not committed until after the cookies are 
	 * done.
	 * @throws IOException
	 */
	public void releaseBuffer() throws IOException {
		LogHelper.debug(releaseBufferLog, "Enter releaseBuffer()");
		byte[] bytes = baos.toByteArray();
		output.write(bytes);
		LogHelper.debug(releaseBufferLog, "{0} bytes written to output", bytes.length);
		output.flush();
		output.close();
		closed = true;
	}

	@Override
	public void flush() throws IOException {
		LogHelper.debug(flushLog, "Enter flush()");
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		baos.flush();
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		baos.write((byte)b);
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		baos.write(b, off, len);
	}

	public boolean closed() {
		return (this.closed);
	}

	public void reset() {
		//noop
	}
	
}