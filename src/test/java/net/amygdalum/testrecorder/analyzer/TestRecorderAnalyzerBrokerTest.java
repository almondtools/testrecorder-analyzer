package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.extensions.assertj.iterables.IterableConditions.containingExactly;
import static net.amygdalum.testrecorder.analyzer.Snapshots.recordInsert;
import static net.amygdalum.testrecorder.analyzer.query.TestCaseQuery.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.conditions.AContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedReferenceType;
import net.amygdalum.testrecorder.util.testobjects.GenericCycle;

public class TestRecorderAnalyzerBrokerTest {

	private TestAgentConfiguration config;
	private TestRecorderAnalyzerBroker broker;

	@BeforeEach
	void before() throws Exception {
		config = TestAgentConfiguration.defaultConfig();
		broker = new TestRecorderAnalyzerBroker(config);
	}

	@Test
	void testAccept() throws Exception {
		GenericCycle<String> thisObject = GenericCycle.recursive("element1");
		GenericCycle<String> argObject = GenericCycle.recursive("element2");
		ContextSnapshot snap = recordInsert(thisObject, argObject);

		broker.accept(snap);

		List<TestCase> all = broker.getDatabase().fetch(query()).collect(toList());
		assertThat(all)
			.hasSize(1)
			.is(containingExactly(
				ATestCase.withSnapshot(
					AContextSnapshot.withKey(snap.getKey())
						.andSetupThis((SerializedReferenceType) snap.getSetupThis()))));
	}

}
