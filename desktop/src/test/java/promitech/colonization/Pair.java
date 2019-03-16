package promitech.colonization;

public class Pair {
	private Object obj1;
	private Object obj2;
	
	public static Pair of(Object obj1, Object obj2) {
		return new Pair(obj1, obj2);
	}
	
	public Pair(Object obj1, Object obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	public <T1> T1 getObj1() {
		return (T1)obj1;
	}
	
	public <T2> T2 getObj2() {
		return (T2)obj2;
	}
}