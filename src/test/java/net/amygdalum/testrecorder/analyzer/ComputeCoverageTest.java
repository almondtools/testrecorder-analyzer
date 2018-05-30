package net.amygdalum.testrecorder.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ConfigurableSerializerFacade;
import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.DefaultSerializationProfile;
import net.amygdalum.testrecorder.MethodSignature;
import net.amygdalum.testrecorder.analyzer.ComputeCoverage;
import net.amygdalum.testrecorder.analyzer.ComputeSourceCode;
import net.amygdalum.testrecorder.analyzer.Coverage;
import net.amygdalum.testrecorder.analyzer.Task;
import net.amygdalum.testrecorder.analyzer.Tasks;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.deserializers.Adaptors;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerator;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerator;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.profile.SerializationProfile;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.types.SerializerSession;
import net.amygdalum.testrecorder.util.testobjects.TestTarget;

public class ComputeCoverageTest {

	private AgentConfiguration config;
	private ConfigurableSerializerFacade facade;
	private SerializerSession session;

	@BeforeEach
	void before() throws Exception {
		config = new AgentConfiguration(getClass().getClassLoader())
			.withDefaultValue(SerializationProfile.class, DefaultSerializationProfile::new);
		facade = new ConfigurableSerializerFacade(config);
		session = facade.newSession();
	}

	@Test
	void testPrime() throws Exception {
		ContextSnapshot snapPrime = createTestTargetSnapshot(new TestTarget(), true, Integer.valueOf(7));
		TestCase testPrime = new TestCase(snapPrime);
		coverage().process(testPrime);

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

	private Task coverage() {
		SetupGenerators setup = new SetupGenerators(new Adaptors<SetupGenerators>(config).load(SetupGenerator.class));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors<MatcherGenerators>(config).load(MatcherGenerator.class));
		List<TestRecorderAgentInitializer> initializers = config.loadConfigurations(TestRecorderAgentInitializer.class);
		return Tasks.of(new ComputeSourceCode(setup, matcher, initializers), new ComputeCoverage());
	}

	@Test
	void testNotPrime() throws Exception {
		ContextSnapshot snapNotPrime = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(4));
		TestCase testNotPrime = new TestCase(snapNotPrime);
		coverage().process(testNotPrime);

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
		ContextSnapshot snap5 = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(5));
		TestCase test5 = new TestCase(snap5);
		coverage().process(test5);

		ContextSnapshot snap7 = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(7));
		TestCase test7 = new TestCase(snap7);
		coverage().process(test7);

		Coverage coverage5 = ComputeCoverage.COVERAGE.from(test5).orElseThrow(RuntimeException::new);
		Coverage coverage7 = ComputeCoverage.COVERAGE.from(test7).orElseThrow(RuntimeException::new);
		assertThat(coverage5).isEqualTo(coverage7);
	}

	@Test
	void testNotEquals() throws Exception {
		ContextSnapshot snap4 = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(4));
		TestCase test4 = new TestCase(snap4);
		coverage().process(test4);

		ContextSnapshot snap2 = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(2));
		TestCase test2 = new TestCase(snap2);
		coverage().process(test2);

		ContextSnapshot snap5 = createTestTargetSnapshot(new TestTarget(), false, Integer.valueOf(5));
		TestCase test5 = new TestCase(snap5);
		coverage().process(test5);

		Coverage coverage4 = ComputeCoverage.COVERAGE.from(test4).orElseThrow(RuntimeException::new);
		Coverage coverage2 = ComputeCoverage.COVERAGE.from(test2).orElseThrow(RuntimeException::new);
		Coverage coverage5 = ComputeCoverage.COVERAGE.from(test5).orElseThrow(RuntimeException::new);

		assertThat(coverage4).withFailMessage("branch coverage should be different").isNotEqualTo(coverage2);
		assertThat(coverage4).withFailMessage("instruction coverage should be different").isNotEqualTo(coverage5);
	}

	private ContextSnapshot createTestTargetSnapshot(TestTarget thisObject, boolean resultObject, Integer argObject) {
		ContextSnapshot snap = new ContextSnapshot(0, "id" + UUID.randomUUID(),
			MethodSignature.fromDescriptor(TestTarget.class.getName().replace('.', '/'), "test", "(Ljava/lang/Integer;)Z"));

		SerializedValue serializedThisObject = facade.serialize(TestTarget.class, thisObject, session);
		SerializedValue serializedArgObject = facade.serialize(Integer.class, argObject, session);
		SerializedValue serializedResultObject = facade.serialize(boolean.class, resultObject, session);

		snap.setSetupThis(serializedThisObject);
		snap.setSetupArgs(serializedArgObject);
		snap.setSetupGlobals();

		snap.setExpectThis(serializedThisObject);
		snap.setExpectArgs(serializedArgObject);
		snap.setExpectGlobals();
		snap.setExpectResult(serializedResultObject);

		return snap;
	}

}
