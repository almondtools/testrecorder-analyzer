package net.amygdalum.testrecorder.analyzer.query;

import static java.lang.System.identityHashCode;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.ComputeCoverage;
import net.amygdalum.testrecorder.analyzer.Coverage;
import net.amygdalum.testrecorder.analyzer.Equivalence;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.util.BiOptional;

public class CoverageEquivalence implements Equivalence {
	@Override
	public int hashCode(TestCase o) {
		return ComputeCoverage.COVERAGE.from(o)
			.map(coverage -> coverage.hashCode())
			.orElse(identityHashCode(o));
	}

	@Override
	public boolean equals(TestCase o1, TestCase o2) {
		Optional<Coverage> coverage1 = ComputeCoverage.COVERAGE.from(o1);
		Optional<Coverage> coverage2 = ComputeCoverage.COVERAGE.from(o2);
		return BiOptional.ofOptionals(coverage1, coverage2)
			.map((first, second) -> first.equals(second), single -> false)
			.orElse(false);
	}

	public static EquivalenceCollector distinctCoverage() {
		return new EquivalenceCollector(new CoverageEquivalence());
	}

}