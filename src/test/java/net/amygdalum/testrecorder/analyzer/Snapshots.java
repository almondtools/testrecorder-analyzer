package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.amygdalum.testrecorder.analyzer.testobjects.Bean;
import net.amygdalum.testrecorder.analyzer.testobjects.FloatComparator;
import net.amygdalum.testrecorder.analyzer.testobjects.GenericCycle;
import net.amygdalum.testrecorder.analyzer.testobjects.In;
import net.amygdalum.testrecorder.analyzer.testobjects.Inner;
import net.amygdalum.testrecorder.analyzer.testobjects.InputOutput;
import net.amygdalum.testrecorder.analyzer.testobjects.IntCounter;
import net.amygdalum.testrecorder.analyzer.testobjects.Odd;
import net.amygdalum.testrecorder.analyzer.testobjects.Positive;
import net.amygdalum.testrecorder.analyzer.testobjects.Static;
import net.amygdalum.testrecorder.callsiterecorder.CallsiteRecorder;
import net.amygdalum.testrecorder.profile.ConfigurableSerializationProfile;
import net.amygdalum.testrecorder.profile.Fields;
import net.amygdalum.testrecorder.profile.Methods;
import net.amygdalum.testrecorder.profile.SerializationProfile;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class Snapshots {

	public static ContextSnapshot recordSetGlobal(Class<Static> thisObject, String argObject) throws Exception {
		Class<?>[] argtypes = {String.class};
		SerializationProfile profile = ConfigurableSerializationProfile.builder()
			.withGlobalFields(asList(Fields.byDescription(Static.class.getDeclaredField("global"))))
			.build();
		Static.global = null;
		try (CallsiteRecorder recorder = CallsiteRecorder.create(profile, Static.class.getDeclaredMethod("setGlobal", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					Static.setGlobal(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(Odd thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = {Integer.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(Odd.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(Positive thisObject, Float argObject) throws Exception {
		Class<?>[] argtypes = {Float.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(Positive.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(In thisObject, Integer argObject) throws Exception {
		Class<?>[] argtypes = {Integer.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(In.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordTest(Inner.Negate thisObject, Boolean argObject) throws Exception {
		Class<?>[] argtypes = {Boolean.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(Inner.Negate.class.getDeclaredMethod("test", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.test(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordInsert(GenericCycle<String> thisObject, GenericCycle<String> argObject) throws Exception {
		try (CallsiteRecorder recorder = CallsiteRecorder.create(GenericCycle.class.getDeclaredMethod("insert", GenericCycle.class))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.insert(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordSetAttribute(Bean thisObject, String argObject) throws Exception {
		Class<?>[] argtypes = {String.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(Bean.class.getDeclaredMethod("setAttribute", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.setAttribute(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordNext(IntCounter thisObject) throws Exception {
		Class<?>[] argtypes = {};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(IntCounter.class.getDeclaredMethod("next", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.next();
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordGetAttribute(Bean thisObject) throws Exception {
		Class<?>[] argtypes = {};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(Bean.class.getDeclaredMethod("getAttribute", argtypes))) {
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
		try (CallsiteRecorder recorder = CallsiteRecorder.create(profile, InputOutput.class.getDeclaredMethod("readLowerCase", argtypes))) {
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
		try (CallsiteRecorder recorder = CallsiteRecorder.create(profile, InputOutput.class.getDeclaredMethod("writeUpperCase", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.writeUpperCase(argObject);
				});
			return recordings.join().get(0);
		}
	}

	public static ContextSnapshot recordAreEqual(FloatComparator thisObject, Float arg1Object, Float arg2Object) throws Exception {
		Class<?>[] argtypes = {float.class, float.class};
		try (CallsiteRecorder recorder = CallsiteRecorder.create(FloatComparator.class.getDeclaredMethod("areEqual", argtypes))) {
			CompletableFuture<List<ContextSnapshot>> recordings = recorder
				.record(() -> {
					thisObject.areEqual(arg1Object, arg2Object);
				});
			return recordings.join().get(0);
		}
	}

}
