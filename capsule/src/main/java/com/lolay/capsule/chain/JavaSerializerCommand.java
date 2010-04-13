package com.lolay.capsule.chain;

import java.util.Collection;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lolay.capsule.NamedAttribute;
import com.lolay.capsule.codec.JavaSerializer;
import com.lolay.capsule.codec.Serializer;
import com.lolay.capsule.util.Assert;
import com.lolay.logging.LogHelper;

/**
 * Takes care of converting from bytes to Java and vice versa using
 * Java serialization.
 *  
 * @author <a href="JonStefansson@eharmony.com">Jon Stefansson</a>
 */
public class JavaSerializerCommand implements Command {

	private final static Log executeLog = LogFactory.getLog(JavaSerializerCommand.class.getName() + ".execute");

	public JavaSerializerCommand() {
		super();
	}
	
	public JavaSerializerCommand(CapsuleConfig config) {
		super();
	}
	
	public boolean execute(Context context) throws Exception {
		try {
			Mode mode = (Mode) context.get("mode");
			Assert.notNull(mode, "mode not found in context");
			switch (mode) {
				case IN:
					fromBytes(context);
					break;
				
				case OUT:
					toBytes(context);
					break;
				
				default:
					LogHelper.warn(executeLog, "Unexpected mode: {0}", mode);
					break;
			}
			return Chain.CONTINUE_PROCESSING;
		}
		catch (Exception e) {
			LogHelper.error(executeLog, "Unable to serialize session attributes [uri={0}]", e, context.get("uri"));
			return Chain.PROCESSING_COMPLETE;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fromBytes(Context context) {
		byte[] bytes = (byte[]) context.get("bytes");
		Assert.notNull(bytes, "bytes not found in context");
		Serializer<Collection<NamedAttribute>> serializer = new JavaSerializer<Collection<NamedAttribute>>();
		Collection<NamedAttribute> attributes = serializer.decode(bytes);
		context.put("attributes", attributes);
	}
	
	@SuppressWarnings("unchecked")
	protected void toBytes(Context context) {
		Collection<NamedAttribute> attributes = (Collection<NamedAttribute>) context.get("attributes");
		Assert.notNull(attributes, "attributes not found in context");
		Serializer<Collection<NamedAttribute>> serializer = new JavaSerializer<Collection<NamedAttribute>>();
		byte[] serializedSession = serializer.encode(attributes);
		context.put("bytes", serializedSession);
	}

}
