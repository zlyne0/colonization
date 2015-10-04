package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import promitech.colonization.infrastructure.FontResource;

class ProductionQuantityDrawer {
	private final float areaWidth;
	private final float areaHeight;
	
	private boolean centerToPoint = false;
	private int centerToX;
	private int centerToY;
	
	ProductionQuantityDrawer(float areaWidth, float areaHeight) {
		this.areaWidth = areaWidth;
		this.areaHeight = areaHeight;
	}
	
	void centerToPoint(int x, int y) {
		centerToPoint = true;
		centerToX = x;
		centerToY = y;
	}
	
	public void draw(Batch batch, ProductionQuantityDrawModel drawModel, float px, float py) {
		if (drawModel.isEmpty()) {
			return;
		}
		float yOffset = 0;
		
		for (int iy=0; iy<drawModel.size(); iy++) {
			TextureRegion texture = drawModel.images[iy];
			int quantity = drawModel.quantities[iy];
			if (quantity == 0) {
				continue;
			}
			
			float imageDistance = (areaWidth - texture.getRegionWidth()) / quantity;
			if (imageDistance > texture.getRegionWidth()) {
				imageDistance = texture.getRegionWidth();
			}
			float xRange = imageDistance * quantity + texture.getRegionWidth();
			float startOffsetX = 0;
			
			for (int i=0; i<quantity; i++) {
				if (centerToPoint) {
					batch.draw(texture, 
						px + centerToX - xRange/2 + startOffsetX, 
						py + centerToY - drawModel.yRange / 2 + yOffset
					);
				} else {
					batch.draw(texture,
						px + startOffsetX,
						py + areaHeight - drawModel.yRange + yOffset 
					);
				}
				startOffsetX += imageDistance;
			}
			if (quantity >= 5) {
				BitmapFont font = FontResource.getGoodsQuantityFont();
				if (centerToPoint) {
					font.draw(batch, Integer.toString(quantity), 
						px + centerToX - FontResource.strIntWidth(font, quantity)/2, 
						py + centerToY - drawModel.yRange / 2 + yOffset + texture.getRegionHeight() - font.getCapHeight()
					);
				} else {
					font.draw(batch, Integer.toString(quantity), 
						px + xRange/2 - FontResource.strIntWidth(font, quantity)/2,
						py + areaHeight - drawModel.yRange + yOffset + (texture.getRegionHeight()/2 + font.getCapHeight()/2)
					);
				}
			}
			yOffset += texture.getRegionHeight();
		}
	}
}
