package net.amygdalum.testrecorder.analyzer;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static net.amygdalum.testrecorder.util.Types.baseType;

import java.util.Set;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.amygdalum.testrecorder.ContextSnapshot;
import net.amygdalum.testrecorder.dynamiccompile.DynamicClassCompiler;
import net.amygdalum.testrecorder.dynamiccompile.DynamicClassCompilerException;
import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.util.Instantiations;
import net.amygdalum.testrecorder.util.WorkSet;
import net.amygdalum.testrecorder.values.SerializedObject;

public class ComputeCoverage implements UpdateProcess {

	private static final Logger LOG = LoggerFactory.getLogger(ComputeCoverage.class);

	public static final Property<Coverage> COVERAGE = new Property<>(ComputeCoverage.class, "coverage", Coverage.class);

	private Property<SourceCode> sourceCode;

	public ComputeCoverage() {
		this.sourceCode = ComputeSourceCode.SOURCE;
	}

	@Override
	public void process(TestCase testCase) throws TaskSkippedException {
		SourceCode sourceCode = this.sourceCode.from(testCase).orElseThrow(UpdateProcess::skip);

		try {
			IRuntime runtime = new LoggerRuntime();

			CoverageClassLoader loader = new CoverageClassLoader(runtime, getClass().getClassLoader());

			for (SerializedValue value : collectValues(testCase.getSnapshot())) {
				if (value instanceof SerializedObject) {
					String typeName = baseType(value.getType()).getName();
					if (!loader.isRedefined(typeName)) {
						loader.redefineClass(typeName);
					}
				}
			}

			RuntimeData data = new RuntimeData();
			runtime.startup(data);

			DynamicClassCompiler compiler = new DynamicClassCompiler();

			Class<?> compile = compiler.compile(sourceCode.getCode(), loader);
			Thread.currentThread().setContextClassLoader(loader);
			JUnitCore junit = new JUnitCore();
			Instantiations.resetInstatiations();
			Result result = junit.run(compile);

			ExecutionDataStore executionData = new ExecutionDataStore();
			SessionInfoStore sessionInfos = new SessionInfoStore();
			data.collect(executionData, sessionInfos, false);
			runtime.shutdown();

			CoverageBuilder coverageBuilder = new CoverageBuilder();
			Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
			analyzer.analyzeClass(loader.getRawBytesForClass(sourceCode.getUnitName()), sourceCode.getUnitName());

			Coverage coverage = new Coverage(result.wasSuccessful());

			for (IClassCoverage cc : coverageBuilder.getClasses()) {
				for (IMethodCoverage mc : cc.getMethods()) {
					coverage.addMethodCoverage(convertCoverage(classNameOf(cc.getPackageName(), cc.getName()), mc));
				}
			}

			COVERAGE.set(coverage).on(testCase);
		} catch (ClassNotFoundException e) {
			LOG.warn("computing coverage failed with class resolution: " + e.getMessage());
			throw UpdateProcess.skip(e);
		} catch (DynamicClassCompilerException e) {
			System.out.println(sourceCode.getCode());
			LOG.warn("computing coverage failed with compilation: " + e.getMessage() + e.getDetailMessages().stream()
				.collect(joining("\n", "\n", "")));
			throw UpdateProcess.skip(e);
		} catch (Exception e) {
			LOG.warn("computing coverage failed with coverage computation: " + e.getMessage(), e);
			throw UpdateProcess.skip(e);
		}
	}

	private MethodCoverage convertCoverage(String className, IMethodCoverage mc) {
		MethodCoverage coverage = new MethodCoverage(methodNameOf(mc.getName(), mc.getDesc()));
		coverage.setInstructionCoverage(mc.getInstructionCounter().getTotalCount(), mc.getInstructionCounter().getCoveredCount());
		coverage.setBranchCoverage(mc.getBranchCounter().getTotalCount(), mc.getBranchCounter().getCoveredCount());
		coverage.setComplexityCoverage(mc.getComplexityCounter().getTotalCount(), mc.getComplexityCounter().getCoveredCount());

		for (int i = mc.getFirstLine(); i <= mc.getLastLine(); i++) {
			ILine line = mc.getLine(i);
			coverage.addLine(convertCoverage(line, i));
		}
		return coverage;
	}

	private LineCoverage convertCoverage(ILine line, int no) {
		LineCoverage coverage = new LineCoverage(no);
		coverage.setBranchCoverage(line.getBranchCounter().getTotalCount(), line.getBranchCounter().getCoveredCount());
		coverage.setInstructionCoverage(line.getInstructionCounter().getTotalCount(), line.getInstructionCounter().getCoveredCount());
		return coverage;
	}

	private Set<SerializedValue> collectValues(ContextSnapshot snapshot) {
		WorkSet<SerializedValue> values = new WorkSet<>();
		values.add(snapshot.getSetupThis());
		values.addAll(asList(snapshot.getSetupArgs()));
		values.add(snapshot.getExpectThis());
		values.addAll(asList(snapshot.getExpectArgs()));
		while (!values.isEmpty()) {
			SerializedValue current = values.remove();
			values.addAll(current.referencedValues());
		}

		Set<SerializedValue> done = values.getDone();
		return done;
	}

	private String classNameOf(String packageName, String name) {
		return packageName + "." + name;
	}

	private String methodNameOf(String name, String desc) {
		return name + desc;
	}

}
