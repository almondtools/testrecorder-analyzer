package net.amygdalum.testrecorder.analyzer.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import net.amygdalum.testrecorder.analyzer.PropertySelector;
import net.amygdalum.testrecorder.analyzer.TestCase;

public class TestCaseQuery {

	private List<PropertySelector> selectors;
	private List<Collector<TestCase, ?, Stream<TestCase>>> collectors;

	public TestCaseQuery() {
		this.selectors = new ArrayList<>();
		this.collectors = new ArrayList<>();
	}

	public static TestCaseQuery query() {
		return new TestCaseQuery();
	}

	public static TestCaseQuery query(PropertySelector selector) {
		TestCaseQuery testCaseQuery = new TestCaseQuery();
		testCaseQuery.selectors.add(selector);
		return testCaseQuery;
	}

	public TestCaseQuery and(PropertySelector selector) {
		selectors.add(selector);
		return this;
	}

	public TestCaseQuery collecting(Collector<TestCase, ?, Stream<TestCase>> collector) {
		collectors.add(collector);
		return this;
	}
	
	public List<PropertySelector> selectors() {
		return selectors;
	}

	public List<Collector<TestCase, ?, Stream<TestCase>>> collectors() {
		return collectors;
	}

}
