package net.amygdalum.testrecorder.analyzer;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tasks implements Task {

	private List<Task> tasks;

	public Tasks() {
		this.tasks = new ArrayList<>();
	}
	
	public static Tasks of(Task... subtasks) {
		Tasks tasks = new Tasks();
		for (Task subtask : subtasks) {
			tasks.add(subtask);
		}
		return tasks;
	}

	public void add(Task subtask) {
		tasks.add(subtask);
	}

	@Override
	public List<Property<?>> requiredProperties() {
		return emptyList();
	}

	@Override
	public void process(TestCase testCase) throws TaskSkippedException {
		Set<Class<?>> failed = new HashSet<>();
		Set<Class<?>> success = new HashSet<>();
		nextTask: for (Task task : tasks) {
			try {
				for (Property<?> property : task.requiredProperties()) {
					if (success.contains(property.getDefinition())) {
						continue;
					} else if (failed.contains(property.getDefinition())) {
						continue nextTask;
					} else {
						throw Task.skip();
					}
				}
				task.process(testCase);
				success.add(task.getClass());
			} catch (TaskSkippedException e) {
				failed.add(task.getClass());
			}
		}
		if (failed.size() == tasks.size()) {
			throw Task.skip();
		}
	}

}
