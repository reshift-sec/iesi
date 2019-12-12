package io.metadew.iesi.test.launch;

import java.util.Objects;

public class LaunchArgument {
	
	private boolean keyvalue;
	private String key;
	private String value;
	
	public LaunchArgument() {
		
	}
	
	public LaunchArgument(boolean keyValue, String key, String value) {
		this.setKeyvalue(keyValue);
		this.setKey(key);
		this.setValue(value);
	}

	// Getters and setters
	public boolean isKeyvalue() {
		return keyvalue;
	}

	public void setKeyvalue(boolean keyvalue) {
		this.keyvalue = keyvalue;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LaunchArgument that = (LaunchArgument) o;
		return keyvalue == that.keyvalue &&
				Objects.equals(key, that.key) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keyvalue, key, value);
	}
}