package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.extensions.assertj.conventions.DefaultEquality.defaultEquality;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CoverageTest {

	@Test
	void testCoverage() throws Exception {
		Coverage coverage = new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"));

		assertThat(coverage).satisfies(defaultEquality()
			.andEqualTo(new Coverage(true)
				.addMethodCoverage(new MethodCoverage("methodName")))
			.andNotEqualTo(new Coverage(false)
				.addMethodCoverage(new MethodCoverage("methodName")))
			.andNotEqualTo(new Coverage(true)
				.addMethodCoverage(new MethodCoverage("otherName")))
			.conventions());
	}

	@Test
	void testIsSuccess() throws Exception {
		assertThat(new Coverage(true).isSuccess()).isTrue();
		assertThat(new Coverage(false).isSuccess()).isFalse();
	}

	@Test
	void testGetMethods() throws Exception {
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"))
			.getMethods())
				.contains(new MethodCoverage("methodName"));
	}

	@Test
	void testGetMethodCoverage() throws Exception {
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"))
			.getMethodCoverage("methodName"))
				.contains(new MethodCoverage("methodName"));
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"))
			.getMethodCoverage("otherName"))
				.isNotPresent();
	}

	@Test
	void testGetLineCoverage() throws Exception {
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName")
				.addLineCoverage(new LineCoverage(22)))
			.getLineCoverage(22))
				.contains(new LineCoverage(22));
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName")
				.addLineCoverage(new LineCoverage(24)))
			.addMethodCoverage(new MethodCoverage("otherName")
				.addLineCoverage(new LineCoverage(22)))
			.getLineCoverage(22))
				.contains(new LineCoverage(22));
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"))
			.addMethodCoverage(new MethodCoverage("otherName")
				.addLineCoverage(new LineCoverage(22)))
			.getLineCoverage(22))
				.contains(new LineCoverage(22));
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName"))
			.getLineCoverage(22))
				.isNotPresent();
	}

	@Test
	void testToString() throws Exception {
		assertThat(new Coverage(true)
			.addMethodCoverage(new MethodCoverage("methodName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3))
			.addMethodCoverage(new MethodCoverage("otherName")
				.withBranchCoverage(2, 1)
				.withComplexityCoverage(3, 2)
				.withInstructionCoverage(4, 3))
			.toString())
				.isEqualTo("{\n"
					+ "\tmethodName (branch: 1/2, complexity: 2/3, instructions: 3/4)\n"
					+ "\totherName (branch: 1/2, complexity: 2/3, instructions: 3/4)\n"
					+ "}:success");
		assertThat(new Coverage(false).toString())
			.isEqualTo("{\n\t"
				+ "\n}:failed");
	}

}
