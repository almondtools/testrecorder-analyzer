package net.amygdalum.testrecorder.analyzer;

public class TestTestrecorderAnalyzerConfig implements TestrecorderAnalyzerConfig {

	@Override
	public String getDatabaseFile() {
		return null;
	}

	@Override
	public String getDatabaseCollection() {
		return "tests";
	}

}
