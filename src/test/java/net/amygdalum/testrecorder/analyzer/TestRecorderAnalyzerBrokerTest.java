package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.toList;
import static net.amygdalum.extensions.assertj.iterables.IterableConditions.containingExactly;
import static net.amygdalum.testrecorder.analyzer.query.TestCaseQuery.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ConfigurableSerializerFacade;
import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.MethodSignature;
import net.amygdalum.testrecorder.conditions.AContextSnapshot;
import net.amygdalum.testrecorder.serializers.SerializerFacade;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.types.SerializerSession;
import net.amygdalum.testrecorder.util.Types;
import net.amygdalum.testrecorder.util.testobjects.GenericCycle;
import net.amygdalum.testrecorder.values.SerializedObject;

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
		SerializerFacade facade = new ConfigurableSerializerFacade(config);

		GenericCycle<String> thisObject = GenericCycle.recursive("element1");
		ContextSnapshot snap = new ContextSnapshot(0, "mysnapshotid", MethodSignature.fromDescriptor(GenericCycle.class.getDeclaredMethod("insert", GenericCycle.class)));

		SerializerSession session = facade.newSession();

		SerializedObject serializedThisObject = (SerializedObject) facade.serialize(GenericCycle.class, thisObject, session);
		serializedThisObject.useAs(Types.parameterized(GenericCycle.class, null, String.class));

		GenericCycle<String> argObject = GenericCycle.recursive("element2");

		SerializedObject serializedArgObject = (SerializedObject) facade.serialize(GenericCycle.class, argObject, session);
		serializedArgObject.useAs(Types.parameterized(GenericCycle.class, null, String.class));

		boolean resultObject = thisObject.insert(argObject);

		session = facade.newSession();

		SerializedObject expectedThisObject = (SerializedObject) facade.serialize(GenericCycle.class, thisObject, session);
		expectedThisObject.useAs(Types.parameterized(GenericCycle.class, null, String.class));

		SerializedValue expectedArgObject = serializedArgObject;

		SerializedValue expectedResultObject = facade.serialize(boolean.class, resultObject, session);

		snap.setSetupThis(serializedThisObject);
		snap.setSetupArgs(serializedArgObject);
		snap.setSetupGlobals();

		snap.setExpectThis(expectedThisObject);
		snap.setExpectArgs(expectedArgObject);
		snap.setExpectGlobals();
		snap.setExpectResult(expectedResultObject);

		broker.accept(snap);

		List<TestCase> all = broker.getDatabase().fetch(query()).collect(toList());
		assertThat(all)
			.hasSize(1)
			.is(containingExactly(
				ATestCase.withSnapshot(
					AContextSnapshot.withKey("mysnapshotid")
						.andSetupThis(serializedThisObject))));
	}

}
