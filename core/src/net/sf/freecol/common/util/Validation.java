package net.sf.freecol.common.util;

public class Validation {

	public static void instanceOf(Object obj, Class<?> clazz) {
		if (clazz.isInstance(obj) == false) {
			throw new IllegalStateException("obj: " + obj + " is not " + clazz + " type");
		}
	}

	public static void notNull(Object obj, String st) {
		if (obj == null) {
			throw new IllegalStateException("NOTNULL " + st);
		}
	}
	
}
