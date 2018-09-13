package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import net.amygdalum.testrecorder.types.SerializedArgument;
import net.amygdalum.testrecorder.types.SerializedField;
import net.amygdalum.testrecorder.types.SerializedResult;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.util.Exceptional;
import net.amygdalum.testrecorder.values.SerializedArray;
import net.amygdalum.testrecorder.values.SerializedList;
import net.amygdalum.testrecorder.values.SerializedLiteral;
import net.amygdalum.testrecorder.values.SerializedMap;
import net.amygdalum.testrecorder.values.SerializedNull;
import net.amygdalum.testrecorder.values.SerializedObject;
import net.amygdalum.testrecorder.values.SerializedSet;

public class SerializedValueWalker {

	private SerializedValue current;
	private List<String> error;

	private SerializedValueWalker(String... errors) {
		this.error = asList(errors);
	}

	public SerializedValueWalker(SerializedValue current) {
		this.current = current;
	}
	
	public SerializedValueWalker(SerializedField current) {
		this.current = current.getValue();
	}

	public SerializedValueWalker(SerializedArgument current) {
		this.current = current.getValue();
	}
	
	public SerializedValueWalker(SerializedResult current) {
		this.current = current.getValue();
	}
	
	public static SerializedValueWalker start(SerializedArgument[] array, int index) {
		if (array == null || index >= array.length) {
			return SerializedValueWalker.fail("no values to start from");
		}
		SerializedArgument start = array[index];
		if (start == null) {
			return SerializedValueWalker.fail("null value to start from");
		}
		return new SerializedValueWalker(start);
	}

	public static SerializedValueWalker start(SerializedField[] array, int index) {
		if (array == null || index >= array.length) {
			return SerializedValueWalker.fail("no fields to start from");
		}
		SerializedField start = array[index];
		if (start == null) {
			return SerializedValueWalker.fail("null field to start from");
		}
		return new SerializedValueWalker(start);
	}

	private static SerializedValueWalker fail(String... msg) {
		return new SerializedValueWalker(msg);
	}

	private SerializedValueWalker error(String msg) {
		if (error == null) {
			error = new ArrayList<String>();
			current = null;
		}
		error.add(msg);
		return this;
	}

	public Entry errorEntry(String msg) {
		if (error == null) {
			error = new ArrayList<String>();
			current = null;
		}
		error.add(msg);
		return new Entry();
	}

	public SerializedValueWalkerException exception() {
		return new SerializedValueWalkerException(error);
	}

	public SerializedValueWalker index(int i) {
		if (current == null) {
			return error("called index(" + i + ") on null");
		} else if (current instanceof SerializedArray) {
			SerializedValue[] base = ((SerializedArray) current).getArray();
			if (i >= base.length) {
				return error("called index(" + i + ") on array of size " + base.length);
			}
			current = base[i];
			return this;
		}
		return error("called index(" + i + ") on " + currentName());
	}

	public SerializedValueWalker element(int i) {
		if (current == null) {
			return error("called element(" + i + ") on null");
		} else if (current instanceof SerializedList) {
			SerializedList base = (SerializedList) current;
			if (i >= base.size()) {
				return error("called element(" + i + ") on list of size " + base.size());
			}
			current = base.get(i);
			return this;
		}
		return error("called element(" + i + ") on " + currentName());
	}

	public SerializedValueWalker select(Predicate<SerializedValue> predicate) {
		if (current == null) {
			return error("called select(<predicate>) on null");
		} else if (current instanceof SerializedSet) {
			SerializedSet base = (SerializedSet) current;
			Optional<SerializedValue> selected = base.stream()
				.filter(predicate)
				.findFirst();
			if (selected.isPresent()) {
				current = selected.get();
				return this;
			} else {
				return error("called select(<predicate>) on set of non-matching elements");
			}
		}
		return error("called select(<predicate>) on " + currentName());
	}

	public Entry entry(Predicate<SerializedValue> predicate) {
		if (current == null) {
			return errorEntry("called entry(<predicate>) on null");
		} else if (current instanceof SerializedMap) {
			SerializedMap base = (SerializedMap) current;
			Optional<Map.Entry<SerializedValue, SerializedValue>> selected = base.entrySet().stream()
				.filter(entry -> predicate.test(entry.getKey()))
				.findFirst();
			if (selected.isPresent()) {
				return new Entry(selected.get());
			} else {
				return errorEntry("called entry(<predicate>) on map of non-matching keys");
			}
		}
		return errorEntry("called entry(<predicate>) on " + currentName());
	}

	public SerializedValueWalker field(String name) {
		if (current == null) {
			return error("called field(" + name + ") on null");
		} else if (current instanceof SerializedObject) {
			SerializedObject base = (SerializedObject) current;
			Optional<SerializedField> field = base.getField(name);
			if (field.isPresent()) {
				current = field.get().getValue();
				return this;
			} else {
				return error("called field(" + name + ") on object with fields " + base.getFields().stream().map(SerializedField::getName).collect(joining(", ", "[", "]")));
			}
		}
		return error("called field(" + name + ") on " + currentName());
	}

	public SerializedValue current() {
		return current;
	}

	public <T> Exceptional<T> forCurrent(Function<SerializedValue, T> func) {
		if (error != null) {
			return Exceptional.throwing(exception());
		}
		return Exceptional.success(func.apply(current));
	}

	public <T> Exceptional<T> forObject(Function<SerializedObject, T> function) {
		if (error != null) {
			return Exceptional.throwing(exception());
		} else if (current instanceof SerializedObject) {
			return Exceptional.success(function.apply(((SerializedObject) current)));
		}
		return Exceptional.throwing(error("called forObject on " + currentName()).exception());
	}

	public <T> Exceptional<T> forLiteral(Function<SerializedLiteral, T> function) {
		if (error != null) {
			return Exceptional.throwing(exception());
		} else if (current instanceof SerializedLiteral) {
			return Exceptional.success(function.apply(((SerializedLiteral) current)));
		}
		return Exceptional.throwing(error("called forLiteral on " + currentName()).exception());
	}

	public <T> Exceptional<T> forNull(Function<SerializedNull, T> function) {
		if (error != null) {
			return Exceptional.throwing(exception());
		} else if (current instanceof SerializedNull) {
			return Exceptional.success(function.apply(((SerializedNull) current)));
		}
		return Exceptional.throwing(error("called forLiteral on " + currentName()).exception());
	}

	public <T> Exceptional<T> forField(String name, Function<SerializedField, T> function) {
		if (error != null) {
			return Exceptional.throwing(exception());
		} else if (current instanceof SerializedObject) {
			Optional<SerializedField> field = ((SerializedObject) current).getField(name);
			if (field.isPresent()) {
				return Exceptional.success(function.apply(field.get()));
			}
		}
		return Exceptional.throwing(error("called forField on " + currentName()).exception());
	}

	private String currentName() {
		if (current == null) {
			return "null";
		}
		return current.getClass().getSimpleName();
	}

	public class Entry {

		private SerializedValue key;
		private SerializedValue value;

		public Entry(Map.Entry<SerializedValue, SerializedValue> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		public Entry() {
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
