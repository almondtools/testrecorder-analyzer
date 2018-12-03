package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.ExtensionStrategy.OVERRIDING;

import net.amygdalum.testrecorder.ExtensionPoint;

@ExtensionPoint(strategy=OVERRIDING)
public interface TestrecorderAnalyzerConfig {

	String getDatabaseFile();

	String getDatabaseCollection();

}
