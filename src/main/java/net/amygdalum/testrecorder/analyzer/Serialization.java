package net.amygdalum.testrecorder.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

	public void serialize(Object value, OutputStream out) throws IOException {
		serializationconfig.encodeToStream(out, value);
	}

	public Object deserialize(InputStream in) throws Exception {
		return serializationconfig.decodeFromStream(in);
	}

}
