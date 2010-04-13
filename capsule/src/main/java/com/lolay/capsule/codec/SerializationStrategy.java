package com.eharmony.capsule.codec;

/**
 * 
 * Factory enum for creating serialization transcoders
 * 
 * @author jtuberville
 *
 */
public enum SerializationStrategy {
	JAVA { @Override public <T> Serializer<T> getSerializer() {	return new JavaSerializer<T>(); } },
	HESSIAN { @Override public <T> Serializer<T> getSerializer() {	return new HessianSerializer<T>(); } };
	
	public abstract <T> Serializer<T> getSerializer();

}
