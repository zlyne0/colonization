package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.ProductionSummary;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class ProductionQuantityDrawModel {
	TextureRegion images[];
	int quantities[];
	float yRange;
	
	void init(ProductionSummary summary) {
		images = null;
		quantities = null;
		
		if (summary.size() == 0) {
			return;
		}
		
		yRange = 0;
		images = new TextureRegion[summary.size()];
		quantities = new int[summary.size()];

		int i = 0;
		for (Entry<String> entry : summary.entries()) {
			Frame image = GameResources.instance.resource(entry.key);
			yRange += image.texture.getRegionHeight();
			
			images[i] = image.texture;
			quantities[i] = entry.value;
			i++;
		}
	}
	
	boolean isEmpty() {
		return images == null || images.length == 0;
	}
	
	int size() {
		return images.length;
	}
}
