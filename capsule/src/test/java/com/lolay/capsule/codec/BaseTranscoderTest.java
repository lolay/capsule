package com.eharmony.capsule.codec;

import java.util.Collection;
import java.util.LinkedList;

import com.eharmony.capsule.NamedAttribute;

public abstract class BaseTranscoderTest {
	
	protected Collection<NamedAttribute> input;

	public void init() {
		input = new LinkedList<NamedAttribute>();
		input.add(new NamedAttribute("key1", "value1value1value1value1value1value1value1value1"));
		input.add(new NamedAttribute("key2", "value1value1value1value1value1"));
		input.add(new NamedAttribute("key3", "value1value1value1value1value1value1value1value1value1value1value1value1value1value1"));
		input.add(new NamedAttribute("key4", "value1value1value1value1value1value1value1value1value1value1value1value1value1value1"));
		input.add(new NamedAttribute("key5", "value1value1value1value1value1value1value1value1value1value1value1value1value1value1"));
		input.add(new NamedAttribute("key6", "value1value1value1value1value1value1value1value1value1value1value1value1value1value1"));
		input.add(new NamedAttribute("key7", "value1value1value1value1value1value1value1value1value1value1value1value1value1value1"));
	}

}
