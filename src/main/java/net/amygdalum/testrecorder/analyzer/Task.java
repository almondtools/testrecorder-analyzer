package net.amygdalum.testrecorder.analyzer;

import java.util.List;

public interface Task {

	static TaskSkippedException skip() {
		return new TaskSkippedException();
	}
	
	static TaskSkippedException skip(Throwable cause) {
		return new TaskSkippedException(cause);
	}

	List<Property<?>> requiredProperties();
	
	void process(TestCase testCase) throws TaskSkippedException;

}
