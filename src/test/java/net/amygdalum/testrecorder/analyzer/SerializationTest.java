package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;
import static net.amygdalum.testrecorder.util.Types.parameterized;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.ConfigurableSerializerFacade;
import net.amygdalum.testrecorder.DefaultSerializerSession;
import net.amygdalum.testrecorder.profile.AgentConfiguration;
import net.amygdalum.testrecorder.profile.Classes;
import net.amygdalum.testrecorder.serializers.SerializerFacade;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.types.SerializerSession;
import net.amygdalum.testrecorder.values.SerializedArray;
import net.amygdalum.testrecorder.values.SerializedEnum;
import net.amygdalum.testrecorder.values.SerializedField;
import net.amygdalum.testrecorder.values.SerializedImmutable;
import net.amygdalum.testrecorder.values.SerializedLambdaObject;
import net.amygdalum.testrecorder.values.SerializedList;
import net.amygdalum.testrecorder.values.SerializedLiteral;
import net.amygdalum.testrecorder.values.SerializedMap;
import net.amygdalum.testrecorder.values.SerializedNull;
import net.amygdalum.testrecorder.values.SerializedObject;
import net.amygdalum.testrecorder.values.SerializedProxy;
import net.amygdalum.testrecorder.values.SerializedSet;

public class SerializationTest {

	private static AgentConfiguration config;
	private static SerializerFacade facade;

	private SerializerSession session;
	private Serialization storage;

	@BeforeAll
	static void beforeAll() {
		config = TestAgentConfiguration.defaultConfig();
		facade = new ConfigurableSerializerFacade(config);
	}

	@BeforeEach
	void before() throws Exception {
		session = facade.newSession();
		storage = new Serialization();
	}

