package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Queue;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedOutput;

public class ExpectOutput implements PropertySelector {

	private Predicate<Queue<SerializedOutput>> constraint;

	public ExpectOutput(Predicate<Queue<SerializedOutput>> constraint) {
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return constraint.test(snapshot.getExpectOutput());
	}

}
