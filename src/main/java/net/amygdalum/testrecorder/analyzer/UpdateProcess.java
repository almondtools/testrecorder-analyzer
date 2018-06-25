package net.amygdalum.testrecorder.analyzer;

public interface UpdateProcess {

	static TaskSkippedException skip() {
		return new TaskSkippedException();
	}
	
	static TaskSkippedException skip(Throwable cause) {
		return new TaskSkippedException(cause);
	}

	void process(TestCase testCase) throws TaskSkippedException;

}