	@Test
	void testSerializedStringLiteral() throws Exception {
		SerializedLiteral literal = serialize("string", SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo("string");
	}

	@Test
	void testSerializedByteLiteral() throws Exception {
		SerializedLiteral literal = serialize((byte) 1, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo((byte) 1);
	}

	@Test
	void testSerializedShortLiteral() throws Exception {
		SerializedLiteral literal = serialize((short) 20000, SerializedLiteral.class);
		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo((short) 20000);
	}

	@Test
	void testSerializedIntLiteral() throws Exception {
		SerializedLiteral literal = serialize(-30000, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo(-30000);
	}

	@Test
	void testSerializedLongLiteral() throws Exception {
		SerializedLiteral literal = serialize(3000000l, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo(3000000l);
	}

	@Test
	void testSerializedFloatLiteral() throws Exception {
		SerializedLiteral literal = serialize(-1.2f, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo(-1.2f);
	}

	@Test
	void testSerializedDoubleLiteral() throws Exception {
		SerializedLiteral literal = serialize(1.2e-14d, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo(1.2e-14d);
	}

	@Test
	void testSerializedBooleanLiteral() throws Exception {
		SerializedLiteral literal = serialize(true, SerializedLiteral.class);

		assertThat(serializationRoundTrip(literal).getValue()).isEqualTo(true);
	}

	@Test
	void testSerializedNullOfTypeString() throws Exception {
		SerializedNull nullValue = SerializedNull.nullInstance();
		nullValue.useAs(String.class);

		assertThat(serializationRoundTrip(nullValue).getUsedTypes()).contains(String.class);
	}

	@Test
	void testSerializedNullOfTypeObject() throws Exception {
		SerializedNull nullValue = SerializedNull.nullInstance();
		nullValue.useAs(Object.class);

		assertThat(serializationRoundTrip(nullValue).getUsedTypes()).contains(Object.class);
	}

	@Test
	void testSerializedEnum() throws Exception {
		SerializedEnum value = serialize(MyEnum.FIRST, SerializedEnum.class);

		assertThat(serializationRoundTrip(value).getName()).isEqualTo(MyEnum.FIRST.toString());
	}

	@Test
	void testSerializedImmutableOfTypeBigInteger() throws Exception {
		SerializedImmutable<?> value = serialize(BigInteger.valueOf(111), SerializedImmutable.class);

		assertThat(serializationRoundTrip(value).getValue()).isEqualTo(BigInteger.valueOf(111));
	}

	@Test
	void testSerializedImmutableOfTypeClass() throws Exception {
		SerializedImmutable<?> value = serialize(String.class, SerializedImmutable.class);

		assertThat(serializationRoundTrip(value).getValue()).isEqualTo(String.class);
	}

	@Test
	void testSerializedLambda() throws Exception {
		MyLambda lambda = (MyLambda & Serializable) x -> x * 2;
		SerializedLambdaObject value = serialize(lambda, SerializedLambdaObject.class);

		assertThat(serializationRoundTrip(value).getSignature().deserialize(MyLambda.class).function(2)).isEqualTo(4);
	}

	@Test
	void testSerializedCapturingLambda() throws Exception {
		int var = 3;
		MyLambda lambda = (MyLambda & Serializable) x -> x * var;
		SerializedLambdaObject value = serialize(lambda, SerializedLambdaObject.class);

		assertThat(serializationRoundTrip(value).getSignature().deserialize(MyLambda.class, 3).function(2)).isEqualTo(6);
	}

	@Test
	void testSerializedProxy() throws Exception {
		MyProxied lambda = (MyProxied) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { MyProxied.class }, new ProxyHandler());

		SerializedProxy value = serialize(lambda, SerializedProxy.class);

		SerializedValue invocationHandler = serializationRoundTrip(value).getInvocationHandler();
		assertThat(invocationHandler.getType()).isEqualTo(ProxyHandler.class);
	}

	@Test
	void testSerializedPrimitiveArray() throws Exception {
		SerializedArray value = serialize(new int[] { 1, 2, 3, 4 }, SerializedArray.class);

		SerializedArray array = serializationRoundTrip(value);
		assertThat(array.getArray()).hasSize(4);
		assertThat(((SerializedLiteral) array.getArray()[0]).getValue()).isEqualTo(1);
		assertThat(((SerializedLiteral) array.getArray()[1]).getValue()).isEqualTo(2);
		assertThat(((SerializedLiteral) array.getArray()[2]).getValue()).isEqualTo(3);
		assertThat(((SerializedLiteral) array.getArray()[3]).getValue()).isEqualTo(4);
	}

	@Test
	void testSerializedObjectArray() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;
		SerializedArray value = serialize(new Object[] { simple, nested, recursive }, SerializedArray.class);

		SerializedArray array = serializationRoundTrip(value);
		assertThat(array.getArray()).hasSize(3);
		assertThat(new SerializedValueWalker(array).index(0).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(array).index(0).field("field").current())
			.isSameAs(new SerializedValueWalker(array).index(1).field("field").field("field").current());
		assertThat(new SerializedValueWalker(array).index(2).current())
			.isSameAs(new SerializedValueWalker(array).index(2).field("field").field("field").current());
	}

	@Test
	void testSerializedSimpleObject() throws Exception {
		Simple simple = new Simple(2);
		SerializedObject value = serialize(simple, SerializedObject.class);

		SerializedObject object = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(object).forObject(SerializedObject::getFields)).hasSize(1);
		assertThat(new SerializedValueWalker(object).forField("field", SerializedField::getType)).isEqualTo(int.class);
		assertThat(new SerializedValueWalker(object).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
	}

	@Test
	void testSerializedNestedObject() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		SerializedObject value = serialize(nested, SerializedObject.class);

		SerializedObject object = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(object).forObject(SerializedObject::getFields)).hasSize(2);
		assertThat(new SerializedValueWalker(object).forField("i", SerializedField::getType)).isEqualTo(int.class);
		assertThat(new SerializedValueWalker(object).forField("field", SerializedField::getType)).isEqualTo(Simple.class);
		assertThat(new SerializedValueWalker(object).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(3);
		assertThat(new SerializedValueWalker(object).field("field").field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
	}

	@Test
	void testSerializedRecursiveObject() throws Exception {
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;
		SerializedObject value = serialize(recursive, SerializedObject.class);

		SerializedObject object = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(object).forObject(SerializedObject::getFields)).hasSize(2);
		assertThat(new SerializedValueWalker(object).forField("i", SerializedField::getType)).isEqualTo(int.class);
		assertThat(new SerializedValueWalker(object).forField("field", SerializedField::getType)).isEqualTo(Recursive.class);
		assertThat(new SerializedValueWalker(object).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(object).field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(object).field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(object).current());
	}

	@Test
	void testSerializedList() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		List<Object> val = new ArrayList<>();
		val.add(simple);
		val.add(recursive);
		val.add(nested);

		SerializedList value = serialize(val, SerializedList.class);

		SerializedList list = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(list).element(0).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(list).element(1).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(list).element(1).field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(list).element(1).field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(list).element(1).current());
		assertThat(new SerializedValueWalker(list).element(2).field("field").current())
			.isSameAs(new SerializedValueWalker(list).element(0).current());
	}

	@Test
	void testSerializedGenericList() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		List<Object> val = new ArrayList<>();
		val.add(simple);
		val.add(recursive);
		val.add(nested);

		SerializedList value = serialize(parameterized(ArrayList.class, null, Object.class), val, SerializedList.class);

		SerializedList list = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(list).element(0).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(list).element(1).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(list).element(1).field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(list).element(1).field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(list).element(1).current());
		assertThat(new SerializedValueWalker(list).element(2).field("field").current())
			.isSameAs(new SerializedValueWalker(list).element(0).current());
	}

	@Test
	void testSerializedSet() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		Set<Object> val = new HashSet<>();
		val.add(simple);
		val.add(nested);
		val.add(recursive);

		SerializedSet value = serialize(val, SerializedSet.class);

		SerializedSet set = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(set).select(ofType(Simple.class)).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(set).select(ofType(Nested.class)).field("field").current())
			.isSameAs(new SerializedValueWalker(set).select(ofType(Simple.class)).current());
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(set).select(ofType(Recursive.class)).current());
	}

	@Test
	void testSerializedGenericSet() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		Set<Object> val = new HashSet<>();
		val.add(simple);
		val.add(nested);
		val.add(recursive);

		SerializedSet value = serialize(parameterized(HashSet.class, null, Object.class), val, SerializedSet.class);

		SerializedSet set = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(set).select(ofType(Simple.class)).field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(set).select(ofType(Nested.class)).field("field").current())
			.isSameAs(new SerializedValueWalker(set).select(ofType(Simple.class)).current());
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(set).select(ofType(Recursive.class)).field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(set).select(ofType(Recursive.class)).current());
	}

	@Test
	void testSerializedMap() throws Exception {
		String label = "label";
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		Map<Object, Object> val = new HashMap<>();
		val.put(simple, nested);
		val.put(label, recursive);

		SerializedMap value = serialize(val, SerializedMap.class);

		SerializedMap map = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(map).entry(ofType(Simple.class)).key().field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(map).entry(ofType(Simple.class)).value().field("field").current())
			.isSameAs(new SerializedValueWalker(map).entry(ofType(Simple.class)).key().current());
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(map).entry(ofType(String.class)).value().current());
	}

	@Test
	void testSerializedGenericMap() throws Exception {
		String label = "label";
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		Recursive recursive = new Recursive(4, new Recursive(5, null));
		recursive.field.field = recursive;

		Map<Object, Object> val = new HashMap<>();
		val.put(simple, nested);
		val.put(label, recursive);

		SerializedMap value = serialize(parameterized(HashMap.class, null, Object.class, Object.class), val, SerializedMap.class);

		SerializedMap map = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(map).entry(ofType(Simple.class)).key().field("field").forLiteral(SerializedLiteral::getValue)).isEqualTo(2);
		assertThat(new SerializedValueWalker(map).entry(ofType(Simple.class)).value().field("field").current())
			.isSameAs(new SerializedValueWalker(map).entry(ofType(Simple.class)).key().current());
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(4);
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("field").field("i").forLiteral(SerializedLiteral::getValue)).isEqualTo(5);
		assertThat(new SerializedValueWalker(map).entry(ofType(String.class)).value().field("field").field("field").current())
			.isSameAs(new SerializedValueWalker(map).entry(ofType(String.class)).value().current());
	}

	@Test
	void testSerializedPlaceholder() throws Exception {
		Simple simple = new Simple(2);
		Nested nested = new Nested(3, simple);
		SerializedObject value = serializePlaceholder(nested, SerializedObject.class);

		SerializedObject object = serializationRoundTrip(value);
		assertThat(new SerializedValueWalker(object).forObject(SerializedObject::getType)).isEqualTo(Nested.class);
	}

	private <T extends SerializedValue> T serialize(Object value, Class<T> clazz) {
		return clazz.cast(facade.serialize(value.getClass(), value, session));
	}

	private <T extends SerializedValue> T serialize(Type type, Object value, Class<T> clazz) {
		return clazz.cast(facade.serialize(type, value, session));
	}

	private <T extends SerializedValue> T serializePlaceholder(Object value, Class<T> clazz) {
		return clazz.cast(facade.serialize(value.getClass(), value, new DefaultSerializerSession()
			.withClassFacades(asList(Classes.byDescription(clazz)))));
	}

	@SuppressWarnings("unchecked")
	private <T extends SerializedValue> T serializationRoundTrip(T value) {
		byte[] bytes = storage.serialize(value);
		return (T) storage.deserialize(bytes);
	}

	private Predicate<SerializedValue> ofType(Type type) {
		return value -> value.getType() == type;
	}

	public static class Simple {
		public int field;

		public Simple(int field) {
			this.field = field;
		}

	}

	public static class Nested {
		public int i;
		public Simple field;

		public Nested(int i, Simple field) {
			this.i = i;
			this.field = field;
		}
	}

	public static class Recursive {
		public int i;
		public Recursive field;

		public Recursive(int i, Recursive field) {
			this.i = i;
			this.field = field;
		}
	}

	public static enum MyEnum {
		FIRST, SECOND;
	}

	public interface MyLambda {
		int function(int arg);
	}

	public interface MyProxied {
		int get();

		void set(int value);
	}

	public static class ProxyHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("set")) {
				return null;
			} else if (method.getName().equals("get")) {
				return 0;
			} else {
				return null;
			}
		}
	}

}
