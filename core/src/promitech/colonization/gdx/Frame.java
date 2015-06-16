package promitech.colonization.gdx;

import com.badlogic.gdx.graphics.Texture;

public class Frame {
    public final int offsetX;
    public final int offsetY;
    public final Texture texture;
    
    public Frame(Texture texture, int offsetX, int offsetY) {
    	this.texture = texture;
    	this.offsetX = offsetX;
    	this.offsetY = offsetY;
    }
    
    public Frame(Texture texture) {
    	this(texture, 0, 0);
    }
}
