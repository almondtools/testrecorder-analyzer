package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.extensions.assertj.conventions.DefaultEquality.defaultEquality;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LineCoverageTest {

	@Test
	void testLineCoverage() throws Exception {
		LineCoverage coverage = new LineCoverage(22)
			.withBranchCoverage(2, 1)
			.withInstructionCoverage(4, 3);

		assertThat(coverage).satisfies(defaultEquality()
			.andEqualTo(new LineCoverage(22)
				.withBranchCoverage(2, 1)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new LineCoverage(33)
				.withBranchCoverage(2, 1)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new LineCoverage(22)
				.withBranchCoverage(2, 2)
				.withInstructionCoverage(4, 3))
			.andNotEqualTo(new LineCoverage(22)
				.withBranchCoverage(2, 1)
				.withInstructionCoverage(4, 4))
			.conventions());
	}

	@Test
	void testGetInstructionCoverage() throws Exception {
		assertThat(new LineCoverage(22)
			.withInstructionCoverage(4, 3).getInstructionCoverage().getTotal())
				.isEqualTo(4);
		assertThat(new LineCoverage(22)
			.withInstructionCoverage(4, 3).getInstructionCoverage().getCovered())
				.isEqualTo(3);
	}

	@Test
	void testGetBranchCoverage() throws Exception {
		assertThat(new LineCoverage(32)
			.withBranchCoverage(3, 2).getBranchCoverage().getTotal())
				.isEqualTo(3);
		assertThat(new LineCoverage(32)
			.withBranchCoverage(3, 2).getBranchCoverage().getCovered())
				.isEqualTo(2);
	}

	@Test
	void testIsCovered() throws Exception {
		assertThat(new LineCoverage(32)
			.withInstructionCoverage(3, 3)
			.withBranchCoverage(3, 3).isCovered())
				.isTrue();
		assertThat(new LineCoverage(32)
			.withInstructionCoverage(3, 2)
			.withBranchCoverage(3, 3).isCovered())
				.isFalse();
		assertThat(new LineCoverage(32)
			.withInstructionCoverage(3, 3)
			.withBranchCoverage(3, 2).isCovered())
				.isFalse();
		assertThat(new LineCoverage(32)
			.withInstructionCoverage(3, 2)
			.withBranchCoverage(3, 2).isCovered())
				.isFalse();
	}

	@Test
	void testToString() throws Exception {
		assertThat(new LineCoverage(32)
			.withInstructionCoverage(3, 2)
			.withBranchCoverage(2, 1).toString())
				.isEqualTo("32 (branch: 1/2, instruction: 2/3)");
	}

}
