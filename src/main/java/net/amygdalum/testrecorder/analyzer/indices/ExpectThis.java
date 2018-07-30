package net.amygdalum.testrecorder.analyzer.indices;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.evaluator.SerializedValueEvaluator;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class ExpectThis<T> extends Index<T> {

	private SerializedValueEvaluator eval;
	private String name;
	private Class<T> type;

	private ExpectThis() {
	}

	public static ExpectThis<?> setupThisApplying(String expression) {
		ExpectThis<?> setupArguments = new ExpectThis<>();
		setupArguments.eval = new SerializedValueEvaluator(expression);
		setupArguments.name = "setup.this." + expression;
		return setupArguments;
	}

	@SuppressWarnings("unchecked")
	public <S> ExpectThis<S> ofType(Class<S> type) {
		ExpectThis<S> setupArguments = (ExpectThis<S>) this;
		setupArguments.type = type;
		return setupArguments;
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
	public Optional<T> extract(TestCase testCase) {
		return testCase.getSnapshot().onSetupThis()
			.flatMap(eval::applyTo)
			.filter(value -> value instanceof SerializedLiteral)
			.map(value -> (SerializedLiteral) value)
			.filter(value -> type.isAssignableFrom(value.getType()))
			.map(value -> type.cast(value.getValue()));
	}

}
