package net.amygdalum.testrecorder.analyzer.request;

import net.amygdalum.testrecorder.analyzer.PropertyUpdate;
import net.amygdalum.testrecorder.analyzer.SyntheticProperty;
import net.amygdalum.testrecorder.analyzer.TestCase;

public class TestProperty implements PropertyUpdate {

	public static final SyntheticProperty<String> TEST = new SyntheticProperty<>(TestProperty.class, "test", String.class);
	private String value;

	public TestProperty(String value) {
		this.value = value;
	}
	
	@Override
	public boolean apply(TestCase testCase) {
		return TEST.set(value).on(testCase);
	}
	
}