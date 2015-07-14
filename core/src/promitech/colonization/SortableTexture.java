package promitech.colonization;

import com.badlogic.gdx.graphics.Texture;

public class SortableTexture implements Comparable<SortableTexture> {

	public final Texture texture;
	public final int index;

	public SortableTexture(Texture texture) {
		this.texture = texture;
		// alway on top
		this.index = -10000;
	}
	
	public SortableTexture(Texture texture, int index) {
		this.texture = texture;
		this.index = index;
	}
	
	@Override
	public int compareTo(SortableTexture o) {
		return o.index - this.index;
	}

}
