package net.amygdalum.testrecorder.analyzer;

public class DefaultTestrecorderAnalyzerConfig implements TestrecorderAnalyzerConfig {

	@Override
	public String getDatabaseFile() {
		return "test.db";
	}

	@Override
	public String getDatabaseCollection() {
		return "tests";
	}

}
