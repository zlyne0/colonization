package promitech.colonization.math;

import com.badlogic.gdx.math.Vector2;

public class Point {
    public int x;
    public int y;
    
    public Point() {
    }
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Point set(int x, int y) {
    	this.x = x;
    	this.y = y;
    	return this;
    }
    
    public Point minus(int x, int y) {
    	this.x -= x;
    	this.y -= y;
    	return this;
    }
    
    public boolean equals(Vector2 v) {
    	return x == v.x && y == v.y;
    }
    
    public boolean equals(int x, int y) {
    	return this.x == x && this.y == y;
    }
    
    public String toString() {
    	return "[" + x + ", " + y + "]";
    }
}
