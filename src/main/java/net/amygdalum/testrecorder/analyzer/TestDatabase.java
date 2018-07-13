package net.amygdalum.testrecorder.analyzer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.query.TestCaseQuery;
import net.amygdalum.testrecorder.analyzer.query.TestCaseUpdate;

public class TestDatabase implements AutoCloseable {

	private Serialization serialization;
	private MVStore store;
	private MVMap<String, Object[][]> tests;

	public TestDatabase(TestrecorderAnalyzerConfig config, Serialization serialization) {
		this.serialization = serialization;
		this.store = MVStore.open(config.getDatabaseFile());
		this.tests = store.openMap(config.getDatabaseCollection());
	}

	public String store(TestCase testCase) {
		String id = testCase.getId();
		tests.put(id, serialize(testCase));
		store.commit();
		return id;
	}

	public TestCase load(String id) {
		return deserialize(id, tests.get(id));
	}

	private boolean isPluggableType(Object value) {
		Class<? extends Object> clazz = value.getClass();
		if (clazz == Byte.class) {
			return true;
		} else if (clazz == Short.class) {
			return true;
		} else if (clazz == Integer.class) {
			return true;
		} else if (clazz == Long.class) {
			return true;
		} else if (clazz == Float.class) {
			return true;
		} else if (clazz == Double.class) {
			return true;
		} else if (clazz == Boolean.class) {
			return true;
		} else if (clazz == Character.class) {
			return true;
		} else if (clazz == String.class) {
			return true;
		} else if (clazz == BigInteger.class) {
			return true;
		} else if (clazz == BigDecimal.class) {
			return true;
		} else if (clazz == UUID.class) {
			return true;
		} else if (clazz == Date.class) {
			return true;
		} else {
			return false;
		}
	}

	public void update(TestCaseUpdate update) {
		try {
			Stream<TestCase> stream = tests.entrySet().stream()
				.map(entry -> deserialize(entry.getKey(), entry.getValue()));
			for (Predicate<TestCase> selector : update.selectors()) {
				stream = stream.filter(selector);
			}
			stream.forEach(testCase -> {
				boolean changed = false;
				for (PropertyUpdate process : update.updates()) {
					changed |= process.apply(testCase);
				}
				if (changed) {
					tests.put(testCase.getId(), serialize(testCase));
				}
			});
			store.commit();
		} catch (TaskFailedException e) {
			store.rollback();
		}
	}

	public Stream<TestCase> fetch(TestCaseQuery query) {
		Stream<TestCase> stream = tests.entrySet().stream()
			.map(entry -> deserialize(entry.getKey(), entry.getValue()));
		for (Predicate<TestCase> selector : query.selectors()) {
			stream = stream.filter(selector);
		}
		for (Collector<TestCase, ?, Stream<TestCase>> collector : query.collectors()) {
			stream = stream.collect(collector);
		}
		return stream;
	}

	@Override
	public void close() {
		if (store != null) {
			store.commit();
			store.close();
		}
	}

	public int size() {
		return tests.size();
	}

	private Object[][] serialize(TestCase testCase) {
		Map<String, Object> properties = testCase.getProperties();
		ContextSnapshot snapshot = testCase.getSnapshot();

		Object[][] keyvalues = new Object[properties.size() + 1][];
		keyvalues[0] = keyvalue("snapshot", serialization.serialize(snapshot));

		int index = 1;
		for (Map.Entry<String, Object> property : properties.entrySet()) {
			String key = property.getKey();
			Object value = property.getValue();
			if (!isPluggableType(value)) {
				value = serialization.serialize(value);
			}
			keyvalues[index] = keyvalue(key, value);
			index++;
		}
		return keyvalues;
	}

	private TestCase deserialize(String id, Object[][] keyvalues) {
		ContextSnapshot snapshot = null;
		Map<String, Object> properties = new HashMap<>();

		for (Object[] keyvalue : keyvalues) {
			String key = (String) keyvalue[0];
			Object value = keyvalue[1];
			if (value instanceof byte[]) {
				value = serialization.deserialize((byte[]) value);
			}
			if (key.equals("snapshot") && value instanceof ContextSnapshot) {
				snapshot = (ContextSnapshot) value;
			} else {
				properties.put(key, value);
			}
		}

		return new TestCase(id, snapshot, properties);
	}

	private Object[] keyvalue(String key, Object value) {
		return new Object[] { key, value };
	}
	
}
