package net.amygdalum.testrecorder.analyzer;

public interface PropertyRangeSelector<T> extends PropertySelector {

	Index<T> indexType();

	T from();

	T to();

}
