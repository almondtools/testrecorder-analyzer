package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import net.amygdalum.testrecorder.DefaultPerformanceProfile;
import net.amygdalum.testrecorder.DefaultSerializationProfile;
import net.amygdalum.testrecorder.profile.ClassPathConfigurationLoader;
import net.amygdalum.testrecorder.profile.DefaultPathConfigurationLoader;
import net.amygdalum.testrecorder.profile.PerformanceProfile;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.profile.SerializationProfile;

public class TestAgentConfiguration extends AgentConfiguration {

	private Map<Class<?>, List<Function<Object[], ?>>> configs;
	
	public TestAgentConfiguration() {
		super(new ClassPathConfigurationLoader(), new DefaultPathConfigurationLoader());
		configs = new HashMap<>();
	}

	public TestAgentConfiguration(ClassLoader loader) {
		super(new ClassPathConfigurationLoader(loader), new DefaultPathConfigurationLoader(loader));
		configs = new HashMap<>();
	}

	public TestAgentConfiguration withLoader(ClassLoader loader) {
		setConfigurationLoaders(asList(new ClassPathConfigurationLoader(loader), new DefaultPathConfigurationLoader(loader)));
		return this;
	}
	
	public static TestAgentConfiguration defaultConfig() {
		return (TestAgentConfiguration) new TestAgentConfiguration()
			.withDefaultValue(SerializationProfile.class, DefaultSerializationProfile::new)
			.withDefaultValue(PerformanceProfile.class, DefaultPerformanceProfile::new)
			.withDefaultValue(TestrecorderAnalyzerConfig.class, TestTestrecorderAnalyzerConfig::new);
	}

	public <T> TestAgentConfiguration loading(Class<T> clazz, Function<Object[], T> supplier) {
		configs.computeIfAbsent(clazz, key -> new ArrayList<>()).add(supplier);
		return this;
	}
	
	@Override
	protected <T> Stream<T> load(Class<T> clazz, Object... args) {
		List<Function<Object[], ?>> suppliers = configs.get(clazz);
		if (suppliers != null) {
			return suppliers.stream()
				.map(supplier -> supplier.apply(args))
				.map(object -> clazz.cast(object));
		}
		return super.load(clazz, args);
	}

}
