package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PropertyUpdates implements PropertyUpdate {

	private List<PropertyUpdate> PropertyUpdatees;

	public PropertyUpdates(List<PropertyUpdate> PropertyUpdatees) {
		this.PropertyUpdatees = PropertyUpdatees;
	}

	public static PropertyUpdates inSequence(PropertyUpdate... PropertyUpdatees) {
		List<PropertyUpdate> collect = Arrays.stream(PropertyUpdatees)
			.flatMap(PropertyUpdates::flatten)
			.collect(toList());
		return new PropertyUpdates(collect);
	}

	private static Stream<PropertyUpdate> flatten(PropertyUpdate PropertyUpdate) {
		if (PropertyUpdate instanceof PropertyUpdates) {
			return ((PropertyUpdates) PropertyUpdate).PropertyUpdatees.stream();
		} else {
			return Stream.of(PropertyUpdate);
		}
	}

	@Override
	public boolean apply(TestCase testCase) {
		boolean changed = false;
		for (PropertyUpdate update : PropertyUpdatees) {
			changed |= update.apply(testCase);
		}
		return changed;
	}

}
