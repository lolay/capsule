package com.eharmony.capsule.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.eharmony.capsule.CapsuleException;

/**
 * 
 * Encodes an arbitrary type into a bytestream using the portable Hessian serialization format.
 * 
 * @author jtuberville
 *
 * @param <T> original type
 */
public class HessianSerializer<T>  implements Serializer<T> {


	public byte[] encode(T sequence1) {
		byte[] result;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Hessian2Output stream = new Hessian2Output(bos);
			stream.writeObject(sequence1);
			stream.close();
			result = bos.toByteArray();
		} catch (IOException e) {
			throw new CapsuleException("Error serializing",e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public T decode(byte[] sequence1) {
		T result;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(sequence1);
			Hessian2Input ois = new Hessian2Input(bais);
			result = (T) ois.readObject();
		} catch (IOException e) {
			throw new CapsuleException("Error deserializing",e);
		} 
		return result;
	}



}
