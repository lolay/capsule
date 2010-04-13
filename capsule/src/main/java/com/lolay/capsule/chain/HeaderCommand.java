package com.lolay.capsule.chain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.capsule.util.Assert;
import com.lolay.logging.LogHelper;

/**
 * Processes the header portion of the cookie. This includes verifying
 * the validity of the header data.
 * 
 * <p>The header consists of a common prefix (MAGIC) and the version
 * number of the strategy used to encrypt the cookie data.</p> 
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class HeaderCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(HeaderCommand.class.getName() + ".execute");
	
	private static final byte[] MAGIC = "CPSL".getBytes();
	
	public HeaderCommand() {
		super();
	}
	
	public HeaderCommand(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context context) throws Exception {
		Mode mode = (Mode) context.get("mode");
		try {
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					parseHeader(context);
					break;
				
				case OUT:
					constructHeader(context);
					break;
				
				default:
					LogHelper.warn(executeLog, "Unexpected mode: {0}", mode);
					break;
			}
			return Chain.CONTINUE_PROCESSING;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to process header [mode={0}, uri={1}]", e, mode, context.get("uri"));
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	/**
	 * Prepends a header (consisting of the MAGIC phrase and the version
	 * number of the session cookie process) to the session bytes.
	 * 
	 * @param context
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void constructHeader(Context context) throws IOException {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(baos);
		output.write(MAGIC);
		Short version = (Short) context.get("version");
		Assert.notNull(version, "version not found in context");
		output.writeShort(version);
		output.write(bytes);
		context.put("bytes", baos.toByteArray());
	}
	
	/**
	 * Parses the incoming request data. Verifies the MAGIC phrase and
	 * extracts the version into the context to make it available to
	 * other Commands.
	 * 
	 * @param context
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void parseHeader(Context context) throws IOException {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream input = new DataInputStream(bais);
		checkMagic(input);
		short streamVersion = parseVersion(input);
		context.put("version", Short.valueOf(streamVersion));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[512];
		for (int bytesRead = bais.read(buffer); bytesRead > 0; bytesRead = bais.read(buffer)) {
			out.write(buffer, 0, bytesRead);
		}
		context.put("bytes", out.toByteArray());
	}
	
	private void checkMagic(DataInputStream input) throws IOException {
		byte[] firstBytes = new byte[MAGIC.length];
		input.read(firstBytes, 0, MAGIC.length);
		if (!Arrays.equals(MAGIC,firstBytes)) {
			throw new IllegalArgumentException(
					"Byte stream is not valid");
		}
	}

	private short parseVersion(DataInputStream input) throws IOException {
		short streamVersion = input.readShort();
		return streamVersion;
	}

}
