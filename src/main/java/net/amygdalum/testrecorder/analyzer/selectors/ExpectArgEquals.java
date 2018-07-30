package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Objects;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.PropertyKeySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class ExpectArgEquals<T> implements PropertyKeySelector<T> {

	private int argIndex;
	private T object;
	private Index<T> index;

	public ExpectArgEquals(int argIndex, T object, Index<T> index) {
		this.argIndex = argIndex;
		this.object = object;
		this.index = index;
	}

	@Override
	public boolean apply(TestCase testCase) {
		return testCase.getSnapshot().onExpectArg(argIndex)
			.filter(arg -> arg instanceof SerializedLiteral)
			.map(arg -> (SerializedLiteral) arg)
			.map(arg -> Objects.equals(arg.getValue(), object))
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