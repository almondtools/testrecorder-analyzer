package net.amygdalum.testrecorder.analyzer.selectors;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class MissingExpectThis implements PropertySelector {

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return !snapshot.onExpectThis().isPresent();
	}

}
