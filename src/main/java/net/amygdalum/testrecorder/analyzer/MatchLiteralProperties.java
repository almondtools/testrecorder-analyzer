package net.amygdalum.testrecorder.analyzer;

import net.amygdalum.testrecorder.types.SerializedValue;
import net.amygdalum.testrecorder.values.SerializedLiteral;
import net.amygdalum.testrecorder.values.SerializedNull;

public interface MatchLiteralProperties {

	default boolean isLiteral(SerializedValue value) {
		return value instanceof SerializedLiteral
			|| value instanceof SerializedNull;
	}

	default Object extractLiteral(SerializedValue value) {
		if (value instanceof SerializedLiteral) {
			return ((SerializedLiteral) value).getValue();
		} else {
			return null;
		}
	}
}
