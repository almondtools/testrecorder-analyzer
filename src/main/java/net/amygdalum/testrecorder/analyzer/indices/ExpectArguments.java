package net.amygdalum.testrecorder.analyzer.indices;

import static net.amygdalum.testrecorder.util.Types.boxedType;

import java.lang.reflect.Type;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.MatchLiteralProperties;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.util.OptionalValue;

public class ExpectArguments<T> extends Index<T> implements MatchLiteralProperties {

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
	public OptionalValue<T> extract(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (!isAppliableTo(snapshot.getArgumentTypes())) {
			return OptionalValue.empty();
		}
		return OptionalValue.of(snapshot.onExpectArg(argIndex))
			.filter(this::isLiteral)
			.map(this::extractLiteral)
			.filter(value -> value == null || type.isInstance(value))
			.map(type::cast);
	}

	private boolean isAppliableTo(Type[] types) {
		if (argIndex >= types.length) {
			return false;
		}
		if (!type.isAssignableFrom(boxedType(types[argIndex]))) {
			return false;
		}
		return true;
	}

}
