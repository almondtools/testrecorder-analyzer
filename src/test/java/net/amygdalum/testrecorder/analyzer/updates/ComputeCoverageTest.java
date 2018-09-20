package net.amygdalum.testrecorder.analyzer.updates;

import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.TestAgentConfiguration.defaultConfig;
import static net.amygdalum.testrecorder.analyzer.testobjects.In.in;
import static net.amygdalum.testrecorder.analyzer.testobjects.Odd.odd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.analyzer.Coverage;
import net.amygdalum.testrecorder.analyzer.PropertyUpdates;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.deserializers.Adaptors;
import net.amygdalum.testrecorder.deserializers.CustomAnnotation;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerator;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerator;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.generator.DefaultTestGeneratorProfile;
import net.amygdalum.testrecorder.generator.TestGeneratorProfile;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class ComputeCoverageTest {

	private AgentConfiguration config;

	@BeforeEach
	void before() throws Exception {
		config = defaultConfig();
	}

	@Test
	void testOdd() throws Exception {
		ContextSnapshot oddSlice = recordTest(odd(), Integer.valueOf(7));
		TestCase oddTest = new TestCase(oddSlice);

		coverage(config).apply(oddTest);

		assertThat(ComputeCoverage.COVERAGE.from(oddTest)).hasValueSatisfying(coverage -> {
			assertThat(coverage.getMethods()).hasSize(4);
			assertThat(coverage.getLineCoverage(9)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isTrue();
			});
			assertThat(coverage.getLineCoverage(11)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isFalse();
			});
		});
	}

	@Test
	void testEven() throws Exception {
		ContextSnapshot evenSlice = recordTest(odd(), Integer.valueOf(4));
		TestCase evenTest = new TestCase(evenSlice);

		coverage(config).apply(evenTest);

		assertThat(ComputeCoverage.COVERAGE.from(evenTest)).hasValueSatisfying(coverage -> {
			assertThat(coverage.getMethods()).hasSize(4);
			assertThat(coverage.getLineCoverage(9)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isFalse();
			});
			assertThat(coverage.getLineCoverage(11)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isTrue();
			});
		});
	}

	@Test
	void testEqualCoverageSlices() throws Exception {
		ContextSnapshot oddSlice = recordTest(odd(), Integer.valueOf(5));
		TestCase oddTest = new TestCase(oddSlice);

		coverage(config).apply(oddTest);

		ContextSnapshot otherOddSlice = recordTest(odd(), Integer.valueOf(7));
		TestCase otherOddTest = new TestCase(otherOddSlice);

		coverage(config).apply(otherOddTest);

		Coverage oddCoverage = ComputeCoverage.COVERAGE.from(oddTest).orElseThrow(RuntimeException::new);
		Coverage otherOddCoverage = ComputeCoverage.COVERAGE.from(otherOddTest).orElseThrow(RuntimeException::new);
		assertThat(oddCoverage).isEqualTo(otherOddCoverage);
	}

	@Test
	void testDifferentCoverageSlice() throws Exception {
		ContextSnapshot evenSlice = recordTest(odd(), Integer.valueOf(4));
		TestCase evenTest = new TestCase(evenSlice);

		coverage(config).apply(evenTest);

		ContextSnapshot oddSlice = recordTest(odd(), Integer.valueOf(5));
		TestCase oddTest = new TestCase(oddSlice);

		coverage(config).apply(oddTest);

		Coverage evenCoverage = ComputeCoverage.COVERAGE.from(evenTest).orElseThrow(RuntimeException::new);
		Coverage oddCoverage = ComputeCoverage.COVERAGE.from(oddTest).orElseThrow(RuntimeException::new);

		assertThat(evenCoverage).withFailMessage("instruction coverage should be different").isNotEqualTo(oddCoverage);
		assertThat(oddCoverage.getMethodCoverage("isOdd(I)Z")).hasValueSatisfying(method -> {
			assertThat(method.getInstructionCoverage().ratio()).isCloseTo(7f/9f, within(0.001f));
			assertThat(method.getBranchCoverage().ratio()).isCloseTo(1f/2f, within(0.001f));
			assertThat(method.getLineCoverage(9)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isTrue();
			});
		});
		assertThat(evenCoverage.getMethodCoverage("isOdd(I)Z")).hasValueSatisfying(method -> {
			assertThat(method.getInstructionCoverage().ratio()).isCloseTo(7f/9f, within(0.001f));
			assertThat(method.getBranchCoverage().ratio()).isCloseTo(1f/2f, within(0.001f));
			assertThat(method.getLineCoverage(9)).hasValueSatisfying(line -> {
				assertThat(line.isCovered()).isFalse();
			});
		});
	}

	@Test
	void testDifferentBranchCoverageSlice() throws Exception {
		ContextSnapshot firstElementSlice = recordTest(in(42, 43), Integer.valueOf(42));
		TestCase firstElementTest = new TestCase(firstElementSlice);

		coverage(config).apply(firstElementTest);

		ContextSnapshot secondElementSlice = recordTest(in(42, 43), Integer.valueOf(43));
		TestCase secondElementTest = new TestCase(secondElementSlice);

		coverage(config).apply(secondElementTest);

		Coverage firstElementCoverage = ComputeCoverage.COVERAGE.from(firstElementTest).orElseThrow(RuntimeException::new);
		Coverage secondElementCoverage = ComputeCoverage.COVERAGE.from(secondElementTest).orElseThrow(RuntimeException::new);

		assertThat(firstElementCoverage).withFailMessage("branch coverage should be different").isNotEqualTo(secondElementCoverage);
		assertThat(firstElementCoverage.getMethodCoverage("isIn(Ljava/lang/Integer;)Z")).hasValueSatisfying(method -> {
			assertThat(method.getInstructionCoverage().ratio()).isCloseTo(18f/20f, within(0.001f));
			assertThat(method.getBranchCoverage().ratio()).isCloseTo(2f/4f, within(0.001f));
		});
		assertThat(secondElementCoverage.getMethodCoverage("isIn(Ljava/lang/Integer;)Z")).hasValueSatisfying(method -> {
			assertThat(method.getInstructionCoverage().ratio()).isCloseTo(18f/20f, within(0.001f));
			assertThat(method.getBranchCoverage().ratio()).isCloseTo(3f/4f, within(0.001f));
		});
	}

	private PropertyUpdates coverage(AgentConfiguration config) {
		SetupGenerators setup = new SetupGenerators(new Adaptors().load(config.loadConfigurations(SetupGenerator.class)));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors().load(config.loadConfigurations(MatcherGenerator.class)));
		List<TestRecorderAgentInitializer> initializers = config.loadConfigurations(TestRecorderAgentInitializer.class);
		List<CustomAnnotation> annotations = config.loadOptionalConfiguration(TestGeneratorProfile.class).orElseGet(DefaultTestGeneratorProfile::new).annotations();
		return PropertyUpdates.inSequence(new ComputeSourceCode(setup, matcher, initializers, annotations), new ComputeCoverage()); 
	}

}
