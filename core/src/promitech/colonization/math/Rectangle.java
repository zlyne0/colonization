package promitech.colonization.math;

public class Rectangle {

	public int x;
	public int y;
	public int width;
	public int height;
	
	public boolean hasPoint(int px, int py) {
		return x <= px && px <= x + width && y <= py && py <= y + height;
	}
	
	public static Rectangle createFromString(String str) {
		// 1,5,10,10
		
        String tabStr[] = str.split(",");
        if (tabStr.length != 4) {
            throw new IllegalStateException("incorrect rectangle string value (only " + tabStr.length + " points): " + str);
        }
		
        Rectangle r = new Rectangle();
        r.x = Integer.parseInt(tabStr[0]);
        r.y = Integer.parseInt(tabStr[1]);
        r.width = Integer.parseInt(tabStr[2]);
        r.height = Integer.parseInt(tabStr[3]);
        return r;
	}
	
}
