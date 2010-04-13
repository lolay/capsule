package com.eharmony.capsule.codec;

/**
 * Serializers are transcoders that convert <T> to a serialize byte stream
 * @author jtuberville
 *
 * @param <T>
 */
public interface Serializer<T> extends Transcoder<T, byte[]> {

}
