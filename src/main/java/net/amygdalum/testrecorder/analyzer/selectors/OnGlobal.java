package net.amygdalum.testrecorder.analyzer.selectors;

import java.util.Arrays;
import java.util.function.BiFunction;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.values.SerializedField;

public class OnGlobal implements PropertySelector {

	private Class<?> clazz;
	private String name;

	private BiFunction<SerializedField, SerializedField, Boolean> constraint;

	public OnGlobal(Class<?> clazz, String name, BiFunction<SerializedField, SerializedField, Boolean> constraint) {
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
		return snapshot.onGlobals()
			.biflatmap(
				before -> Arrays.stream(before)
					.filter(field -> field.getDeclaringClass() == clazz && field.getName().equals(name))
					.findFirst(),
				after -> Arrays.stream(after)
					.filter(field -> field.getDeclaringClass() == clazz && field.getName().equals(name))
					.findFirst())
			.map((before, after) -> constraint.apply(before, after), single -> false)
			.orElse(false);
	}

}
