package net.amygdalum.testrecorder.analyzer.indices;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class MultipleProfiles implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Class<?> clazz = context.getRequiredTestClass();
		return Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(ProfileData.class))
			.map(field -> new TestProfileInvocationContext(field.getAnnotation(ProfileData.class).displayName(), getField(null, field)));
	}

	private Object getField(Object instance, Field field) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

}
