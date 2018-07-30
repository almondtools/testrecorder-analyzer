package net.amygdalum.testrecorder.analyzer.indices;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.evaluator.SerializedValueEvaluator;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class SetupThis<T> extends Index<T> {

	private SerializedValueEvaluator eval;
	private String name;
	private Class<T> type;

	private SetupThis() {
	}

	public static SetupThis<?> setupThisApplying(String expression) {
		SetupThis<?> setupArguments = new SetupThis<>();
		setupArguments.eval = new SerializedValueEvaluator(expression);
		setupArguments.name = "setup.this." + expression;
		return setupArguments;
	}

	@SuppressWarnings("unchecked")
	public <S> SetupThis<S> ofType(Class<S> type) {
		SetupThis<S> setupArguments = (SetupThis<S>) this;
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
