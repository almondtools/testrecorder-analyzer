package net.amygdalum.testrecorder.analyzer.indices;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.MatchLiteralProperties;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.evaluator.SerializedValueEvaluator;
import net.amygdalum.testrecorder.util.OptionalValue;

public class ExpectThis<T> extends Index<T> implements MatchLiteralProperties {

	private SerializedValueEvaluator eval;
	private String name;
	private Class<T> type;

	private ExpectThis(String expression, Class<T> type) {
		this.type = type;
		this.eval = new SerializedValueEvaluator(expression, type);
		this.name = "expect.this" + expression;
	}

	public static ExpectThis<?> expectThisApplying(String expression) {
		return new ExpectThis<>(expression, null);
	}

	public static <T> ExpectThis<T> expectThisApplying(String expression, Class<T> type) {
		return new ExpectThis<>(expression, type);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Class<?> type() {
		return type;
	}

	@Override
	public OptionalValue<T> extract(TestCase testCase) {
		return OptionalValue.of(testCase.getSnapshot().onExpectThis()
			.flatMap(eval::applyTo))
			.filter(this::isLiteral)
			.map(this::extractLiteral)
			.filter(value -> value == null || type.isInstance(value))
			.map(type::cast);
	}

}
