package promitech.colonization.actors.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.gdx.Frame;
import promitech.colonization.infrastructure.FontResource;

class WarehouseGoodActor extends ImageButton {
    
    private final GoodsType goodsType;
    private int amount = 0;
    
    private static TextureRegionDrawable getGoodTexture(GoodsType goodsType) {
        Frame img = GameResources.instance.goodsImage(goodsType);
        return new TextureRegionDrawable(img.texture);
    }
    
    WarehouseGoodActor(GoodsType goodsType, int amount) {
        super(getGoodTexture(goodsType));
        this.goodsType = goodsType;
        this.amount = amount;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        BitmapFont font = FontResource.getBuildingGoodsQuantityFont();
        if (amount == 0) {
            font.setColor(Color.GRAY);
        } else {
            font.setColor(Color.WHITE);
        }
        float quantityStrLength = quantityStrLength(font);
        font.draw(batch, Integer.toString(amount), getX() + getWidth()/2 - quantityStrLength/2, getY());
    }
    
    private float quantityStrLength(BitmapFont font) {
    	String str = "5";
    	if (amount >= 100) {
    		str = "555";
    	} else {
    		if (amount < 10) {
    			str = "5";
    		} else {
    			str = "55";
    		}
    	}
    	return FontResource.strWidth(font, str);
    }
}

class WarehousePanel extends Table {
    WarehousePanel() {
    }

    public void initGoods(Specification specification, Colony colony) {
        List<GoodsType> goodsTypes = new ArrayList<GoodsType>(specification.goodsTypes.entities());
        Collections.sort(goodsTypes, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
        
        defaults().space(20);
        
        for (GoodsType goodsType : goodsTypes) {
            if (!goodsType.isStorable()) {
                continue;
            }
            System.out.println("goodsType: " + goodsType.getId() + ", " + goodsType.isStorable() + ", " + goodsType.getInsertOrder());
            
            int goodsAmount = colony.getGoodsContainer().goodsAmount(goodsType);
            WarehouseGoodActor goodActor = new WarehouseGoodActor(goodsType, goodsAmount);
            add(goodActor);            
        }
    }
}
