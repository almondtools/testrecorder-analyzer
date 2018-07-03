package net.amygdalum.testrecorder.analyzer;

import static net.amygdalum.testrecorder.util.Types.baseType;

import java.util.List;

import net.amygdalum.testrecorder.ClassDescriptor;
import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.generator.ClassGenerator;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;
import net.amygdalum.testrecorder.types.Computation;
import net.amygdalum.testrecorder.types.Deserializer;

public class ComputeSourceCode implements UpdateProcess {

	public static final Property<SourceCode> SOURCE = new Property<>(ComputeSourceCode.class, "source", SourceCode.class);

	private Deserializer<Computation> setup;
	private Deserializer<Computation> matcher;
	private List<TestRecorderAgentInitializer> initializers;

	public ComputeSourceCode(Deserializer<Computation> setup, Deserializer<Computation> matcher, List<TestRecorderAgentInitializer> initializers) {
		this.setup = setup;
		this.matcher = matcher;
		this.initializers = initializers;
	}
	
	@Override
	public void process(TestCase testCase) throws TaskFailedException {
		try {
			ContextSnapshot snapshot = testCase.getSnapshot();

			Class<?> thisType = baseType(snapshot.getThisType());
			while (thisType.getEnclosingClass() != null) {
				thisType = thisType.getEnclosingClass();
			}
			ClassDescriptor baseType = ClassDescriptor.of(thisType);

			String pkg = baseType.getPackage();
			String name = baseType.getSimpleName();
			ClassGenerator classGenerator = new ClassGenerator(setup, matcher, initializers, pkg, name + "Test");
			classGenerator.generate(snapshot);
			String sourceCode = classGenerator.render();

			SOURCE.set(new SourceCode(pkg + '.' + name, sourceCode)).on(testCase);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

	}

}
