package net.amygdalum.testrecorder.analyzer;

import org.nustaq.serialization.FSTConfiguration;

public class Serialization {

	private FSTConfiguration serializationconfig;

	public Serialization() {
		this.serializationconfig = FSTConfiguration.createDefaultConfiguration();
	}

	public byte[] serialize(Object value) {
		return serializationconfig.asByteArray(value);
	}

	public Object deserialize(byte[] bytes) {
		return serializationconfig.asObject(bytes);
	}

}
