package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.extensionpoint.ExtensionStrategy.OVERRIDING;

import net.amygdalum.testrecorder.extensionpoint.ExtensionPoint;

@ExtensionPoint(strategy=OVERRIDING)
public interface TestrecorderAnalyzerConfig {

	String getDatabaseFile();

	String getDatabaseCollection();

}
