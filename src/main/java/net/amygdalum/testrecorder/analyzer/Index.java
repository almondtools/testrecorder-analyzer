package net.amygdalum.testrecorder.analyzer;

import java.util.Optional;

public abstract class Index<T> {

	public abstract String name();

	public abstract Class<?> type();

	public abstract Optional<T> extract(TestCase testCase);

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Index<?> that = (Index<?>) obj;
		return this.name().equals(that.name())
			&& this.type() == that.type();
	}

	@Override
	public int hashCode() {
		return name().hashCode() * 13 + type().hashCode();
	}
}
