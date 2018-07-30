package net.amygdalum.testrecorder.analyzer.indices;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class ExpectArguments<T> extends Index<T> {

	private int argIndex;
	private String name;
	private Class<T> type;

	private ExpectArguments() {
	}

	public static ExpectArguments<?> expectArgumentAt(int argIndex) {
		ExpectArguments<?> expectArguments = new ExpectArguments<>();
		expectArguments.argIndex = argIndex;
		expectArguments.name = "expect.arguments." + expectArguments.argIndex;
		return expectArguments;
	}

	@SuppressWarnings("unchecked")
	public <S> ExpectArguments<S> ofType(Class<S> type) {
		ExpectArguments<S> expectArguments = (ExpectArguments<S>) this;
		expectArguments.type = type;
		return expectArguments;
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
		return testCase.getSnapshot().onExpectArg(argIndex)
			.filter(value -> value instanceof SerializedLiteral)
			.map(value -> (SerializedLiteral) value)
			.filter(value -> type.isAssignableFrom(value.getType()))
			.map(value -> type.cast(value.getValue()));
	}

}
