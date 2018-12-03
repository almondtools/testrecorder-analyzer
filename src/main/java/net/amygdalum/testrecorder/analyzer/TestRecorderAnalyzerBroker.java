package net.amygdalum.testrecorder.analyzer;

import net.amygdalum.testrecorder.profile.SnapshotConsumer;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class TestRecorderAnalyzerBroker implements SnapshotConsumer {

	private TestDatabase database;

	public TestRecorderAnalyzerBroker(AgentConfiguration config) {
		this.database = new TestDatabase(config.loadConfiguration(TestrecorderAnalyzerConfig.class), new Serialization());
	}
	
	public TestDatabase getDatabase() {
		return database;
	}

	@Override
	public void accept(ContextSnapshot snapshot) {
		TestCase testCase = new TestCase(snapshot);
		database.store(testCase);
	}

}
