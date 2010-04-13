package com.lolay.capsule.chain;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.capsule.codec.CryptoTranscoder;
import com.lolay.capsule.util.Assert;
import com.lolay.logging.LogHelper;

/**
 * Encrypt the bytes attribute using the CryptoTranscoder.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class CryptoTranscoderCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(CryptoTranscoderCommand.class.getName() + ".execute");
	private final static Log decryptLog = LogFactory.getLog(CryptoTranscoderCommand.class.getName() + ".decrypt");
	private final static Log encryptLog = LogFactory.getLog(CryptoTranscoderCommand.class.getName() + ".encrypt");

	public CryptoTranscoderCommand() {
		super();
	}
	
	public CryptoTranscoderCommand(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context context) throws Exception {
		Mode mode = (Mode) context.get("mode");
		try {
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					decrypt(context);
					break;
					
				case OUT:
					encrypt(context);
					break;
					
				default:
					break;
			}
			return Chain.CONTINUE_PROCESSING;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to process encryption [mode={0}, uri={1}]", e, mode, context.get("uri"));
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void decrypt(Context context) {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		Short version = (Short) context.get("version");
		Assert.notNull(version, "version not found in context");
		CryptoTranscoder crypto = new CryptoTranscoder(version);
		LogHelper.debug(decryptLog, "decrypting data [version={0}]", version);
		byte[] decryptedBytes = crypto.decode(bytes);
		context.put("bytes", decryptedBytes);
	}
	
	@SuppressWarnings("unchecked")
	protected void encrypt(Context context) {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		CryptoTranscoder crypto = new CryptoTranscoder();
		byte[] encryptedBytes = crypto.encode(bytes);
		context.put("bytes", encryptedBytes);
		short version = crypto.getLatestVersion();
		context.put("version", Short.valueOf(version));
		LogHelper.debug(encryptLog, "data encrypted [version={0}]", version);
	}

}
