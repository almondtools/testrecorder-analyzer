package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Arrays;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.values.SerializedField;

public class ExpectGlobal implements PropertySelector {

	private Class<?> clazz;
	private String name;

	private Predicate<SerializedField> constraint;

	public ExpectGlobal(Class<?> clazz, String name, Predicate<SerializedField> constraint) {
		this.clazz = clazz;
		this.name = name;
		this.constraint = constraint;
	}

	@Override
	public boolean apply(TestCase testCase) {
		ContextSnapshot snapshot = testCase.getSnapshot();
		if (snapshot == null) {
			return false;
		}
		return Arrays.stream(snapshot.getExpectGlobals())
			.filter(field -> field.getDeclaringClass() == clazz && field.getName().equals(name))
			.findFirst()
			.map(constraint::test)
			.orElse(false);
	}

}
