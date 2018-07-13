package net.amygdalum.testrecorder.analyzer;

import java.util.Optional;

public class Property<T> {

	private Class<?> clazz;
	private String name;
	private Class<T> type;

	public Property(Class<?> clazz, String name, Class<T> type) {
		this.clazz = clazz;
		this.name = name;
		this.type = type;
	}
	
	public Class<?> getDefinition() {
		return clazz;
	}

	public Optional<T> from(PropertyStore store) {
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

		public boolean on(PropertyStore store) {
			return store.set(Property.this, value);
		}
		
	}

}
