package net.amygdalum.testrecorder.analyzer.testobjects;

public class FloatComparator {

	private float tolerance;

	public FloatComparator(float tolerance) {
		this.tolerance = tolerance;
	}

	public boolean areEqual(float f1, float f2) {
		if (f1 == f2) {
			return true;
		}
		return Math.abs(f1 - f2) <= tolerance;
	}

}