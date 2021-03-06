package net.amygdalum.testrecorder.analyzer.request;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordAreEqual;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordGetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordInsert;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordReadLowerCase;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetGlobal;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordWriteUpperCase;
import static net.amygdalum.testrecorder.analyzer.TestAgentConfiguration.defaultConfig;
import static net.amygdalum.testrecorder.analyzer.collectors.CoverageEquivalence.distinctCoverage;
import static net.amygdalum.testrecorder.analyzer.indices.ExpectArguments.expectArgumentAt;
import static net.amygdalum.testrecorder.analyzer.indices.SetupArguments.setupArgumentAt;
import static net.amygdalum.testrecorder.analyzer.request.TestCaseQuery.query;
import static net.amygdalum.testrecorder.analyzer.request.TestCaseUpdate.properties;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.analyzer.Serialization;
import net.amygdalum.testrecorder.analyzer.SerializedValueWalker;
import net.amygdalum.testrecorder.analyzer.TestAgentConfiguration;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.TestDatabase;
import net.amygdalum.testrecorder.analyzer.TestTestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.TestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.selectors.ExpectArgEquals;
import net.amygdalum.testrecorder.analyzer.selectors.SetupArg;
import net.amygdalum.testrecorder.analyzer.selectors.SetupArgEquals;
import net.amygdalum.testrecorder.analyzer.selectors.SetupThis;
import net.amygdalum.testrecorder.analyzer.testobjects.Bean;
import net.amygdalum.testrecorder.analyzer.testobjects.FloatComparator;
import net.amygdalum.testrecorder.analyzer.testobjects.GenericCycle;
import net.amygdalum.testrecorder.analyzer.testobjects.In;
import net.amygdalum.testrecorder.analyzer.testobjects.InputOutput;
import net.amygdalum.testrecorder.analyzer.testobjects.Odd;
import net.amygdalum.testrecorder.analyzer.testobjects.Static;
import net.amygdalum.testrecorder.analyzer.updates.ComputeCoverage;
import net.amygdalum.testrecorder.analyzer.updates.ComputeSourceCode;
import net.amygdalum.testrecorder.deserializers.Adaptors;
import net.amygdalum.testrecorder.deserializers.CustomAnnotation;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerator;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerator;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.generator.DefaultTestGeneratorProfile;
import net.amygdalum.testrecorder.generator.TestGeneratorProfile;

public class TestDatabaseEnhancedQueryTest {

	private static TestrecorderAnalyzerConfig config;
	private static Serialization serialization;

	private static ComputeSourceCode sourceCode;
	private static ComputeCoverage coverage;
	private static TestDatabase database;

	@BeforeAll
	static void before() throws Exception {
		config = new TestTestrecorderAnalyzerConfig();
		serialization = new Serialization();

		sourceCode = computeSourceCode();
		coverage = computeCoverage();

		database = setupTestDatabase(config, serialization);
	}

	private static ComputeSourceCode computeSourceCode() {
		TestAgentConfiguration config = defaultConfig();
		SetupGenerators setup = new SetupGenerators(new Adaptors().load(config.loadConfigurations(SetupGenerator.class)));
		MatcherGenerators matcher = new MatcherGenerators(new Adaptors().load(config.loadConfigurations(MatcherGenerator.class)));
		List<CustomAnnotation> annotations = config.loadOptionalConfiguration(TestGeneratorProfile.class).orElseGet(DefaultTestGeneratorProfile::new).annotations();
		return new ComputeSourceCode(setup, matcher, annotations);
	}

	private static ComputeCoverage computeCoverage() {
		return new ComputeCoverage();
	}

	@AfterAll
	static void after() throws Exception {
		database.close();
	}

	private static TestDatabase setupTestDatabase(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(setupArgumentAt(0).ofType(Float.class));
		database.prepareIndexOn(expectArgumentAt(1).ofType(Float.class));

		database.store(new TestCase(recordSetAttribute(new Bean(), "str")));
		database.store(new TestCase(recordGetAttribute(new Bean())));

		database.store(new TestCase(recordTest(new In(42, 43, 44), 41)));
		database.store(new TestCase(recordTest(new In(42, 43, 44), 43)));

		GenericCycle<String> recursive1 = GenericCycle.recursive("element1");
		GenericCycle<String> recursive2 = GenericCycle.recursive("element2");
		database.store(new TestCase(recordInsert(recursive1, recursive1)));
		database.store(new TestCase(recordInsert(recursive1, recursive2)));

		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(2))));
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(3))));
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(4))));
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(5))));

		database.store(new TestCase(recordAreEqual(new FloatComparator(0.01f), 1f, 1f)));
		database.store(new TestCase(recordAreEqual(new FloatComparator(0.01f), 1f, 1.005f)));
		database.store(new TestCase(recordAreEqual(new FloatComparator(0.01f), 1f, 1.02f)));
		database.store(new TestCase(recordAreEqual(new FloatComparator(0.05f), 1f, 1.02f)));

		database.store(new TestCase(recordSetGlobal(Static.class, "global")));
		database.store(new TestCase(recordSetGlobal(Static.class, null)));

		database.store(new TestCase(recordReadLowerCase(new InputOutput(), "Hello In")));
		database.store(new TestCase(recordWriteUpperCase(new InputOutput(), "Hello Out")));

		database.update(properties(sourceCode, coverage));

		return database;
	}

	@Test
	void testSelectionCollectingAll() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThis(value -> new SerializedValueWalker(value)
			.forObject(v -> v.getType() == Odd.class)
			.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(4)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == Odd.class);
	}

	@Test
	void testSelectionCollectingPartial() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThis(value -> new SerializedValueWalker(value)
			.forObject(v -> v.getType() == Odd.class)
			.andRecover(false)))
				.and(new SetupArg(0, value -> new SerializedValueWalker(value)
					.forLiteral(v -> ((Integer) v.getValue()).intValue() < 5)
					.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(3)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == Odd.class);
	}

	@Test
	void testSelectionCollectingIndexedPartial() throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArgEquals<>(1, 1.02f, expectArgumentAt(1).ofType(Float.class))))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == FloatComparator.class);
	}

	@Test
	void testSelectionCollectingDoubleIndexed1Partial() throws Exception {
		List<TestCase> cases = database.fetch(
			query(new ExpectArgEquals<>(1, 1.02f, expectArgumentAt(1).ofType(Float.class)))
				.and(new SetupArgEquals<>(0, 1f, setupArgumentAt(0).ofType(Float.class))))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == FloatComparator.class);
	}

	@Test
	void testSelectionCollectingDoubleIndexed2Partial() throws Exception {
		List<TestCase> cases = database.fetch(
			query(new SetupArgEquals<>(0, 1f, setupArgumentAt(0).ofType(Float.class)))
				.and(new ExpectArgEquals<>(1, 1.02f, expectArgumentAt(1).ofType(Float.class))))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == FloatComparator.class);
	}

	@Test
	void testSelectionCollectingDistinctCoverage() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThis(value -> new SerializedValueWalker(value)
			.forObject(v -> v.getType() == Odd.class)
			.andRecover(false)))
				.collecting(distinctCoverage()))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == Odd.class);
	}

}
