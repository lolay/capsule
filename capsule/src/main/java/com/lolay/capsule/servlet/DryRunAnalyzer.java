package com.eharmony.capsule.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.capsule.NamedAttribute;
import com.eharmony.capsule.codec.Base64Transcoder;
import com.eharmony.capsule.codec.CryptoTranscoder;
import com.eharmony.capsule.codec.InstrumentedTranscoder;
import com.eharmony.capsule.codec.SerializationStrategy;
import com.eharmony.capsule.codec.Serializer;
import com.eharmony.logging.LogHelper;

/**
 * 
 * Caspule analyzer for Servlet HttpSessions that does nothing by default.  To enable analysis you must set
 * capsule.session.analysis.enabled=true in the System properties.  To exclude any session keys from the analysis
 * include them in a comma separtated list
 * 
 * -Dcapsule.session.excluded.classes=com.example.UnserializableClass1,com.example.UnserializableClass2
 * 
 * The serialization strategy defaults to Java but can be changed via
 * 
 * -Dcapsule.session.serialization.strategy
 * 
 * @author jtuberville
 *
 */
public class DryRunAnalyzer extends AbstractSessionAnalyzer implements ServletRequestListener {
	
	protected final static Log staticInitLog = LogFactory.getLog(DryRunAnalyzer.class.getName() + ".staticInitlog"); 
	protected final static Log analyzeSessionLog = LogFactory.getLog(DryRunAnalyzer.class.getName() + ".analyzeSession"); 
	protected final static Log extractSessionAttrsLog = LogFactory.getLog(DryRunAnalyzer.class.getName() + ".extractSessionAttrs"); 

	private static final Serializer<List<NamedAttribute>> SERIALIZER;
	private static final String EXCLUDED_CLASSES = "capsule.session.excluded.classes";
	private static final String SERIALIALIZATION_STRATEGY = "capsule.session.serialization.strategy";
	private static final String LOG_MESSAGE = "Capsule dry run {%d} total attrs, {%d} skipped attrs, {%d} bytes (serialized), {%d} bytes (encrypted), {%d} bytes (encoded), serialized in {%d} ns, encrypted in {%d} ns, encoded in {%d} ns, decoded in {%d} ns, decrypted in {%d} ns, deserialized in {%d} ns";
	private static final String SERIALIZE_SIZE = "seralizeSize";
	private static final String SERIALIZE_TIME = "serializeTime";
	private static final String DESERIALIZE_TIME = "deserializeTime";
	private static final String ENCODE_SIZE = "encodeSize";
	private static final String ENCODE_TIME = "encodeTime";
	private static final String DECODE_TIME = "decodeTime";
	private static final String ENCRYPT_TIME = "encryptTime";
	private static final String ENCRYPT_SIZE = "encryptSize";
	private static final String DECRYPT_TIME = "decryptTime";
	private static final String TOTAL_ATTRS = "totalAttrs";
	private static final String SKIPPED_ATTRS = "skippedAttrs";
 	
	

	private static final Set<String> excludedClasses;
	
	static {
		String strategyName = System.getProperty(SERIALIALIZATION_STRATEGY,"java").toUpperCase();
		SERIALIZER = SerializationStrategy.valueOf(strategyName).getSerializer();

		
		String configMsg = "DryRunAnalyzer ("+strategyName+","+statusString()+")";
		
		if (System.getProperty(EXCLUDED_CLASSES) == null) {
			excludedClasses = Collections.emptySet();
			LogHelper.info(staticInitLog, configMsg+": No classes excluded");
		} else {
			String[] exclusionsArray = System.getProperty(EXCLUDED_CLASSES).split(",");
			excludedClasses = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(exclusionsArray)));
			LogHelper.info(staticInitLog, configMsg+": Excluding "+ excludedClasses);
		}
		
	}
	
	private final InstrumentedTranscoder<List<NamedAttribute>,byte[]> serializer = new InstrumentedTranscoder<List<NamedAttribute>, byte[]>(SERIALIZER,SERIALIZE_TIME,DESERIALIZE_TIME);
	private final InstrumentedTranscoder<byte[],byte[]> crypter = new InstrumentedTranscoder<byte[], byte[]>(new CryptoTranscoder(),ENCRYPT_TIME,DECRYPT_TIME);
	private final InstrumentedTranscoder<byte[],byte[]> encoder = new InstrumentedTranscoder<byte[], byte[]>(new Base64Transcoder(),ENCODE_TIME,DECODE_TIME);


	@Override
	public void analyzeSession(HttpSession session) {
		try {
			Map<String, Long> stats = buildStats(session);
			String statement = String.format(LOG_MESSAGE, stats.get(TOTAL_ATTRS), stats.get(SKIPPED_ATTRS), stats.get(SERIALIZE_SIZE), stats.get(ENCRYPT_SIZE), stats.get(ENCODE_SIZE), stats.get(SERIALIZE_TIME),stats.get(ENCRYPT_TIME),stats.get(ENCODE_TIME), stats.get(DECODE_TIME), stats.get(DECRYPT_TIME),stats.get(DESERIALIZE_TIME));
			LogHelper.info(analyzeSessionLog, statement);
			} catch (Exception e) {
				LogHelper.error(analyzeSessionLog, e);
			}
		
	}	


	private Map<String, Long> buildStats(HttpSession session) {
		Map<String, Long> stats = new HashMap<String, Long>();

		List<NamedAttribute> sessionAttrs = extractSessionAttrs(session, stats);

		byte[] serialized = serializer.encode(sessionAttrs,stats);
		
		byte[] encrypted = crypter.encode(serialized, stats);

		byte[] encoded = encoder.encode(encrypted, stats);

		byte[] decoded = encoder.decode(encoded, stats);

		byte[] decrypted = crypter.decode(decoded, stats);

		List<NamedAttribute> deserialized = serializer.decode(decrypted, stats);
		
		assert sessionAttrs.equals(deserialized);

		stats.put(SERIALIZE_SIZE,(long)serialized.length);
		stats.put(ENCRYPT_SIZE,(long)encrypted.length);
		stats.put(ENCODE_SIZE, (long)encoded.length);
		
		return stats;
	}

	
	private List<NamedAttribute> extractSessionAttrs(HttpSession session, Map<String, Long> stats) {
		List<NamedAttribute> attrs = new LinkedList<NamedAttribute>();
		long total = 0;
		long skipped = 0;
		
		
		for (@SuppressWarnings("unchecked")Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
			total++;
			String name = e.nextElement();
			Object value = session.getAttribute(name);
			String valueClassName = value.getClass().getName();
			if (!excludedClasses.contains(valueClassName)) {

				NamedAttribute namedAttr = new NamedAttribute(name,value);
				attrs.add(namedAttr);
				
			} else {
				skipped++;
				LogHelper.debug(extractSessionAttrsLog, "Skipping session attribute"+name+" of type "+valueClassName);
			}
			
		}
		
		stats.put(TOTAL_ATTRS, total);
		stats.put(SKIPPED_ATTRS, skipped);
		return attrs;
	}
}
