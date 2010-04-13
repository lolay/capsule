package com.eharmony.capsule.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.eharmony.capsule.CapsuleException;

/**
 * 
 * Encodes an arbitrary type into a bytestream using the Sun Java serialization format.
 * 
 * @author jtuberville
 *
 * @param <T> original type
 */
public class JavaSerializer<T>  implements Serializer<T> {

	@SuppressWarnings("unchecked")
	public T decode(byte[] sequence1) {
		T result;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(sequence1);
			ObjectInputStream ois = new ObjectInputStream(bais);
			result = (T) ois.readObject();
		} catch (IOException e) {
			throw new CapsuleException(e);
		} catch (ClassNotFoundException e) {
			throw new CapsuleException(e);
		}
		return result;
	}

	public byte[] encode(T sequence1) {
		byte[] result;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(bos);
			stream.writeObject(sequence1);
			stream.close();
			result = bos.toByteArray();
		} catch (IOException e) {
			throw new CapsuleException(e);
		}
		return result;
	}


}
