package net.amygdalum.testrecorder.analyzer.query;

import java.util.Optional;

import net.amygdalum.testrecorder.analyzer.ComputeCoverage;
import net.amygdalum.testrecorder.analyzer.Coverage;
import net.amygdalum.testrecorder.analyzer.Equivalence;
import net.amygdalum.testrecorder.analyzer.TestCase;

public class CoverageEquivalence implements Equivalence {
	@Override
	public int hashCode(TestCase o) {
		return ComputeCoverage.COVERAGE.from(o)
			.map(coverage -> coverage.hashCode())
			.orElse(System.identityHashCode(o));
	}

	@Override
	public boolean equals(TestCase o1, TestCase o2) {
		Optional<Coverage> coverage1 = ComputeCoverage.COVERAGE.from(o1);
		Optional<Coverage> coverage2 = ComputeCoverage.COVERAGE.from(o2);
		if (coverage1.isPresent() && coverage2.isPresent()) {
			return coverage1.get().equals(coverage2.get());
		}
		return false;
	}

	public static EquivalenceCollector distinctCoverage() {
		return new EquivalenceCollector(new CoverageEquivalence());
	}

}