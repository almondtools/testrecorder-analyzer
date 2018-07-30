package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.extensions.assertj.conventions.DefaultEquality.defaultEquality;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

import org.junit.jupiter.api.Test;

public class CounterTest {

	@Test
	void testGetTotal() throws Exception {
		assertThat(new Counter(2, 1).getTotal()).isEqualByComparingTo(2);
	}

	@Test
	void testGetCovered() throws Exception {
		assertThat(new Counter(2, 1).getCovered()).isEqualByComparingTo(1);
	}

	@Test
	void testRatio() throws Exception {
		assertThat(new Counter(2, 1).ratio()).isCloseTo(1f / 2f, withPercentage(0.001));
	}

	@Test
	void testIsCovered() throws Exception {
		assertThat(new Counter(2, 2).isCovered()).isTrue();
		assertThat(new Counter(2, 1).isCovered()).isFalse();
		assertThat(new Counter(2, 0).isCovered()).isFalse();
	}

	@Test
	void testIsUncovered() throws Exception {
		assertThat(new Counter(2, 2).isUncovered()).isFalse();
		assertThat(new Counter(2, 1).isUncovered()).isFalse();
		assertThat(new Counter(2, 0).isUncovered()).isTrue();
	}

	@Test
	void testEquals() throws Exception {
		assertThat(new Counter(2, 1)).satisfies(defaultEquality()
			.andEqualTo(new Counter(2, 1))
			.andNotEqualTo(new Counter(2, 2))
			.andNotEqualTo(new Counter(1, 1))
			.conventions());
	}

	@Test
	void testToString() throws Exception {
		assertThat(new Counter(2, 1).toString()).isEqualTo("1/2");
	}

}
