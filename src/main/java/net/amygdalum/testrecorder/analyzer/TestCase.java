package net.amygdalum.testrecorder.analyzer;

import static java.util.Collections.emptyMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.amygdalum.testrecorder.ContextSnapshot;

public class TestCase implements SyntheticPropertyStore {

	private String id; 
	private ContextSnapshot snapshot;
	private Map<String, Object> properties;

	public TestCase(String id, ContextSnapshot snapshot, Map<String, Object> properties) {
		this.id = id;
		this.snapshot = snapshot;
		this.properties = new LinkedHashMap<>(properties);
	}
	
	public TestCase(ContextSnapshot snapshot) {
		this(UUID.randomUUID().toString(), snapshot, emptyMap());
	}
	
	public String getId() {
		return id;
	}
	
	public ContextSnapshot getSnapshot() {
		return snapshot;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public <T> Optional<T> get(SyntheticProperty<T> property) {
		String key = property.getKey();
		return property.validate(properties.get(key));
	}

	@Override
	public <T> boolean set(SyntheticProperty<T> property, T value) {
		String key = property.getKey();
		
		Object oldValue = properties.put(key, value);
		return !Objects.equals(oldValue, value);
	}

}
