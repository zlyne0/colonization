package promitech.colonization.actors.colony;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

import net.sf.freecol.common.model.ProductionSummary;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;

class ProductionQuantityDrawModel {
	TextureRegion images[];
	int quantities[];
	float yRange;

	void initList(ProductionSummary summary) {
		if (summary.size() == 0) {
			quantities = null;
			images = null;
			return;
		}
		
		List<GoodsType> goodsTypes = Specification.instance.goodsTypes.sortedEntities();
		if (quantities == null || quantities.length != goodsTypes.size()) {
			images = new TextureRegion[goodsTypes.size()];
			quantities = new int[goodsTypes.size()];
		}
		yRange = 0;
		
		int i = 0;
		int quantity;
		for (GoodsType gt : goodsTypes) {
			quantity = summary.getQuantity(gt.getId());
			
			Frame image = GameResources.instance.resource(gt.getId());
			yRange += image.texture.getRegionHeight();
			images[i] = image.texture;
			quantities[i] = quantity;
			i++;
		}
	}
	
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
