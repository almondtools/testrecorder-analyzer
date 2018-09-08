package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Objects;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.MatchLiteralProperties;
import net.amygdalum.testrecorder.analyzer.PropertyKeySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.evaluator.SerializedValueEvaluator;
import net.amygdalum.testrecorder.util.OptionalValue;

public class SetupThisEquals<T> implements PropertyKeySelector<T>, MatchLiteralProperties {

	private SerializedValueEvaluator expression;
	private T object;
	private Index<T> index;

	public SetupThisEquals(String expression, T object, Index<T> index) {
		this.expression = new SerializedValueEvaluator(expression, index.type());
		this.object = object;
		this.index = index;
	}

	@Override
	public boolean apply(TestCase testCase) {
		return OptionalValue.of(testCase.getSnapshot().onSetupThis()
			.flatMap(self -> expression.applyTo(self)))
			.filter(this::isLiteral)
			.map(this::extractLiteral)
			.map(arg -> Objects.equals(arg, object))
			.orElse(false);
	}

	@Override
	public Index<T> indexType() {
		return index;
	}

	public T key() {
		return object;
	}
}