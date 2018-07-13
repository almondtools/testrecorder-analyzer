package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

public class SerializedValueWalkerException extends RuntimeException {

	public SerializedValueWalkerException(Collection<String> messages) {
		super(messages.stream().collect(joining("\n")));
	}

}
