package net.amygdalum.testrecorder.analyzer;

import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Coverage implements Serializable {

	private boolean success;
	private List<MethodCoverage> methods;

	public Coverage(boolean success) {
		this.success = success;
		this.methods = new ArrayList<>();
	}

	public boolean isSuccess() {
		return success;
	}

	public List<MethodCoverage> getMethods() {
		return methods;
	}

	public Optional<MethodCoverage> getMethodCoverage(String name) {
		return methods.stream()
			.filter(method -> method.getName().equals(name))
			.findFirst();
	}

	public Coverage addMethodCoverage(MethodCoverage coverage) {
		methods.add(coverage);
		return this;
	}

	public Optional<LineCoverage> getLineCoverage(int line) {
		return methods.stream()
			.flatMap(method -> method.getLines().stream())
			.filter(l -> l.getLine() == line)
			.findFirst();
	}
	
	@Override
	public String toString() {
		return methods.stream()
			.map(MethodCoverage::toString)
			.collect(joining("\n\t", "{\n\t","\n}"))
			+ (success ? ":success" : ":failed");
	}
	
	@Override
	public int hashCode() {
		return (success ? 1 : 0)
			+ Objects.hashCode(methods);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Coverage that = (Coverage) obj;
		return this.success == that.success
			&& Objects.equals(this.methods, that.methods);
	}

}
