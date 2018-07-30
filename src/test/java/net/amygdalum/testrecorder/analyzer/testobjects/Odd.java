package net.amygdalum.testrecorder.analyzer.testobjects;

import java.util.function.Predicate;

public class Odd implements Predicate<Integer> {

	private boolean isOdd(int n) {
		if (n % 2 == 1) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean test(Integer n) {
		return isOdd(n);
	}
	
	public static Odd odd() {
		return new Odd();
	}

}