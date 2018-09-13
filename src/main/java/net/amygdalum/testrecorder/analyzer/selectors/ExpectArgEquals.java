package net.amygdalum.testrecorder.analyzer.selectors;

import static net.amygdalum.testrecorder.util.Types.boxedType;

import java.lang.reflect.Type;
import java.util.Objects;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.MatchLiteralProperties;
import net.amygdalum.testrecorder.analyzer.PropertyKeySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedArgument;
import net.amygdalum.testrecorder.util.OptionalValue;

public class ExpectArgEquals<T> implements PropertyKeySelector<T>, MatchLiteralProperties {

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
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (!isAppliableTo(snapshot.getArgumentTypes())) {
			return false;
		}
		return OptionalValue.of(snapshot.onExpectArg(argIndex))
			.map(SerializedArgument::getValue)
			.filter(this::isLiteral)
			.map(this::extractLiteral)
			.map(arg -> Objects.equals(arg, object))
			.orElse(false);
	}

	private boolean isAppliableTo(Type[] types) {
		if (argIndex >= types.length) {
			return false;
		}
		if (!index.type().isAssignableFrom(boxedType(types[argIndex]))) {
			return false;
		}
		return true;
	}

	@Override
	public Index<T> indexType() {
		return index;
	}

	public T key() {
		return object;
	}
}