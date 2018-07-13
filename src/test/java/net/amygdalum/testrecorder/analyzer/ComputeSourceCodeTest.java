package net.amygdalum.testrecorder.analyzer;


import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.TestAgentConfiguration.defaultConfig;
import static net.amygdalum.testrecorder.util.testobjects.In.in;
import static net.amygdalum.testrecorder.util.testobjects.Inner.negate;
import static net.amygdalum.testrecorder.util.testobjects.Odd.odd;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.deserializers.Adaptors;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerator;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerator;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;

public class ComputeSourceCodeTest {

	private AgentConfiguration config;

	@BeforeEach
	void before() throws Exception {
		config = defaultConfig();
	}

	@Test
	void testOdd() throws Exception {
		ContextSnapshot oddSlice = recordTest(odd(), Integer.valueOf(7));
		TestCase oddTest = new TestCase(oddSlice);

		source(config).apply(oddTest);

		assertThat(ComputeSourceCode.SOURCE.from(oddTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.Odd");
			assertThat(source.getCode()).containsSubsequence(
				"class OddTest",
				"void testTest0()",
				"odd1.test(7);");
		});
	}

	@Test
	void testEven() throws Exception {
		ContextSnapshot evenSlice = recordTest(odd(), Integer.valueOf(6));
		TestCase evenTest = new TestCase(evenSlice);

		source(config).apply(evenTest);

		assertThat(ComputeSourceCode.SOURCE.from(evenTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.Odd");
			assertThat(source.getCode()).containsSubsequence(
				"class OddTest",
				"void testTest0()",
				"odd1.test(6);");
		});
	}

	@Test
	void testIn() throws Exception {
		ContextSnapshot inSlice = recordTest(in(42, 43), Integer.valueOf(42));
		TestCase inTest = new TestCase(inSlice);

		source(config).apply(inTest);

		assertThat(ComputeSourceCode.SOURCE.from(inTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.In");
			assertThat(source.getCode()).containsSubsequence(
				"class InTest",
				"void testTest0()",
				"in", ".test(42);");
		});
	}

	@Test
	void testNotIn() throws Exception {
		ContextSnapshot inSlice = recordTest(in(42, 43), Integer.valueOf(41));
		TestCase inTest = new TestCase(inSlice);

		source(config).apply(inTest);

		assertThat(ComputeSourceCode.SOURCE.from(inTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.In");
			assertThat(source.getCode()).containsSubsequence(
				"class InTest",
				"void testTest0()",
				"in", ".test(41);");
		});
	}

	@Test
	void testInner() throws Exception {
		ContextSnapshot innerSlice = recordTest(negate(), true);
		TestCase innerTest = new TestCase(innerSlice);

		source(config).apply(innerTest);

		assertThat(ComputeSourceCode.SOURCE.from(innerTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.Inner");
			assertThat(source.getCode()).containsSubsequence(
				"class InnerTest",
				"void testTest0()",
				"negate", ".test(true);",
				"assertThat", "equalTo(false)");
		});
	}

	private PropertyUpdate source(AgentConfiguration config) {
		SetupGenerators setup = new SetupGenerators(new Adaptors<SetupGenerators>(config).load(SetupGenerator.class));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors<MatcherGenerators>(config).load(MatcherGenerator.class));
		List<TestRecorderAgentInitializer> initializers = config.loadConfigurations(TestRecorderAgentInitializer.class);
		return new ComputeSourceCode(setup, matcher, initializers);
	}

}
