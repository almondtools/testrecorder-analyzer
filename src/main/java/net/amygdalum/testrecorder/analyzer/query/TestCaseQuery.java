package net.amygdalum.testrecorder.analyzer.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import net.amygdalum.testrecorder.analyzer.TestCase;

public class TestCaseQuery {

	private List<Predicate<TestCase>> selectors;
	private List<Collector<TestCase, ?, Stream<TestCase>>> collectors;

	public TestCaseQuery() {
		this.selectors = new ArrayList<>();
		this.collectors = new ArrayList<>();
	}

	public static TestCaseQuery query() {
		return new TestCaseQuery();
	}

	public TestCaseQuery selecting(Predicate<TestCase> selector) {
		selectors.add(selector);
		return this;
	}

	public TestCaseQuery collecting(Collector<TestCase, ?, Stream<TestCase>> collector) {
		collectors.add(collector);
		return this;
	}
	
	public List<Predicate<TestCase>> selectors() {
		return selectors;
	}

	public List<Collector<TestCase, ?, Stream<TestCase>>> collectors() {
		return collectors;
	}

}
