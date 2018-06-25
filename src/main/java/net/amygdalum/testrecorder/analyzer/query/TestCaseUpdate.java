package net.amygdalum.testrecorder.analyzer.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.analyzer.UpdateProcess;

public class TestCaseUpdate {

	private List<Predicate<TestCase>> selectors;
	private List<UpdateProcess> updates;

	public TestCaseUpdate() {
		this.updates = new ArrayList<>();
	}

	public static TestCaseUpdate update() {
		return new TestCaseUpdate();
	}

	public TestCaseUpdate selecting(Predicate<TestCase> selector) {
		selectors.add(selector);
		return this;
	}

	public TestCaseUpdate updating(UpdateProcess update) {
		updates.add(update);
		return this;
	}
	
	public List<Predicate<TestCase>> selectors() {
		return selectors;
	}

	public List<UpdateProcess> updates() {
		return updates;
	}

}
