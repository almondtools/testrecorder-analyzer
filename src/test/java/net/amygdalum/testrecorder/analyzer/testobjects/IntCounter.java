package net.amygdalum.testrecorder.analyzer.testobjects;

public class IntCounter {
	private int value;

	public IntCounter() {
		value = 0;
	}
	
	public IntCounter(int value) {
		this.value = value;
	}
	
	public int next() {
		return value++;
	}

	public void reset() {
		value = 0;
	}

}