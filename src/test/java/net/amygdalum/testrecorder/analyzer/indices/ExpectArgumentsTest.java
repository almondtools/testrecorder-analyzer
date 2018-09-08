package net.amygdalum.testrecorder.analyzer.indices;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.indices.ExpectArguments.expectArgumentAt;
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
import net.amygdalum.testrecorder.analyzer.selectors.ExpectArgEquals;
import net.amygdalum.testrecorder.analyzer.testobjects.Odd;
import net.amygdalum.testrecorder.analyzer.testobjects.Positive;
import net.amygdalum.testrecorder.values.SerializedLiteral;

@ExtendWith(MultipleProfiles.class)
public class ExpectArgumentsTest {

	private static final ExpectArguments<Float> FLOAT_INDEX = expectArgumentAt(0).ofType(Float.class);
	private static final ExpectArguments<Integer> INTEGER_INDEX = expectArgumentAt(0).ofType(Integer.class);

	private static TestrecorderAnalyzerConfig config;
	private static Serialization serialization;

	@ProfileData(displayName = "raw data")
	private static TestDatabase database;
	@ProfileData(displayName = "with integer index")
	private static TestDatabase databaseWithIntegerIndex;
	@ProfileData(displayName = "with float index")
	private static TestDatabase databaseWithFloatIndex;
	@ProfileData(displayName = "with multiple index")
	private static TestDatabase databaseWithIndex;

	@BeforeAll
	static void before() throws Exception {
		config = new TestTestrecorderAnalyzerConfig();
		serialization = new Serialization();

		database = setupDatabase(config, serialization);
		databaseWithIntegerIndex = setupDatabaseWithIntegerIndex(config, serialization);
		databaseWithFloatIndex = setupDatabaseWithFloatIndex(config, serialization);
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
		database.prepareIndexOn(FLOAT_INDEX);

		fillDatabase(database);

		return database;
	}

	private static TestDatabase setupDatabaseWithIntegerIndex(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(INTEGER_INDEX);

		fillDatabase(database);

		return database;
	}

	private static TestDatabase setupDatabaseWithFloatIndex(TestrecorderAnalyzerConfig config, Serialization serialization) throws Exception {
		TestDatabase database = new TestDatabase(config, serialization);
		database.prepareIndexOn(FLOAT_INDEX);

		fillDatabase(database);

		return database;
	}

	private static void fillDatabase(TestDatabase database) throws Exception {
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(2))));
		database.store(new TestCase(recordTest(Odd.odd(), Integer.valueOf(3))));
		database.store(new TestCase(recordTest(Positive.positive(), Float.valueOf(-2f))));
		database.store(new TestCase(recordTest(Positive.positive(), Float.valueOf(0.3f))));
	}

	@ProfiledTest
	void testIntegerSelection(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArgEquals<>(0, 2, INTEGER_INDEX)))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allSatisfy(testcase -> assertThat(SerializedValueWalker.start(testcase.getSnapshot().getExpectArgs(), 0)
				.forLiteral(SerializedLiteral::getValue)
				.orFail(e -> new RuntimeException(e))).isEqualTo(2));
	}

	@ProfiledTest
	void testIntegerSelectionUnindexed(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArgEquals<>(0, 4, INTEGER_INDEX)))
			.collect(toList());

		assertThat(cases).isEmpty();
	}

	@ProfiledTest
	void testFloatSelection(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArgEquals<>(0, 0.3f, FLOAT_INDEX)))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allSatisfy(testcase -> assertThat(SerializedValueWalker.start(testcase.getSnapshot().getExpectArgs(), 0)
				.forLiteral(SerializedLiteral::getValue)
				.orFail(e -> new RuntimeException(e))).isEqualTo(0.3f));
	}

	@ProfiledTest
	void testFloatSelectionUnindexed(TestDatabase database) throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArgEquals<>(0, 0.4f, FLOAT_INDEX)))
			.collect(toList());

		assertThat(cases).isEmpty();
	}

}
