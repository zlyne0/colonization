package promitech.colonization.screen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;
import promitech.colonization.screen.colony.DragAndDropSourceContainer;

public class LabelGoodActor extends Widget {

	private static final float GOOD_IMG_WIDTH = 32;
	
	protected static class DragAndDropPayload {
		AbstractGoods abstractGoods;
		boolean changeTransferGoodsQuantity = false;
		
		public DragAndDropPayload(AbstractGoods abstractGoods, boolean changeTransferGoodsQuantity) {
			this.abstractGoods = abstractGoods;
			this.changeTransferGoodsQuantity = changeTransferGoodsQuantity;
		}
	}
	
	protected final GoodsType goodsType;
	protected final TextureRegion textureRegion;
	protected BitmapFont font;
	protected String label = "";
    public DragAndDropSourceContainer<AbstractGoods> dragAndDropSourceContainer;
    
    public static float goodsImgWidth() {
    	return GOOD_IMG_WIDTH;
    }
    
    public static float goodsImgHeight() {
    	return FontResource.getGoodsQuantityFont().getCapHeight() + GOOD_IMG_WIDTH;
    }
    
	public LabelGoodActor(GoodsType goodsType) {
		this.goodsType = goodsType;
		
		Frame img = GameResources.instance.goodsImage(goodsType);
		this.textureRegion = img.texture;
		this.font = FontResource.getGoodsQuantityFont();
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
    @Override
    public float getPrefHeight() {
    	return goodsImgHeight();
    }
    
    @Override
    public float getPrefWidth() {
        return Math.max(textureRegion.getRegionWidth(), FontResource.strWidth(font, label));
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
    	float halfOfImgAndStrHeight = (textureRegion.getRegionHeight() + font.getCapHeight()) / 2;
    	batch.draw(
    	    textureRegion, 
    	    getX() + getWidth()/2 - textureRegion.getRegionWidth()/2,
    	    getY() + getHeight()/2 + halfOfImgAndStrHeight - textureRegion.getRegionHeight()
    	);
    	
        float priceStrWidth = FontResource.strWidth(font, label);
        font.setColor(getQuantityColor());
        font.draw(batch, label, 
        		getX() + getWidth()/2 - priceStrWidth/2,
        		getY() + getHeight()/2 - halfOfImgAndStrHeight + font.getCapHeight()  
        );
    }

    public Color getQuantityColor() {
        return Color.WHITE;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
    
	public String toString() {
		return "type[" + goodsType + "], label[" + label + "]";
	}

	public GoodsType getGoodsType() {
		return goodsType;
	}
}
