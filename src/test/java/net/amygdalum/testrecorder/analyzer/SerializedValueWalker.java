package net.amygdalum.testrecorder.analyzer;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.values.SerializedArray;
import net.amygdalum.testrecorder.values.SerializedField;
import net.amygdalum.testrecorder.values.SerializedList;
import net.amygdalum.testrecorder.values.SerializedLiteral;
import net.amygdalum.testrecorder.values.SerializedMap;
import net.amygdalum.testrecorder.values.SerializedObject;
import net.amygdalum.testrecorder.values.SerializedSet;

public class SerializedValueWalker {

	private SerializedValue current;

	public SerializedValueWalker(SerializedValue current) {
		this.current = current;
	}

	public SerializedValueWalker index(int i) {
		if (current instanceof SerializedArray) {
			current = ((SerializedArray) current).getArray()[i];
			return this;
		}
		throw new SerializedValueWalkerException("called index(" + i + ") on " + current.getClass().getSimpleName());
	}

	public SerializedValueWalker element(int i) {
		if (current instanceof SerializedList) {
			current = ((SerializedList) current).get(i);
			return this;
		}
		throw new SerializedValueWalkerException("called element(" + i + ") on " + current.getClass().getSimpleName());
	}

	public SerializedValueWalker select(Predicate<SerializedValue> predicate) {
		if (current instanceof SerializedSet) {
			Optional<SerializedValue> selected = ((SerializedSet) current).stream()
				.filter(predicate)
				.findFirst();
			if (selected.isPresent()) {
				current = selected.get();
				return this;
			}
		}
		throw new SerializedValueWalkerException("called select(<predicate>) on " + current.getClass().getSimpleName());
	}

	public Entry entry(Predicate<SerializedValue> predicate) {
		if (current instanceof SerializedMap) {
			Optional<Map.Entry<SerializedValue, SerializedValue>> selected = ((SerializedMap) current).entrySet().stream()
				.filter(entry -> predicate.test(entry.getKey()))
				.findFirst();
			if (selected.isPresent()) {
				return new Entry(selected.get());
			}
		}
		throw new SerializedValueWalkerException("called entry(<predicate>) on " + current.getClass().getSimpleName());
	}

	public SerializedValueWalker field(String name) {
		if (current instanceof SerializedObject) {
			Optional<SerializedField> field = ((SerializedObject) current).getField(name);
			if (field.isPresent()) {
				current = field.get().getValue();
				return this;
			}
		}
		throw new SerializedValueWalkerException("called field(" + name + ") on " + current.getClass().getSimpleName());
	}

	public SerializedValue current() {
		return current;
	}

	public <T> T forField(String name, Function<SerializedField, T> function) {
		if (current instanceof SerializedObject) {
			Optional<SerializedField> field = ((SerializedObject) current).getField(name);
			if (field.isPresent()) {
				return function.apply(field.get());
			}
		}
		throw new SerializedValueWalkerException("called forField on " + current.getClass().getSimpleName());
	}

	public <T> T forObject(Function<SerializedObject, T> function) {
		if (current instanceof SerializedObject) {
			return function.apply(((SerializedObject) current));
		}
		throw new SerializedValueWalkerException("called forObject on " + current.getClass().getSimpleName());
	}

	public <T> T forLiteral(Function<SerializedLiteral, T> function) {
		if (current instanceof SerializedLiteral) {
			return function.apply(((SerializedLiteral) current));
		}
		throw new SerializedValueWalkerException("called forLiteral on " + current.getClass().getSimpleName());
	}

	public class Entry {

		private SerializedValue key;
		private SerializedValue value;

		public Entry(Map.Entry<SerializedValue, SerializedValue> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public SerializedValueWalker key() {
			SerializedValueWalker.this.current = key;
			return SerializedValueWalker.this;
		}

		public SerializedValueWalker value() {
			SerializedValueWalker.this.current = value;
			return SerializedValueWalker.this;
		}

	}

}
