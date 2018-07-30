package net.amygdalum.testrecorder.analyzer.indices;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

public class TestProfileInvocationContext implements TestTemplateInvocationContext, ParameterResolver {

	private String name;
	private Object value;

	public TestProfileInvocationContext(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String getDisplayName(int invocationIndex) {
		return name;
	}
	
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isInstance(value);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return value;
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return asList(this);
	}
}
