package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedArgument;
import net.amygdalum.testrecorder.types.SerializedValue;

public class ExpectArg implements PropertySelector {

	private int index;
	private Predicate<SerializedValue> constraint;

	public ExpectArg(int index, Predicate<SerializedValue> constraint) {
		this.index = index;
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return snapshot.onExpectArg(index)
			.map(SerializedArgument::getValue)
			.map(constraint::test)
			.orElse(false);
	}

}
