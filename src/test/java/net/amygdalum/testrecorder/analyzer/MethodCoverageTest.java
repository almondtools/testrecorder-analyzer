package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.extensions.assertj.conventions.DefaultEquality.defaultEquality;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class MethodCoverageTest {

	@Test
	void testMethodCoverage() throws Exception {
		MethodCoverage coverage = new MethodCoverage("methodName")
			.withBranchCoverage(2, 1)
			.withComplexityCoverage(3, 2)
			.withInstructionCoverage(4, 3);

		assertThat(coverage).satisfies(defaultEquality()
			.andEqualTo(new MethodCoverage("methodName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new MethodCoverage("otherName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new MethodCoverage("methodName")
				.withBranchCoverage(2, 2)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new MethodCoverage("methodName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 3)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new MethodCoverage("methodName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 4))
			.andNotEqualTo(new MethodCoverage("methodName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3)
				.addLineCoverage(new LineCoverage(22)
					.withBranchCoverage(2, 1)
					.withInstructionCoverage(4, 3)))
			.conventions());
	}

	@Test
	void testGetInstructionCoverage() throws Exception {
		assertThat(new MethodCoverage("methodName")
			.withInstructionCoverage(4, 3).getInstructionCoverage().getTotal())
				.isEqualTo(4);
		assertThat(new MethodCoverage("methodName")
			.withInstructionCoverage(4, 3).getInstructionCoverage().getCovered())
				.isEqualTo(3);
	}

	@Test
	void testGetBranchCoverage() throws Exception {
		assertThat(new MethodCoverage("methodName")
			.withBranchCoverage(3, 2).getBranchCoverage().getTotal())
				.isEqualTo(3);
		assertThat(new MethodCoverage("methodName")
			.withBranchCoverage(3, 2).getBranchCoverage().getCovered())
				.isEqualTo(2);
	}

	@Test
	void testGetComplexityCoverage() throws Exception {
		assertThat(new MethodCoverage("methodName")
			.withComplexityCoverage(2, 1).getComplexityCoverage().getTotal())
				.isEqualTo(2);
		assertThat(new MethodCoverage("methodName")
			.withComplexityCoverage(2, 1).getComplexityCoverage().getCovered())
				.isEqualTo(1);
	}

	@Test
	void testGetLineCoverage() throws Exception {
		assertThat(new MethodCoverage("methodName")
			.addLineCoverage(new LineCoverage(22))
			.getLineCoverage(22))
				.contains(new LineCoverage(22));
		assertThat(new MethodCoverage("methodName")
			.getLineCoverage(22))
				.isNotPresent();
		assertThat(new MethodCoverage("methodName")
			.addLineCoverage(new LineCoverage(23))
			.getLineCoverage(22))
				.isNotPresent();
	}

	@Test
	void testToString() throws Exception {
		assertThat(new MethodCoverage("methodName")
		.withBranchCoverage(2, 1)
		.withComplexityCoverage(3, 2)
		.withInstructionCoverage(4, 3)
		.addLineCoverage(new LineCoverage(22)
			.withBranchCoverage(2, 1)
			.withInstructionCoverage(4, 3)).toString())
		.isEqualTo("methodName (branch: 1/2, complexity: 2/3, instructions: 3/4)");
	}

}

