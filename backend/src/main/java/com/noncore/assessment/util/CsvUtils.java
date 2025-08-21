package com.noncore.assessment.util;

import java.nio.charset.StandardCharsets;

public final class CsvUtils {

	private CsvUtils() {}

	public static String nullToEmpty(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	public static String escape(String value) {
		if (value == null) return "";
		String s = value.replace("\r", " ").replace("\n", " ");
		if (s.contains(",") || s.contains("\"")) {
			return '"' + s.replace("\"", "\"\"") + '"';
		}
		return s;
	}
}


