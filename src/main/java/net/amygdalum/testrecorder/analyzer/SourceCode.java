package net.amygdalum.testrecorder.analyzer;

import java.io.Serializable;

public class SourceCode implements Serializable {

	private String unitName;
	private String code;

	public SourceCode() {
		
	}
	
	public SourceCode(String name, String code) {
		this.unitName = name;
		this.code = code;
	}
	
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getUnitName() {
		return unitName;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

}
