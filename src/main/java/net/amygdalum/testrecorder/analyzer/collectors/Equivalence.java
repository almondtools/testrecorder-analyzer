package net.amygdalum.testrecorder.analyzer.collectors;

import net.amygdalum.testrecorder.analyzer.TestCase;

public interface Equivalence {

	boolean equals(TestCase o1, TestCase o2);
	
	int hashCode(TestCase o);
}
