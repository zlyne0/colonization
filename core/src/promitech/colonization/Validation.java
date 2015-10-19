package promitech.colonization;

public class Validation {

	public static void instanceOf(Object obj, Class<?> clazz) {
		if (clazz.isInstance(obj) == false) {
			throw new IllegalStateException("obj: " + obj + " is not " + clazz + " type");
		}
	}
	
}
