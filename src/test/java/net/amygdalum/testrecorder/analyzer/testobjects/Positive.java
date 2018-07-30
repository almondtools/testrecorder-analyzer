package net.amygdalum.testrecorder.analyzer.testobjects;

import java.util.function.Predicate;

public class Positive implements Predicate<Float> {

	private boolean isPositive(float x) {
		if (x >= 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean test(Float x) {
		return isPositive(x);
	}
	
	public static Positive positive() {
		return new Positive();
	}

}