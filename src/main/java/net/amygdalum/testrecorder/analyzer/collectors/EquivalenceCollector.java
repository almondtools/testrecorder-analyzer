package net.amygdalum.testrecorder.analyzer.collectors;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import net.amygdalum.testrecorder.analyzer.TestCase;

public class EquivalenceCollector implements Collector<TestCase, Map<Integer, Queue<TestCase>>, Stream<TestCase>> {

	private Equivalence equivalence;

	public EquivalenceCollector(Equivalence equivalence) {
		this.equivalence = equivalence;
	}

	@Override
	public Supplier<Map<Integer, Queue<TestCase>>> supplier() {
		return LinkedHashMap::new;
	}

	@Override
	public BiConsumer<Map<Integer, Queue<TestCase>>, TestCase> accumulator() {
		return (accumulator, element) -> {
			int hashCode = equivalence.hashCode(element);
			accumulator.computeIfAbsent(hashCode, key -> new LinkedList<>()).add(element);
		};
	}

	@Override
	public BinaryOperator<Map<Integer, Queue<TestCase>>> combiner() {
		return (accumulator1, accumulator2) -> {
			Map<Integer, Queue<TestCase>> accumulator = new LinkedHashMap<>();
			for (Map.Entry<Integer, Queue<TestCase>> entry : accumulator1.entrySet()) {
				accumulator.computeIfAbsent(entry.getKey(), key -> new LinkedList<>()).addAll(entry.getValue());
			}
			for (Map.Entry<Integer, Queue<TestCase>> entry : accumulator2.entrySet()) {
				accumulator.computeIfAbsent(entry.getKey(), key -> new LinkedList<>()).addAll(entry.getValue());
			}
			return accumulator;
		};
	}

	@Override
	public Function<Map<Integer, Queue<TestCase>>, Stream<TestCase>> finisher() {
		return accumulator -> {
			Builder<TestCase> result = Stream.builder();
			for (Map.Entry<Integer, Queue<TestCase>> entry : accumulator.entrySet()) {
				Queue<TestCase> values = entry.getValue();
				while (!values.isEmpty()) {
					TestCase selected = values.remove();
					result.add(selected);
					Iterator<TestCase> valueIterator = values.iterator();
					while (valueIterator.hasNext()) {
						TestCase next = valueIterator.next();
						if (equivalence.equals(selected, next)) {
							valueIterator.remove();
						}
					}
				}
			}
			return result.build();
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.noneOf(Characteristics.class);
	}

}
