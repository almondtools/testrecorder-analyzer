package net.amygdalum.testrecorder.analyzer.query;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordGetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordInsert;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordReadLowerCase;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetGlobal;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordWriteUpperCase;
import static net.amygdalum.testrecorder.analyzer.query.ContextSnapshotPredicate.onSetupThis;
import static net.amygdalum.testrecorder.analyzer.query.TestCasePredicate.onContextSnapshot;
import static net.amygdalum.testrecorder.analyzer.query.TestCasePredicate.onProperty;
import static net.amygdalum.testrecorder.analyzer.query.TestCaseQuery.query;
import static net.amygdalum.testrecorder.analyzer.query.TestCaseUpdate.properties;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.analyzer.Serialization;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.TestDatabase;
import net.amygdalum.testrecorder.analyzer.TestTestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.TestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.util.testobjects.Bean;
import net.amygdalum.testrecorder.util.testobjects.GenericCycle;
import net.amygdalum.testrecorder.util.testobjects.In;
import net.amygdalum.testrecorder.util.testobjects.InputOutput;
import net.amygdalum.testrecorder.util.testobjects.Odd;
import net.amygdalum.testrecorder.util.testobjects.Static;

public class TestDatabaseUpdateTest {

	private static TestrecorderAnalyzerConfig config;
	private static Serialization serialization;

	private static TestDatabase database;

	@BeforeAll
	static void beforeAll() throws Exception {
		config = new TestTestrecorderAnalyzerConfig();
		serialization = new Serialization();
	}

	@BeforeEach
	void before() throws Exception {
		database = setupTestDatabase(config, serialization);
	}

	@AfterEach
	void after() throws Exception {
		database.close();
	}

	private static TestDatabase setupTestDatabase(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);

		database.store(new TestCase(recordSetAttribute(new Bean(), "str")));
		database.store(new TestCase(recordGetAttribute(new Bean())));

		database.store(new TestCase(recordTest(new In(42, 43, 44), 41)));
		database.store(new TestCase(recordTest(new In(42, 43, 44), 43)));

		GenericCycle<String> recursive1 = GenericCycle.recursive("element1");
		GenericCycle<String> recursive2 = GenericCycle.recursive("element2");
		database.store(new TestCase(recordInsert(recursive1, recursive1)));
		database.store(new TestCase(recordInsert(recursive1, recursive2)));

		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(4))));
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(5))));

		database.store(new TestCase(recordSetGlobal(Static.class, "global")));
		database.store(new TestCase(recordSetGlobal(Static.class, null)));

		database.store(new TestCase(recordReadLowerCase(new InputOutput(), "Hello In")));
		database.store(new TestCase(recordWriteUpperCase(new InputOutput(), "Hello Out")));

		return database;
	}

	@Test
	void testUpdateAll() throws Exception {
		database.update(properties(new TestProperty("test")));

		List<TestCase> result = database.fetch(query(onProperty(TestProperty.TEST, value -> value.equals("test"))))
			.collect(toList());

		assertThat(result)
			.hasSize(12)
			.allMatch(testCase -> testCase.get(TestProperty.TEST).map(v -> v.equals("test")).orElse(false));
	}

	@Test
	void testUpdateSome() throws Exception {
		database.update(properties(new TestProperty("test")).where(onContextSnapshot(onSetupThis(value -> false, true))));

		List<TestCase> result = database.fetch(query(onProperty(TestProperty.TEST, value -> value.equals("test"))))
			.collect(toList());

		assertThat(result)
			.hasSize(2)
			.allMatch(testCase -> testCase.get(TestProperty.TEST).map(v -> v.equals("test")).orElse(false));
	}

}
