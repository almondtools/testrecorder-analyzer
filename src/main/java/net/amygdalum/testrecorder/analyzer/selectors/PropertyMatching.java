package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Optional;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.SyntheticProperty;
import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;

public class PropertyMatching<T> implements PropertySelector {

	private SyntheticProperty<T> property;
	private Predicate<T> constraint;

	public PropertyMatching(SyntheticProperty<T> property, Predicate<T> constraint) {
		this.property = property;
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		Optional<T> value = testCase.get(property);
		return value
			.map(constraint::test)
			.orElse(false);
	}

}
