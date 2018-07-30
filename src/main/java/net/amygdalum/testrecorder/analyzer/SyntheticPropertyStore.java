package net.amygdalum.testrecorder.analyzer;

import java.util.Optional;

public interface SyntheticPropertyStore {

	<T> Optional<T> get(SyntheticProperty<T> property);

	<T> boolean set(SyntheticProperty<T> property, T value);

}
