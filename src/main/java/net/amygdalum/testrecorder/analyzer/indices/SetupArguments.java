package net.amygdalum.testrecorder.analyzer.indices;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class SetupArguments<T> extends Index<T> {

	private int argIndex;
	private String name;
	private Class<T> type;

	private SetupArguments() {
	}

	public static SetupArguments<?> setupArgumentAt(int argIndex) {
		SetupArguments<?> setupArguments = new SetupArguments<>();
		setupArguments.argIndex = argIndex;
		setupArguments.name = "setup.arguments." + setupArguments.argIndex;
		return setupArguments;
	}

	@SuppressWarnings("unchecked")
	public <S> SetupArguments<S> ofType(Class<S> type) {
		SetupArguments<S> setupArguments = (SetupArguments<S>) this;
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
		return testCase.getSnapshot().onSetupArg(argIndex)
			.filter(value -> value instanceof SerializedLiteral)
			.map(value -> (SerializedLiteral) value)
			.filter(value -> type.isAssignableFrom(value.getType()))
			.map(value -> type.cast(value.getValue()));
	}

}
