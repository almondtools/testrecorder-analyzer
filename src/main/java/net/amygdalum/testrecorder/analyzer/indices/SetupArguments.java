package net.amygdalum.testrecorder.analyzer.indices;

import static net.amygdalum.testrecorder.util.Types.boxedType;

import java.lang.reflect.Type;

import net.amygdalum.testrecorder.analyzer.Index;
import net.amygdalum.testrecorder.analyzer.MatchLiteralProperties;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedArgument;
import net.amygdalum.testrecorder.util.OptionalValue;

public class SetupArguments<T> extends Index<T> implements MatchLiteralProperties {

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
	public OptionalValue<T> extract(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (!isAppliableTo(snapshot.getArgumentTypes())) {
			return OptionalValue.empty();
		}
		return OptionalValue.of(snapshot.onSetupArg(argIndex))
			.map(SerializedArgument::getValue)
			.filter(this::isLiteral)
			.map(this::extractLiteral)
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
