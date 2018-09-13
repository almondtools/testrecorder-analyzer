package net.amygdalum.testrecorder.analyzer.request;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordGetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordInsert;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordReadLowerCase;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetAttribute;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordSetGlobal;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordTest;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordWriteUpperCase;
import static net.amygdalum.testrecorder.analyzer.request.TestCaseQuery.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.analyzer.Serialization;
import net.amygdalum.testrecorder.analyzer.SerializedValueWalker;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.TestDatabase;
import net.amygdalum.testrecorder.analyzer.TestTestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.TestrecorderAnalyzerConfig;
import net.amygdalum.testrecorder.analyzer.selectors.ExpectArg;
import net.amygdalum.testrecorder.analyzer.selectors.ExpectOutput;
import net.amygdalum.testrecorder.analyzer.selectors.ExpectThis;
import net.amygdalum.testrecorder.analyzer.selectors.OnGlobal;
import net.amygdalum.testrecorder.analyzer.selectors.SetupArg;
import net.amygdalum.testrecorder.analyzer.selectors.SetupInput;
import net.amygdalum.testrecorder.analyzer.selectors.SetupThis;
import net.amygdalum.testrecorder.analyzer.testobjects.Bean;
import net.amygdalum.testrecorder.analyzer.testobjects.GenericCycle;
import net.amygdalum.testrecorder.analyzer.testobjects.In;
import net.amygdalum.testrecorder.analyzer.testobjects.InputOutput;
import net.amygdalum.testrecorder.analyzer.testobjects.Odd;
import net.amygdalum.testrecorder.analyzer.testobjects.Static;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.values.SerializedLiteral;

public class TestDatabaseQueryTest {

	private static TestrecorderAnalyzerConfig config;
	private static Serialization serialization;

	private static TestDatabase database;

	@BeforeAll
	static void before() throws Exception {
		config = new TestTestrecorderAnalyzerConfig();
		serialization = new Serialization();
		database = setupTestDatabase(config, serialization);
	}

	@AfterAll
	static void after() throws Exception {
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
	void testSelectionOnSetupThis() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupThis(value -> new SerializedValueWalker(value)
		.field("attribute")
		.forNull(v -> true)
		.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == Bean.class);
	}

	@Test
	void testSelectionOnExpectThis() throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectThis(value -> new SerializedValueWalker(value)
		.field("attribute")
		.forLiteral(v -> v.getValue().equals("str"))
		.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == Bean.class);
	}

	@Test
	void testSelectionOnSetupArgument() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupArg(0, value -> new SerializedValueWalker(value)
		.forLiteral(v -> v.getValue().equals(43))
		.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == In.class)
			.allSatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getSetupArgs(), 0)
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo(43);
			});
	}

	@Test
	void testSelectionOnSetupArgumentChanging() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupArg(0, value -> new SerializedValueWalker(value)
		.forObject(v -> v.getType() == GenericCycle.class)
		.andRecover(false)))
				.and(new SetupArg(0, value -> new SerializedValueWalker(value)
				.field("next")
				.field("a")
				.forLiteral(v -> v.getValue().equals("element2"))
				.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allSatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getSetupArgs(), 0)
					.field("next")
					.field("a")
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo("element2");
			});
	}

	@Test
	void testSelectionOnExpectArgument() throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArg(0, value -> new SerializedValueWalker(value)
		.forLiteral(v -> v.getValue().equals(43))
		.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.allMatch(c -> c.getSnapshot().getSetupThis().getType() == In.class)
			.allSatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getSetupArgs(), 0)
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo(43);
			});
	}

	@Test
	void testSelectionOnExpectArgumentChanging() throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectArg(0, value -> new SerializedValueWalker(value)
		.forObject(v -> v.getType() == GenericCycle.class)
		.andRecover(false)))
				.and(new ExpectArg(0, value -> new SerializedValueWalker(value)
				.field("next")
				.field("a")
				.forLiteral(v -> v.getValue().equals("element1"))
				.andRecover(false))))
			.collect(toList());

		assertThat(cases)
			.hasSize(2)
			.allSatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getExpectArgs(), 0)
					.field("next")
					.field("a")
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo("element1");
			})
			.anySatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getSetupArgs(), 0)
					.field("next")
					.field("a")
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo("element1");
			})
			.anySatisfy(c -> {
				Object value = SerializedValueWalker.start(c.getSnapshot().getSetupArgs(), 0)
					.field("next")
					.field("a")
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(value).isEqualTo("element2");
			});
	}

	@Test
	void testSelectionOnSetupExpectGlobals() throws Exception {
		List<TestCase> cases = database.fetch(query(new OnGlobal(Static.class, "global", (before, after) -> {
			SerializedValue globalBeforeValue = before.getValue();
			SerializedValue globalAfterValue = after.getValue();
			Object globalBefore = globalBeforeValue instanceof SerializedLiteral ? ((SerializedLiteral) globalBeforeValue).getValue() : null;
			Object globalAfter = globalAfterValue instanceof SerializedLiteral ? ((SerializedLiteral) globalAfterValue).getValue() : null;
			return !Objects.equals(globalBefore, globalAfter);
		})))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.anySatisfy(c -> {
				Object setupvalue = SerializedValueWalker.start(c.getSnapshot().getSetupGlobals(), 0)
					.forNull(v -> null)
					.orFail(RuntimeException::new);
				assertThat(setupvalue).isNull();

				Object expectvalue = SerializedValueWalker.start(c.getSnapshot().getExpectGlobals(), 0)
					.forLiteral(SerializedLiteral::getValue)
					.orFail(RuntimeException::new);
				assertThat(expectvalue).isEqualTo("global");
			});
	}

	@Test
	void testSelectionOnSetupInput() throws Exception {
		List<TestCase> cases = database.fetch(query(new SetupInput(setupInput -> !setupInput.isEmpty())))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.anySatisfy(c -> {
				String input = c.getSnapshot().getSetupInput().stream()
					.map(i -> new SerializedValueWalker(i.getResult())
						.forLiteral(v -> v.getValue())
						.andRecover(e -> (char) 0))
					.map(Object::toString)
					.collect(joining());

				Object result = new SerializedValueWalker(c.getSnapshot().getExpectResult())
					.forLiteral(v -> v.getValue())
					.andRecover(e -> null);

				assertThat(input).isEqualTo("Hello In\u0000");
				assertThat(result).isEqualTo("hello in");
			});
	}

	@Test
	void testSelectionOnExpectOutput() throws Exception {
		List<TestCase> cases = database.fetch(query(new ExpectOutput(expectOutput -> !expectOutput.isEmpty())))
			.collect(toList());

		assertThat(cases)
			.hasSize(1)
			.anySatisfy(c -> {
				String output = c.getSnapshot().getExpectOutput().stream()
					.map(i -> SerializedValueWalker.start(i.getArguments(), 0)
						.forLiteral(v -> v.getValue())
						.andRecover(e -> (char) 0))
					.map(Object::toString)
					.collect(joining());

				Object argument = SerializedValueWalker.start(c.getSnapshot().getExpectArgs(), 0)
					.forLiteral(v -> v.getValue())
					.andRecover(e -> null);

				assertThat(argument).isEqualTo("Hello Out");
				assertThat(output).isEqualTo("HELLO OUT");
			});
	}

}
