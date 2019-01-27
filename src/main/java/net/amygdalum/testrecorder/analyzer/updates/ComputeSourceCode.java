package net.amygdalum.testrecorder.analyzer.updates;

import static net.amygdalum.testrecorder.util.Types.baseType;

import java.util.List;

import net.amygdalum.testrecorder.analyzer.PropertyUpdate;
import net.amygdalum.testrecorder.analyzer.SourceCode;
import net.amygdalum.testrecorder.analyzer.SyntheticProperty;
import net.amygdalum.testrecorder.analyzer.TaskFailedException;
import net.amygdalum.testrecorder.analyzer.TestCase;
import net.amygdalum.testrecorder.deserializers.CustomAnnotation;
import net.amygdalum.testrecorder.deserializers.builder.SetupGenerators;
import net.amygdalum.testrecorder.deserializers.matcher.MatcherGenerators;
import net.amygdalum.testrecorder.generator.ClassGenerator;
import net.amygdalum.testrecorder.generator.JUnit4TestTemplate;
import net.amygdalum.testrecorder.generator.TestTemplate;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.util.ClassDescriptor;

public class ComputeSourceCode implements PropertyUpdate {

	public static final SyntheticProperty<SourceCode> SOURCE = new SyntheticProperty<>(ComputeSourceCode.class, "source", SourceCode.class);

	private SetupGenerators setup;
	private MatcherGenerators matcher;
	private List<CustomAnnotation> annotations;

	public ComputeSourceCode(SetupGenerators setup, MatcherGenerators matcher, List<CustomAnnotation> annotations) {
		this.setup = setup;
		this.matcher = matcher;
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
			TestTemplate template = new JUnit4TestTemplate();
			ClassGenerator classGenerator = new ClassGenerator(setup, matcher, template, annotations, pkg, name + "Test");
			classGenerator.generate(snapshot);
			String sourceCode = classGenerator.render();

			return SOURCE.set(new SourceCode(pkg + '.' + name, sourceCode)).on(testCase);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

	}

}
