package net.amygdalum.testrecorder.analyzer.query;

import net.amygdalum.testrecorder.analyzer.Property;
import net.amygdalum.testrecorder.analyzer.PropertyUpdate;
import net.amygdalum.testrecorder.analyzer.TestCase;

public class TestProperty implements PropertyUpdate {

	public static final Property<String> TEST = new Property<>(TestProperty.class, "test", String.class);
	private String value;

	public TestProperty(String value) {
		this.value = value;
	}
	
	@Override
	public boolean apply(TestCase testCase) {
		return TEST.set(value).on(testCase);
	}
	
}