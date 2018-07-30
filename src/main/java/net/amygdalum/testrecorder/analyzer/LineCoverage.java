package net.amygdalum.testrecorder.analyzer;

import java.io.Serializable;
import java.util.Objects;

public class LineCoverage implements Serializable {

	private int line;
	private Counter instruction;
	private Counter branch;
	
	public LineCoverage(int line) {
		this.line = line;
	}
	
	public int getLine() {
		return line;
	}

	public LineCoverage withInstructionCoverage(int total, int covered) {
		instruction = new Counter(total, covered);
		return this;
	}
	
	public Counter getInstructionCoverage() {
		return instruction;
	}

	public LineCoverage withBranchCoverage(int total, int covered) {
		branch = new Counter(total, covered);
		return this;
	}
	
	public Counter getBranchCoverage() {
		return branch;
	}

	public boolean isCovered() {
		return instruction.isCovered()
			&& branch.isCovered();
	}
	@Override
	public String toString() {
		return String.format("%d (branch: %d/%d, instruction: %d/%d)", line, branch.getCovered(), branch.getTotal(), instruction.getCovered(), instruction.getTotal());
	}

	@Override
	public int hashCode() {
		return line
			+ Objects.hashCode(instruction) * 7
			+ Objects.hashCode(branch) * 31;
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
		LineCoverage that = (LineCoverage) obj;
		return this.line == that.line
			&& Objects.equals(this.instruction, that.instruction)
			&& Objects.equals(this.branch, that.branch);
	}

}
