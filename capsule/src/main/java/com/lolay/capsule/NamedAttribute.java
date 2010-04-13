package com.eharmony.capsule;

import java.io.Serializable;

/**
 * 
 * Core value object for the capsule representing String named attributes of any value
 * 
 * @author jtuberville
 *
 */
public class NamedAttribute implements Serializable {
	private static final long serialVersionUID = -5683149255343169586L;
	private final String name;
	private final Object value;
	
	public NamedAttribute(String name, Object object) {
		if (object != null && !(object instanceof Serializable)) {
			throw new IllegalArgumentException("Object must be serializable");
		}
		this.name = name;
		this.value = object;
	}
	
	

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (! (obj instanceof NamedAttribute))
			return false;
		NamedAttribute other = (NamedAttribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	

}
