package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.analyzer.TestAgentConfiguration.defaultConfig;
import static net.amygdalum.testrecorder.util.testobjects.In.in;
import static net.amygdalum.testrecorder.util.testobjects.Inner.negate;
import static net.amygdalum.testrecorder.util.testobjects.Odd.odd;
import static org.assertj.core.api.Assertions.assertThat;

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
import net.amygdalum.testrecorder.util.testobjects.In;
import net.amygdalum.testrecorder.util.testobjects.Inner;
import net.amygdalum.testrecorder.util.testobjects.Odd;

public class ComputeSourceCodeTest {

	private AgentConfiguration config;

	@BeforeEach
	void before() throws Exception {
		config = defaultConfig();
	}

	@Test
	void testOdd() throws Exception {
		ContextSnapshot oddSlice = createSnapshot(odd(), Integer.valueOf(7));
		TestCase oddTest = new TestCase(oddSlice);

		source(config).process(oddTest);

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
		ContextSnapshot evenSlice = createSnapshot(odd(), Integer.valueOf(6));
		TestCase evenTest = new TestCase(evenSlice);

		source(config).process(evenTest);

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
		ContextSnapshot inSlice = createSnapshot(in(42, 43), Integer.valueOf(42));
		TestCase inTest = new TestCase(inSlice);

		source(config).process(inTest);

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
		ContextSnapshot inSlice = createSnapshot(in(42, 43), Integer.valueOf(41));
		TestCase inTest = new TestCase(inSlice);

		source(config).process(inTest);

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
		ContextSnapshot innerSlice = createSnapshot(negate(), true);
		TestCase innerTest = new TestCase(innerSlice);

		source(config).process(innerTest);

		assertThat(ComputeSourceCode.SOURCE.from(innerTest)).hasValueSatisfying(source -> {
			assertThat(source.getUnitName()).isEqualTo("net.amygdalum.testrecorder.util.testobjects.Inner");
			assertThat(source.getCode()).containsSubsequence(
				"class InnerTest",
				"void testTest0()",
				"negate", ".test(true);",
				"assertThat", "equalTo(false)");
		});
	}

	private UpdateProcess source(AgentConfiguration config) {
		SetupGenerators setup = new SetupGenerators(new Adaptors<SetupGenerators>(config).load(SetupGenerator.class));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors<MatcherGenerators>(config).load(MatcherGenerator.class));
		List<TestRecorderAgentInitializer> initializers = config.loadConfigurations(TestRecorderAgentInitializer.class);
		return new ComputeSourceCode(setup, matcher, initializers);
	}

	private ContextSnapshot createSnapshot(Odd thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = { Integer.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(Odd.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	private ContextSnapshot createSnapshot(In thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = { Integer.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(In.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	private ContextSnapshot createSnapshot(Inner.Negate thisObject, Boolean argObject) throws Exception {
		Class<?>[] argtypes = { Boolean.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(Inner.Negate.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}
}
