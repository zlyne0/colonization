package promitech.colonization.gdx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Frame implements Comparable<Frame> {
	
	public static final int DEEPEST_ORDER = -10000; 
	
    public final int offsetX;
    public final int offsetY;
    public final TextureRegion texture;
    public final int zIndex;
    public String key;
    
    public Frame(TextureRegion texture, int offsetX, int offsetY, int zIndex) {
    	this.texture = texture;
    	this.offsetX = offsetX;
    	this.offsetY = offsetY;
    	this.zIndex = zIndex;
    }
    
    public Frame(TextureRegion texture, int offsetX, int offsetY) {
    	this(texture, offsetX, offsetY, DEEPEST_ORDER);
    }
    
    public Frame(TextureRegion texture) {
    	this(texture, 0, 0, DEEPEST_ORDER);
    }

    public Frame(TextureRegion texture, int zIndex) {
    	this(texture, 0, 0, zIndex);
    }
    
    public String toString() {
    	String st = "frame zindex = " + zIndex;
    	if (key != null) {
    		st += ", key = " + key;
    	}
    	return st;
    }
    
	@Override
	public int compareTo(Frame o) {
		return o.zIndex - this.zIndex;
	}
    
}
