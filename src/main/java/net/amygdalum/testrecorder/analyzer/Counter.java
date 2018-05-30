package net.amygdalum.testrecorder.analyzer;

import java.io.Serializable;
import java.util.Objects;

public class Counter implements Serializable {

	private int total;
	private int covered;

	public Counter(int total, int covered) {
		this.total = total;
		this.covered = covered;
	}

	public int getTotal() {
		return total;
	}
	
	public int getCovered() {
		return covered;
	}

	public boolean isCovered() {
		return total == covered;
	}
	
	@Override
	public int hashCode() {
		return total
			+ Objects.hashCode(covered) * 13;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Counter that = (Counter) obj;
		return this.total == that.total
			&& this.covered == that.covered;
	}

}
