package net.amygdalum.testrecorder.analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.amygdalum.testrecorder.ContextSnapshot;

public class TestCase implements PropertyStore {

	private ContextSnapshot snapshot;
	private Map<String, Object> properties;

	public TestCase() {
		this.properties = new HashMap<>();
	}
	
	public TestCase(ContextSnapshot snapshot) {
		this.snapshot = snapshot;
		this.properties = new HashMap<>();
	}
	
	public void setSnapshot(ContextSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public ContextSnapshot getSnapshot() {
		return snapshot;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public <T> Optional<T> get(Property<T> property) {
		String key = property.getKey();
		return property.validate(properties.get(key));
	}

	@Override
	public <T> void set(Property<T> property, T value) {
		String key = property.getKey();
		properties.put(key, value);
	}

}
