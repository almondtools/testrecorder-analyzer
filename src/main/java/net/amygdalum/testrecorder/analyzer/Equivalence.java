package net.amygdalum.testrecorder.analyzer;

public interface Equivalence {

	boolean equals(TestCase o1, TestCase o2);
	
	int hashCode(TestCase o);
}
