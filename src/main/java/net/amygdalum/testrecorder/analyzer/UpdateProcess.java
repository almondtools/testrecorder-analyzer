package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.analyzer.UpdateProcessChain.chain;

public interface UpdateProcess {

	void process(TestCase testCase) throws TaskFailedException;

	public default UpdateProcess then(UpdateProcess next) {
		return chain(this, next);
	}

}
