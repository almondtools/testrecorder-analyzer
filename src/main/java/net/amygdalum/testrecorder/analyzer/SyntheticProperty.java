package net.amygdalum.testrecorder.analyzer;

import java.util.Optional;

public class SyntheticProperty<T> {

	private Class<?> clazz;
	private String name;
	private Class<T> type;

	public SyntheticProperty(Class<?> clazz, String name, Class<T> type) {
		this.clazz = clazz;
		this.name = name;
		this.type = type;
	}
	
	public Class<?> getDefinition() {
		return clazz;
	}
	
	public Class<T> getType() {
		return type;
	}

	public Optional<T> from(SyntheticPropertyStore store) {
		return store.get(this);
	}

	public SetProperty set(T value) {
		return new SetProperty(value);
	}
	
	public String getKey() {
		return clazz.getName() + "." + name;
	}
	
	public Optional<T> validate(Object object) {
		if (object == null || !type.isInstance(object)) {
			return Optional.empty();
		}
		return Optional.of(type.cast(object));
	}

	public class SetProperty {

		private T value;

		public SetProperty(T value) {
			this.value = value;
		}

		public boolean on(SyntheticPropertyStore store) {
			return store.set(SyntheticProperty.this, value);
		}
		
	}

}
