package net.amygdalum.testrecorder.analyzer;

import java.util.Optional;

public interface PropertyStore {

	<T> Optional<T> get(Property<T> property);

	<T> void set(Property<T> property, T value);

}
