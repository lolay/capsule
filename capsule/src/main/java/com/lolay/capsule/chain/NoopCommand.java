package com.eharmony.capsule.chain;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.logging.LogHelper;

/**
 * Does nothing. Useful as a fallback Command.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class NoopCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(NoopCommand.class.getName() + ".execute");

	public NoopCommand() {
		super();
	}
	
	public NoopCommand(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context context) throws Exception {
		LogHelper.debug(executeLog, "Enter execute");
		return Chain.PROCESSING_COMPLETE;
	}

}
