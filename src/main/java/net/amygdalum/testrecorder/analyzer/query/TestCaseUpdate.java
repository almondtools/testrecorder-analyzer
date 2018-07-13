package net.amygdalum.testrecorder.analyzer.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.PropertyUpdate;

public class TestCaseUpdate {

	private List<Predicate<TestCase>> selectors;
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

	public TestCaseUpdate where(Predicate<TestCase> selector) {
		selectors.add(selector);
		return this;
	}

	public List<Predicate<TestCase>> selectors() {
		return selectors;
	}

	public List<PropertyUpdate> updates() {
		return updates;
	}

}
