package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class UpdateProcessChain implements UpdateProcess {

	private List<UpdateProcess> updateProcesses;

	public UpdateProcessChain(List<UpdateProcess> updateProcesses) {
		this.updateProcesses = updateProcesses;
	}

	public static UpdateProcess chain(UpdateProcess... updateProcesses) {
		List<UpdateProcess> collect = Arrays.stream(updateProcesses)
			.flatMap(UpdateProcessChain::flatten)
			.collect(toList());
		return new UpdateProcessChain(collect);
	}

	private static Stream<UpdateProcess> flatten(UpdateProcess updateProcess) {
		if (updateProcess instanceof UpdateProcessChain) {
			return ((UpdateProcessChain) updateProcess).updateProcesses.stream();
		} else {
			return Stream.of(updateProcess);
		}
	}

	@Override
	public void process(TestCase testCase) throws TaskFailedException {
		for (UpdateProcess updateProcess : updateProcesses) {
			updateProcess.process(testCase);
		}
	}

}
