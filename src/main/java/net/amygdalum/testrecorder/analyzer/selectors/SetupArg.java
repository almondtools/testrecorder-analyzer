package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.function.Predicate;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.SerializedValue;

public class SetupArg implements PropertySelector {

	private int index;
	private Predicate<SerializedValue> constraint;

	public SetupArg(int index, Predicate<SerializedValue> constraint) {
		this.index = index;
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return snapshot.onSetupArg(index)
			.map(constraint::test)
			.orElse(false);
	}

}
