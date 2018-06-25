package net.amygdalum.testrecorder.analyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateProcesses implements UpdateProcess {

	private List<UpdateProcess> processes;

	public UpdateProcesses() {
		this.processes = new ArrayList<>();
	}
	
	public static UpdateProcesses of(UpdateProcess... subprocesses) {
		UpdateProcesses process = new UpdateProcesses();
		for (UpdateProcess subprocess : subprocesses) {
			process.add(subprocess);
		}
		return process;
	}

	public void add(UpdateProcess subprocess) {
		processes.add(subprocess);
	}

	@Override
	public void process(TestCase testCase) throws TaskSkippedException {
		Set<Class<?>> failed = new HashSet<>();
		Set<Class<?>> success = new HashSet<>();
		for (UpdateProcess process : processes) {
			try {
				process.process(testCase);
				success.add(process.getClass());
			} catch (TaskSkippedException e) {
				failed.add(process.getClass());
			}
		}
		if (failed.size() == processes.size()) {
			throw UpdateProcess.skip();
		}
	}

}
