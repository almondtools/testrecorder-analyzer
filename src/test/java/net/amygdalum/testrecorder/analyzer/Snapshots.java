package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.callsiterecorder.CallsiteRecorder;
import net.amygdalum.testrecorder.profile.ConfigurableSerializationProfile;
import net.amygdalum.testrecorder.profile.Fields;
import net.amygdalum.testrecorder.profile.Methods;
import net.amygdalum.testrecorder.profile.SerializationProfile;
import net.amygdalum.testrecorder.util.testobjects.Bean;
import net.amygdalum.testrecorder.util.testobjects.GenericCycle;
import net.amygdalum.testrecorder.util.testobjects.In;
import net.amygdalum.testrecorder.util.testobjects.Inner;
import net.amygdalum.testrecorder.util.testobjects.InputOutput;
import net.amygdalum.testrecorder.util.testobjects.Odd;
import net.amygdalum.testrecorder.util.testobjects.Static;

public class Snapshots {

	public static ContextSnapshot recordSetGlobal(Class<Static> thisObject, String argObject) throws Exception {
		Class<?>[] argtypes = { String.class };
		SerializationProfile profile = ConfigurableSerializationProfile.builder()
			.withGlobalFields(asList(Fields.byDescription(Static.class.getDeclaredField("global"))))
			.build();
		Static.global = null;
		try (CallsiteRecorder recorder = new CallsiteRecorder(profile, Static.class.getDeclaredMethod("setGlobal", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					Static.setGlobal(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(Odd thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = { Integer.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(Odd.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(In thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = { Integer.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(In.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(Inner.Negate thisObject, Boolean argObject) throws Exception {
		Class<?>[] argtypes = { Boolean.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(Inner.Negate.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordInsert(GenericCycle<String> thisObject, GenericCycle<String> argObject) throws Exception {
		try (CallsiteRecorder recorder = new CallsiteRecorder(GenericCycle.class.getDeclaredMethod("insert", GenericCycle.class))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.insert(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordSetAttribute(Bean thisObject, String argObject) throws Exception {
		Class<?>[] argtypes = { String.class };
		try (CallsiteRecorder recorder = new CallsiteRecorder(Bean.class.getDeclaredMethod("setAttribute", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.setAttribute(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordGetAttribute(Bean thisObject) throws Exception {
		Class<?>[] argtypes = {};
		try (CallsiteRecorder recorder = new CallsiteRecorder(Bean.class.getDeclaredMethod("getAttribute", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.getAttribute();
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordReadLowerCase(InputOutput thisObject, String inObject) throws Exception {
		Class<?>[] argtypes = new Class[0];
		SerializationProfile profile = ConfigurableSerializationProfile.builder()
			.withInputs(asList(Methods.byDescription(InputOutput.class.getDeclaredMethod("in"))))
			.build();
		inObject.chars()
			.mapToObj(i -> Character.valueOf((char) i))
			.forEach(InputOutput.IN::add);
		try (CallsiteRecorder recorder = new CallsiteRecorder(profile, InputOutput.class.getDeclaredMethod("readLowerCase", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.readLowerCase();
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordWriteUpperCase(InputOutput thisObject, String argObject) throws Exception {
		Class<?>[] argtypes = {String.class};
		SerializationProfile profile = ConfigurableSerializationProfile.builder()
			.withOutputs(asList(Methods.byDescription(InputOutput.class.getDeclaredMethod("out", char.class))))
			.build();
		try (CallsiteRecorder recorder = new CallsiteRecorder(profile, InputOutput.class.getDeclaredMethod("writeUpperCase", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.writeUpperCase(argObject);
				});
			return recordings.join().get(0);
		}
	}
	
}
