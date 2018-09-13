package net.amygdalum.testrecorder.analyzer;

import org.assertj.core.api.Condition;
import org.assertj.core.description.Description;
import org.assertj.core.description.TextDescription;

import net.amygdalum.extensions.assertj.conditions.CompoundDescription;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class ATestCase extends Condition<TestCase> {

	private Condition<ContextSnapshot> snapshotCondition;

	public ATestCase() {
	}

	public static ATestCase withSnapshot(Condition<ContextSnapshot> snapshotCondition) {
		return new ATestCase().andSnapshot(snapshotCondition);
	}

	public ATestCase andSnapshot(Condition<ContextSnapshot> snapshotCondition) {
		this.snapshotCondition = snapshotCondition;
		return this;
	}

	@Override
	public Description description() {
		CompoundDescription description = new CompoundDescription(new TextDescription("matches TestCase"));
		if (snapshotCondition != null) {
			description.addComponent("snapshot", snapshotCondition.description());
		}
		return description;
	}

	@Override
	public boolean matches(TestCase testCase) {
		if (snapshotCondition != null && !snapshotCondition.matches(testCase.getSnapshot())) {
			return false;
		}
		return true;
	}

}
