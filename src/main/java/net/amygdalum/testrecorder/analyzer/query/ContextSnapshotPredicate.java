package net.amygdalum.testrecorder.analyzer.query;

import java.util.Queue;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.values.SerializedField;
import net.amygdalum.testrecorder.values.SerializedInput;
import net.amygdalum.testrecorder.values.SerializedOutput;

public interface ContextSnapshotPredicate extends Predicate<ContextSnapshot> {

	public static ContextSnapshotPredicate onSetupThis(Predicate<SerializedValue> downstream) {
		return onSetupThis(downstream, false);
	}

	public static ContextSnapshotPredicate onSetupThis(Predicate<SerializedValue> downstream, boolean defaultOnNonExisting) {
		return snapshot -> snapshot.onSetupThis().map(downstream::test).orElse(defaultOnNonExisting);
	}
	
	public static ContextSnapshotPredicate onSetupArgs(Predicate<SerializedValue[]> downstream) {
		return snapshot -> downstream.test(snapshot.getSetupArgs());
	}

	public static ContextSnapshotPredicate onSetupArg(int index, Predicate<SerializedValue> downstream) {
		return snapshot -> index < snapshot.getSetupArgs().length
			&& downstream.test(snapshot.getSetupArgs()[index]);
	}

	public static ContextSnapshotPredicate onSetupGlobals(Predicate<SerializedField[]> downstream) {
		return snapshot -> downstream.test(snapshot.getSetupGlobals());
	}

	public static ContextSnapshotPredicate onSetupInput(Predicate<Queue<SerializedInput>> downstream) {
		return snapshot -> downstream.test(snapshot.getSetupInput());
	}

	public static ContextSnapshotPredicate onExpectThis(Predicate<SerializedValue> downstream) {
		return onExpectThis(downstream, false);
	}

	public static ContextSnapshotPredicate onExpectThis(Predicate<SerializedValue> downstream, boolean defaultNonExisting) {
		return snapshot -> snapshot.onExpectThis().map(downstream::test).orElse(defaultNonExisting);
	}
	
	public static ContextSnapshotPredicate onExpectArgs(Predicate<SerializedValue[]> downstream) {
		return snapshot -> downstream.test(snapshot.getExpectArgs());
	}

	public static ContextSnapshotPredicate onExpectArg(int index, Predicate<SerializedValue> downstream) {
		return snapshot -> index < snapshot.getExpectArgs().length
			&& downstream.test(snapshot.getExpectArgs()[index]);
	}

	public static ContextSnapshotPredicate onExpectResult(Predicate<SerializedValue> downstream, boolean defaultOnNonExisting) {
		return snapshot -> snapshot.onExpectResult().map(downstream::test).orElse(defaultOnNonExisting);
	}

	public static ContextSnapshotPredicate onExpectException(Predicate<SerializedValue> downstream, boolean defaultOnNonExisting) {
		return snapshot -> snapshot.onExpectException().map(downstream::test).orElse(defaultOnNonExisting);
	}

	public static ContextSnapshotPredicate onExpectGlobals(Predicate<SerializedField[]> downstream) {
		return snapshot -> downstream.test(snapshot.getExpectGlobals());
	}

	public static ContextSnapshotPredicate onExpectOutput(Predicate<Queue<SerializedOutput>> downstream) {
		return snapshot -> downstream.test(snapshot.getExpectOutput());
	}

}
