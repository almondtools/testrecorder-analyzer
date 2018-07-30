package net.amygdalum.testrecorder.analyzer;


public interface PropertyKeySelector<T> extends PropertySelector {

	Index<T> indexType();
	
	T key();
	
}
