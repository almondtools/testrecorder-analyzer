package net.amygdalum.testrecorder.analyzer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MethodCoverage implements Serializable {

	private String name;
	private Counter instructions;
	private Counter branches;
	private Counter complexity;
	private List<LineCoverage> lines;

	public MethodCoverage(String name) {
		this.name = name;
		this.lines = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setInstructionCoverage(int total, int covered) {
		instructions = new Counter(total, covered);
	}

	public Counter getInstructionCoverage() {
		return instructions;
	}

	public void setBranchCoverage(int total, int covered) {
		branches = new Counter(total, covered);
	}
	
	public Counter getBranchCoverage() {
		return branches;
	}

	public void setComplexityCoverage(int total, int covered) {
		complexity = new Counter(total, covered);
	}
	
	public Counter getComplexityCoverage() {
		return complexity;
	}

	public void addLine(LineCoverage coverage) {
		lines.add(coverage);
	}

	public List<LineCoverage> getLines() {
		return lines;
	}

	public Optional<LineCoverage> getLine(int lineNo) {
		return lines.stream()
			.filter(line -> line.getLine() == lineNo)
			.findFirst();
	}

	@Override
	public String toString() {
		return String.format("%s (branch: %d/%d, complexity: %d/%d, instructions: %d/%d)", name, branches.getCovered(), branches.getTotal(), complexity.getCovered(), complexity.getTotal(),
			instructions.getCovered(), instructions.getTotal());
	}

	@Override
	public int hashCode() {
		return name.hashCode()
			+ Objects.hashCode(instructions) * 7
			+ Objects.hashCode(branches) * 13
			+ Objects.hashCode(complexity) * 17
			+ Objects.hashCode(lines) * 31;
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
		MethodCoverage that = (MethodCoverage) obj;
		return Objects.equals(this.name, that.name)
			&& Objects.equals(this.instructions, that.instructions)
			&& Objects.equals(this.branches, that.branches)
			&& Objects.equals(this.complexity, that.complexity)
			&& Objects.equals(this.lines, that.lines);
	}

}
