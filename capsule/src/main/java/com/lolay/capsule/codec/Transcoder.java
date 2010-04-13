package com.eharmony.capsule.codec;

/**
 * 
 * Represents any algorithm that encodes one sequence to another sequence as well as decoding back to the original sequence
 * 
 * @author jtuberville
 *
 * @param <S1> The type of the initial sequence
 * @param <S2> The type of the encoded sequence
 */
public interface Transcoder<S1, S2> {
	S2 encode(S1 sequence1);
	S1 decode(S2 sequence1);
}
