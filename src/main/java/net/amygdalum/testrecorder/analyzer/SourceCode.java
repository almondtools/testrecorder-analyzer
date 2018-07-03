package net.amygdalum.testrecorder.analyzer;

import java.io.Serializable;

public class SourceCode implements Serializable {

	private String unitName;
	private String code;

	public SourceCode(String name, String code) {
		this.unitName = name;
		this.code = code;
	}

	public String getUnitName() {
		return unitName;
	}

	public String getCode() {
		return code;
	}

}
