package net.amygdalum.testrecorder.analyzer.indices;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordNext;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetAttribute;
import static net.amygdalum.testrecorder.analyzer.indices.SetupThis.setupThisApplying;
import static net.amygdalum.testrecorder.analyzer.request.TestCaseQuery.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import net.amygdalum.testrecorder.analyzer.Serialization;
import net.amygdalum.testrecorder.analyzer.SerializedValueWalker;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.TestDatabase;
import net.amygdalum.testrecorder.analyzer.TestTestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.TestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.selectors.SetupThisEquals;
import net.amygdalum.testrecorder.analyzer.testobjects.Bean;
import net.amygdalum.testrecorder.analyzer.testobjects.IntCounter;
import net.amygdalum.testrecorder.values.SerializedLiteral;

@ExtendWith(MultipleProfiles.class)
public class SetupThisTest {

	private static final SetupThis<String> STRING_INDEX = setupThisApplying(".attribute", String.class);
	private static final SetupThis<Integer> INTEGER_INDEX = setupThisApplying(".value", Integer.class);

	private static TestrecorderAnalyzerConfig config;
	private static Serialization serialization;

	@ProfileData(displayName = "raw data")
	private static TestDatabase database;
	@ProfileData(displayName = "with integer index")
	private static TestDatabase databaseWithIntegerIndex;
	@ProfileData(displayName = "with String index")
	private static TestDatabase databaseWithStringIndex;
	@ProfileData(displayName = "with multiple index")
	private static TestDatabase databaseWithIndex;

	@BeforeAll
	static void before() throws Exception {
		config = new TestTestrecorderAnalyzerConfig();
		serialization = new Serialization();

		database = setupDatabase(config, serialization);
		databaseWithIntegerIndex = setupDatabaseWithIntegerIndex(config, serialization);
		databaseWithStringIndex = setupDatabaseWithStringIndex(config, serialization);
		databaseWithIndex = setupDatabaseWithMultiIndex(config, serialization);
	}

	@AfterAll
	static void after() throws Exception {
		if (database != null) {
			database.close();
		}
		if (databaseWithIndex != null) {
			databaseWithIndex.close();
		}
	}

	private static TestDatabase setupDatabase(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);

		fillDatabase(database);

		return database;
	}

	private static TestDatabase setupDatabaseWithMultiIndex(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(INTEGER_INDEX);
		database.prepareIndexOn(STRING_INDEX);

		fillDatabase(database);

		return database;
	}

	private static TestDatabase setupDatabaseWithIntegerIndex(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(INTEGER_INDEX);

		fillDatabase(database);

		return database;
	}

	private static TestDatabase setupDatabaseWithStringIndex(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(STRING_INDEX);

		fillDatabase(database);

		return database;
	}

	private static void fillDatabase(TestDatabase database) throws Exception {
		database.store(new TestCase(recordSetAttribute(new Bean(), "value1")));
		database.store(new TestCase(recordSetAttribute(new Bean(), "value2")));
		database.store(new TestCase(recordNext(new IntCounter())));
		database.store(new TestCase(recordNext(new IntCounter(2))));
	}

	@ProfiledTest
	void testStringSelection(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThisEquals<>(".attribute", null, STRING_INDEX)))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allSatisfy(testcase -> assertThat(new SerializedValueWalker(testcase.getSnapshot().getSetupThis())
				.field("attribute")
				.forNull(nullValue -> (String) null)
				.orFail(e -> new RuntimeException(e))).isEqualTo(null));
	}

	@ProfiledTest
	void testStringSelectionUnindexed(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThisEquals<>(".attribute", "value1", STRING_INDEX)))
			.collect(toList());
		
		assertThat(cases).isEmpty();
	}
	
	@ProfiledTest
	void testIntegerSelection(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThisEquals<>(".value", 2, INTEGER_INDEX)))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allSatisfy(testcase -> assertThat(new SerializedValueWalker(testcase.getSnapshot().getSetupThis())
				.field("value")
				.forLiteral(SerializedLiteral::getValue)
				.orFail(e -> new RuntimeException(e))).isEqualTo(2));
	}

	@ProfiledTest
	void testIntegerSelectionUnindexed(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThisEquals<>(".value", 3, INTEGER_INDEX)))
			.collect(toList());
		
		assertThat(cases).isEmpty();
	}
	
}
