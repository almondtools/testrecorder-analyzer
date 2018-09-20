package net.amygdalum.testrecorder.analyzer.updates;

import static net.amygdalum.testrecorder.util.Types.baseType;

import java.util.List;

import net.amygdalum.testrecorder.ClassDescriptor;
import net.amygdalum.testrecorder.analyzer.PropertyUpdate;
import net.amygdalum.testrecorder.analyzer.SourceCode;
import net.amygdalum.testrecorder.analyzer.SyntheticProperty;
import net.amygdalum.testrecorder.analyzer.TaskFailedException;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.deserializers.CustomAnnotation;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.generator.ClassGenerator;
import net.amygdalum.testrecorder.runtime.TestRecorderAgentInitializer;
import net.amygdalum.testrecorder.types.ContextSnapshot;

public class ComputeSourceCode implements PropertyUpdate {

	public static final SyntheticProperty<SourceCode> SOURCE = new SyntheticProperty<>(ComputeSourceCode.class, "source", SourceCode.class);

	private SetupGenerators setup;
	private MatcherGenerators matcher;
	private List<TestRecorderAgentInitializer> initializers;
	private List<CustomAnnotation> annotations;

	public ComputeSourceCode(SetupGenerators setup, MatcherGenerators matcher, List<TestRecorderAgentInitializer> initializers, List<CustomAnnotation> annotations) {
		this.setup = setup;
		this.matcher = matcher;
		this.initializers = initializers;
		this.annotations = annotations;
	}
	
	@Override
	public boolean apply(TestCase testCase) {
		try {
			ContextSnapshot snapshot = testCase.getSnapshot();

			Class<?> thisType = baseType(snapshot.getThisType());
			while (thisType.getEnclosingClass() != null) {
				thisType = thisType.getEnclosingClass();
			}
			ClassDescriptor baseType = ClassDescriptor.of(thisType);

			String pkg = baseType.getPackage();
			String name = baseType.getSimpleName();
			ClassGenerator classGenerator = new ClassGenerator(setup, matcher, initializers, annotations, pkg, name + "Test");
			classGenerator.generate(snapshot);
			String sourceCode = classGenerator.render();

			return SOURCE.set(new SourceCode(pkg + '.' + name, sourceCode)).on(testCase);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

	}

}
