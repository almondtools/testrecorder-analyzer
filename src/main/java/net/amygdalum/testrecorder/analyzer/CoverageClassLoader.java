package net.amygdalum.testrecorder.analyzer;

import java.io.IOException;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;

import net.amygdalum.testrecorder.util.ExtensibleClassLoader;

public class CoverageClassLoader extends ExtensibleClassLoader {

	private Instrumenter instr;

	public CoverageClassLoader(IRuntime runtime, ClassLoader classLoader, String... packages) {
		super(classLoader, packages);
		instr = new Instrumenter(runtime);
	}

	@Override
	protected byte[] getBytesForClass(String name) throws IOException {
		byte[] original = super.getBytesForClass(name);
		return instr.instrument(original, name);
	}

	public byte[] getRawBytesForClass(String name) throws IOException {
		return super.getBytesForClass(name);
	}
}
