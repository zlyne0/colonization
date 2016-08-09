package promitech.colonization.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.actors.colony.DragAndDropSourceContainer;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

public class LabelGoodActor extends Widget {

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
    	return textureRegion.getRegionHeight() + font.getCapHeight();
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
