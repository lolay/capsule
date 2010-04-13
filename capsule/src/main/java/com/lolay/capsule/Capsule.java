package com.lolay.capsule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.lolay.capsule.codec.CryptoTranscoder;
import com.lolay.capsule.codec.JavaSerializer;
import com.lolay.capsule.codec.Serializer;
import com.lolay.capsule.codec.Transcoder;

/**
 * Represents a collection of NamedAttributes that can be converted to a serial form
 * @author joshua
 *
 */
public class Capsule {
	// Inner class since only one SerialForm, for now, but makes it easier to extract to factory if needed
	public static class SerialForm {
		
		private Serializer<Collection<NamedAttribute>> serializer = new JavaSerializer<Collection<NamedAttribute>>();
		static final byte[] MAGIC = "CPSL".getBytes();
		static final short VERSION = 1;

		public Capsule parse(byte[] stream) {
			Collection<NamedAttribute> attributes;
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(stream);
				DataInputStream input = new DataInputStream(bais);

				checkMagic(input);
				checkVersion(input);
				short keyVersion = input.readShort();
				Transcoder<byte[], byte[]> crypto = new CryptoTranscoder(keyVersion);
				byte[] cipherbytes = new byte[input.available()];
				input.readFully(cipherbytes);
				byte[] clearbytes = crypto.decode(cipherbytes);
				attributes = serializer.decode(clearbytes);

			} catch (IOException e) {
				throw new CapsuleException(e);
			}
			return new Capsule(attributes);
		}

		private void checkVersion(DataInputStream input) throws IOException {
			short streamVersion = input.readShort();
			if (streamVersion != VERSION) {
				throw new IllegalArgumentException("Version "
						+ streamVersion + " is not supported");
			}
		}

		private void checkMagic(DataInputStream input) throws IOException {
			byte[] firstBytes = new byte[MAGIC.length];
			input.read(firstBytes, 0, MAGIC.length);
			if (!Arrays.equals(MAGIC,firstBytes)) {
				throw new IllegalArgumentException(
						"Byte stream is not valid");
			}
		}

		public byte[] toBytes(Capsule capsule) {
			KeyManager keyManager = new KeyManager();
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try {
				DataOutputStream output = new DataOutputStream(bytes);
				output.write(SerialForm.MAGIC);
				output.writeShort(SerialForm.VERSION);
				short keyVersion = keyManager.getLatestVersion();
				output.writeShort(keyVersion);
				byte[] serialized = serializer.encode(capsule.attributes);
				CryptoTranscoder encrypter = new CryptoTranscoder(keyVersion);
				byte[] cipherbytes = encrypter.encode(serialized);
				output.write(cipherbytes);
				
			} catch (IOException e) {
				throw new CapsuleException(e);
			}

			return bytes.toByteArray();
		}

	}

	private Collection<NamedAttribute> attributes;

	
	public Capsule(Collection<NamedAttribute> attributes) {
		this.attributes = attributes;
	}
	
	

	public byte[] toBytes() {
		return new SerialForm().toBytes(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Capsule))
			return false;
		Capsule other = (Capsule) obj;

		return attributes.equals(other.attributes);
	}

	public Collection<NamedAttribute> getAttributes() {
		return attributes;
	}

}
