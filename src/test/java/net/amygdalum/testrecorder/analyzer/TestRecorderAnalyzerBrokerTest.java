package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.extensions.assertj.iterables.IterableConditions.containingExactly;
import static net.amygdalum.testrecorder.analyzer.query.TestCaseQuery.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.callsiterecorder.CallsiteRecorder;
import net.amygdalum.testrecorder.conditions.AContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedReferenceType;
import net.amygdalum.testrecorder.util.testobjects.GenericCycle;

public class TestRecorderAnalyzerBrokerTest {

	private TestAgentConfiguration config;
	private TestRecorderAnalyzerBroker broker;

	@BeforeEach
	public void before() throws Exception {
		config = TestAgentConfiguration.defaultConfig();
		broker = new TestRecorderAnalyzerBroker(config);
	}

	@Test
	void testAccept() throws Exception {
		GenericCycle<String> thisObject = GenericCycle.recursive("element1");
		GenericCycle<String> argObject = GenericCycle.recursive("element2");
		ContextSnapshot snap = createGenericCycleSnapshot(thisObject, argObject);

		broker.accept(snap);

		List<TestCase> all = broker.getDatabase().fetch(query()).collect(toList());
		assertThat(all)
			.hasSize(1)
			.is(containingExactly(
				ATestCase.withSnapshot(
					AContextSnapshot.withKey(snap.getKey())
						.andSetupThis((SerializedReferenceType) snap.getSetupThis()))));
	}

	private ContextSnapshot createGenericCycleSnapshot(GenericCycle<String> thisObject, GenericCycle<String> argObject) throws Exception {
		try (CallsiteRecorder recorder = new CallsiteRecorder(GenericCycle.class.getDeclaredMethod("insert", GenericCycle.class))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.insert(argObject);
				});
			return recordings.join().get(0);
		}
	}

}
