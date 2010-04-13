package com.lolay.capsule.chain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.logging.LogHelper;

/**
 * Serialize the session data using Hessian.
 * 
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class HessianCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(HessianCommand.class.getName() + ".execute");

	public HessianCommand() {
		super();
	}
	
	public HessianCommand(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context arg0) throws Exception {
		LogHelper.warn(executeLog, "This class is not implemented yet!");
		return false;
	}

}
