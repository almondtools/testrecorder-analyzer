package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedValue;

public class ExpectThis implements PropertySelector {

	private Predicate<SerializedValue> constraint;

	public ExpectThis(Predicate<SerializedValue> constraint) {
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
			
		}
		return snapshot.onExpectThis()
			.map(constraint::test)
			.orElse(false);
	}

}
