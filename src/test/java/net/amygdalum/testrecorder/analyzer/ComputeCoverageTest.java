package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.analyzer.TestAgentConfiguration.defaultConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.callsiterecorder.CallsiteRecorder;
import net.amygdalum.testrecorder.deserializers.Adaptors;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerator;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerator;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;
import net.amygdalum.testrecorder.util.testobjects.TestTarget;

public class ComputeCoverageTest {

	private AgentConfiguration config;

	@BeforeEach
	void before() throws Exception {
		config = defaultConfig();
		;
	}

	@Test
	void testPrime() throws Exception {
		ContextSnapshot snapPrime = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(7));
		TestCase testPrime = new TestCase(snapPrime);
		coverage(config).process(testPrime);

		assertThat(ComputeCoverage.COVERAGE.from(testPrime)).hasValueSatisfying(coverage -> {
			assertThat(coverage.getMethods()).hasSize(3);
			assertThat(coverage.getLineCoverage(10)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isFalse();
			});
			assertThat(coverage.getLineCoverage(13)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isTrue();
			});
		});
	}

	private UpdateProcess coverage(AgentConfiguration config) {
		SetupGenerators setup = new SetupGenerators(new Adaptors<SetupGenerators>(config).load(SetupGenerator.class));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors<MatcherGenerators>(config).load(MatcherGenerator.class));
		List<TestRecorderAgentInitializer> initializers = config.loadConfigurations(TestRecorderAgentInitializer.class);
		return UpdateProcesses.of(new ComputeSourceCode(setup, matcher, initializers), new ComputeCoverage());
	}

	@Test
	void testNotPrime() throws Exception {
		ContextSnapshot snapNotPrime = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(4));
		TestCase testNotPrime = new TestCase(snapNotPrime);
		coverage(config).process(testNotPrime);

		assertThat(ComputeCoverage.COVERAGE.from(testNotPrime)).hasValueSatisfying(coverage -> {
			assertThat(coverage.getMethods()).hasSize(3);
			assertThat(coverage.getLineCoverage(10)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isTrue();
			});
			assertThat(coverage.getLineCoverage(13)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isFalse();
			});
		});
	}

	@Test
	void testEquals() throws Exception {
		ContextSnapshot snap5 = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(5));
		TestCase test5 = new TestCase(snap5);
		coverage(config).process(test5);

		ContextSnapshot snap7 = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(7));
		TestCase test7 = new TestCase(snap7);
		coverage(config).process(test7);

		Coverage coverage5 = ComputeCoverage.COVERAGE.from(test5).orElseThrow(RuntimeException::new);
		Coverage coverage7 = ComputeCoverage.COVERAGE.from(test7).orElseThrow(RuntimeException::new);
		assertThat(coverage5).isEqualTo(coverage7);
	}

	@Test
	void testNotEquals() throws Exception {
		ContextSnapshot snap4 = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(4));
		TestCase test4 = new TestCase(snap4);
		coverage(config).process(test4);

		ContextSnapshot snap2 = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(2));
		TestCase test2 = new TestCase(snap2);
		coverage(config).process(test2);

		ContextSnapshot snap5 = createTestTargetSnapshot(new TestTarget(), Integer.valueOf(5));
		TestCase test5 = new TestCase(snap5);
		coverage(config).process(test5);

		Coverage coverage4 = ComputeCoverage.COVERAGE.from(test4).orElseThrow(RuntimeException::new);
		Coverage coverage2 = ComputeCoverage.COVERAGE.from(test2).orElseThrow(RuntimeException::new);
		Coverage coverage5 = ComputeCoverage.COVERAGE.from(test5).orElseThrow(RuntimeException::new);

		assertThat(coverage4).withFailMessage("branch coverage should be different").isNotEqualTo(coverage2);
		assertThat(coverage4).withFailMessage("instruction coverage should be different").isNotEqualTo(coverage5);
	}

	private ContextSnapshot createTestTargetSnapshot(TestTarget thisObject, Integer argObject) throws Exception {
		try (CallsiteRecorder recorder = new CallsiteRecorder(method(TestTarget.class, "test", Integer.class))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	private Method method(Class<?> clazz, String name, Class<?>... argtypes) {
		try {
			return clazz.getDeclaredMethod(name, argtypes);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
