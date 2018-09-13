package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Queue;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedInput;

public class SetupInput implements PropertySelector {

	private Predicate<Queue<SerializedInput>> constraint;

	public SetupInput(Predicate<Queue<SerializedInput>> constraint) {
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return constraint.test(snapshot.getSetupInput());
	}

}
