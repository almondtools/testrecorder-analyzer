package net.amygdalum.testrecorder.analyzer.query;

import java.util.function.Predicate;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.Property;
import net.amygdalum.testrecorder.analyzer.TestCase;

public interface TestCasePredicate extends Predicate<TestCase> {

	public static TestCasePredicate onContextSnapshot(Predicate<ContextSnapshot> downstream) {
		return testCase -> downstream.test(testCase.getSnapshot());
	}

	public static <T> TestCasePredicate onProperty(Property<T> property, Predicate<T> downstream) {
		return testCase -> testCase.get(property)
			.map(downstream::test)
			.orElse(false);
	}

}
