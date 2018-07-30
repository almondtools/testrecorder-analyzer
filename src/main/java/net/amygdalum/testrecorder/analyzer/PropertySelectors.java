package net.amygdalum.testrecorder.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PropertySelectors {

	private PropertyKeySelector<?> keySelector;
	private PropertyRangeSelector<?> rangeSelector;
	private List<PropertySelector> filterSelectors;

	public PropertySelectors(List<PropertySelector> selectors, Predicate<Index<?>> isIndex) {
		this.filterSelectors = new ArrayList<>(selectors.size());

		for (PropertySelector selector : selectors) {
			if (keySelector == null && selector instanceof PropertyKeySelector<?> && isIndex.test(((PropertyKeySelector<?>) selector).indexType())) {
				keySelector = (PropertyKeySelector<?>) selector;
			} else if (rangeSelector == null && selector instanceof PropertyRangeSelector<?>&& isIndex.test(((PropertyRangeSelector<?>) selector).indexType())) {
				rangeSelector = (PropertyRangeSelector<?>) selector;
			} else {
				filterSelectors.add(selector);
			}
		}
	}

	public Stream<TestCase> dispatch(Function<PropertyKeySelector<?>, Stream<TestCase>> onKeySelector, Function<PropertyRangeSelector<?>, Stream<TestCase>> onRangeSelector, Supplier<Stream<TestCase>> elseSelector) {
		Stream<TestCase> stream = bestPreselector(onKeySelector, onRangeSelector, elseSelector);
		for (PropertySelector selector : filterSelectors) {
			stream = stream.filter(testCase -> selector.apply(testCase));
		}
		return stream;
	}

	private Stream<TestCase> bestPreselector(Function<PropertyKeySelector<?>, Stream<TestCase>> onKeySelector, Function<PropertyRangeSelector<?>, Stream<TestCase>> onRangeSelector,
		Supplier<Stream<TestCase>> elseSelector) {
		if (keySelector != null) {
			Stream<TestCase> stream = onKeySelector.apply(keySelector);
			if (rangeSelector != null) {
				stream = stream.filter(testCase -> rangeSelector.apply(testCase));
			}
			return stream;
		} else if (rangeSelector != null) {
			return onRangeSelector.apply(rangeSelector);
		} else {
			return elseSelector.get();
		}
	}

}
