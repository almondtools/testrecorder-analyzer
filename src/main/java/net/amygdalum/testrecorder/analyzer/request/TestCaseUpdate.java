package net.amygdalum.testrecorder.analyzer.request;

import java.util.ArrayList;
import java.util.List;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.PropertyUpdate;

public class TestCaseUpdate {

	private List<PropertySelector> selectors;
	private List<PropertyUpdate> updates;

	public TestCaseUpdate() {
		this.selectors = new ArrayList<>();
		this.updates = new ArrayList<>();
	}

	public static TestCaseUpdate properties(PropertyUpdate... updates) {
		TestCaseUpdate testCaseUpdate = new TestCaseUpdate();
		for (PropertyUpdate update : updates) {
			testCaseUpdate.updates.add(update);
		}
		return testCaseUpdate;
	}

	public TestCaseUpdate where(PropertySelector selector) {
		selectors.add(selector);
		return this;
	}

	public List<PropertySelector> selectors() {
		return selectors;
	}

	public List<PropertyUpdate> updates() {
		return updates;
	}

}
