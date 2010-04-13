package com.lolay.capsule.chain;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.capsule.codec.Base64Transcoder;
import com.lolay.capsule.codec.Transcoder;
import com.lolay.capsule.util.Assert;
import com.lolay.logging.LogHelper;

/**
 * Applies Base64 encoding to the bytes in the context.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class Base64Command implements Command {

	private final static Log executeLog = LogFactory.getLog(Base64Command.class.getName() + ".execute");
	private final static Log encodeLog = LogFactory.getLog(Base64Command.class.getName() + ".encode");
	private final static Log decodeLog = LogFactory.getLog(Base64Command.class.getName() + ".decode");

	private Transcoder<byte[], byte[]> transcoder = new Base64Transcoder();

	public Base64Command() {
		super();
	}
	public Base64Command(CapsuleConfig config) {
		super();
	}
	public boolean execute(Context context) throws Exception {
		Mode mode = (Mode) context.get("mode");
		try {
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					decode(context);
					break;
					
				case OUT:
					encode(context);
					break;
					
				default:
					break;
			}
			return Chain.CONTINUE_PROCESSING;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to apply Base64 encoding [mode={0}, uri={1}]", e, mode, context.get("uri"));
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void encode(Context context) {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		byte[] encodedBytes = transcoder.encode(bytes);
		LogHelper.debug(encodeLog, "Base64 encoded bytes length [{0}]", encodedBytes.length);
		context.put("bytes", encodedBytes);
	}
	
	@SuppressWarnings("unchecked")
	protected void decode(Context context) {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		LogHelper.debug(decodeLog, "Base64 decoding bytes length [{0}]", bytes.length);
		byte[] decodedBytes = transcoder.decode(bytes);
		context.put("bytes", decodedBytes);
	}

}
