package net.amygdalum.testrecorder.analyzer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.analyzer.request.TestCaseQuery;
import net.amygdalum.testrecorder.analyzer.request.TestCaseUpdate;

public class TestDatabase implements AutoCloseable {

	private Serialization serialization;
	private MVStore store;
	private MVMap<String, Object[][]> tests;
	private Map<Index<?>, MVMap<?, NavigableSet<String>>> indices;

	public TestDatabase(TestrecorderAnalyzerConfig config, Serialization serialization) {
		this.serialization = serialization;
		this.store = MVStore.open(config.getDatabaseFile());
		this.tests = store.openMap(config.getDatabaseCollection());
		this.indices = new ConcurrentHashMap<>();
	}

	public void prepareIndexOn(Index<?> index) {
		if (isPluggableType(index.type())) {
			String key = index.name();
			MVMap<String, NavigableSet<String>> map = store.openMap(key);
			indices.put(index, map);
		}
	}

	public String store(TestCase testCase) {
		String id = testCase.getId();
		store(id, testCase);
		store.commit();
		return id;
	}

	@SuppressWarnings("unchecked")
	private void store(String id, TestCase testCase) {
		tests.put(id, serialize(testCase));
		indices.forEach((property, index) -> {
			property.extract(testCase).ifPresent(indexValue -> {
				MVMap<Object, NavigableSet<String>> erasedIndex = (MVMap<Object, NavigableSet<String>>) index;
				Object erasedIndexValue = indexValue;
				erasedIndex.computeIfAbsent(erasedIndexValue, v -> new ConcurrentSkipListSet<>())
					.add(testCase.getId());
			});
		});
	}

	public TestCase load(String id) {
		return deserialize(id, tests.get(id));
	}

	public static boolean isPluggableType(Object value) {
		return isPluggableType(value.getClass());
	}

	private static boolean isPluggableType(Class<?> clazz) {
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
			PropertySelectors selectors = new PropertySelectors(update.selectors(), indices::containsKey);

			Stream<TestCase> stream = selectors.dispatch(
				this::selectByKey,
				this::selectByRange,
				this::selectAll);

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
		PropertySelectors selectors = new PropertySelectors(query.selectors(), indices::containsKey);

		Stream<TestCase> stream = selectors.dispatch(
			this::selectByKey,
			this::selectByRange,
			this::selectAll);

		for (Collector<TestCase, ?, Stream<TestCase>> collector : query.collectors()) {
			stream = stream.collect(collector);
		}
		return stream;
	}

	@SuppressWarnings("unchecked")
	private Stream<TestCase> selectByKey(PropertyKeySelector<?> selector) {
		MVMap<Object, NavigableSet<String>> index = (MVMap<Object, NavigableSet<String>>) indices.get(selector.indexType());

		NavigableSet<String> ids = index.get(selector.key());

		return ids.stream()
			.map(id -> deserialize(id, tests.get(id)));
	}

	@SuppressWarnings("unchecked")
	private Stream<TestCase> selectByRange(PropertyRangeSelector<?> selector) {
		MVMap<Object, NavigableSet<String>> index = (MVMap<Object, NavigableSet<String>>) indices.get(selector.indexType());
		
		NavigableSet<String> ids = new ConcurrentSkipListSet<>();
		
		Cursor<Object, NavigableSet<String>> cursor = index.cursor(selector.from());
		while (cursor.hasNext() && !cursor.getKey().equals(selector.to())) {
			ids.addAll(cursor.getValue());
		}

		return ids.stream()
			.map(id -> deserialize(id, tests.get(id)));
	}

	private Stream<TestCase> selectAll() {
		return tests.entrySet().stream()
			.map(entry -> deserialize(entry.getKey(), entry.getValue()));
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
