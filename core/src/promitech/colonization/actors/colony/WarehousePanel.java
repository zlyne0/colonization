package promitech.colonization.actors.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private int quantity = 0;
    
    private static TextureRegionDrawable getGoodTexture(GoodsType goodsType) {
        Frame img = GameResources.instance.goodsImage(goodsType);
        return new TextureRegionDrawable(img.texture);
    }
    
    WarehouseGoodActor(GoodsType goodsType, int quantity) {
        super(getGoodTexture(goodsType));
        this.goodsType = goodsType;
        this.quantity = quantity;
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        
        BitmapFont font = FontResource.getGoodsQuantityFont();
        if (quantity == 0) {
            font.setColor(Color.GRAY);
        } else {
            font.setColor(Color.WHITE);
        }
        float quantityStrLength = FontResource.strIntWidth(font, quantity);
        font.draw(batch, Integer.toString(quantity), getX() + getWidth()/2 - quantityStrLength/2, getY());
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
}

class WarehousePanel extends Table {
    private java.util.Map<GoodsType, WarehouseGoodActor> goodActorByType = new HashMap<GoodsType, WarehouseGoodActor>();
    
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
            setGoodQuantity(goodsType, goodsAmount);
        }
    }
    
    private void setGoodQuantity(GoodsType goodsType, int goodsAmount) {
        WarehouseGoodActor warehouseGoodActor = goodActorByType.get(goodsType);
        if (warehouseGoodActor == null) {
            warehouseGoodActor = new WarehouseGoodActor(goodsType, goodsAmount);
            goodActorByType.put(goodsType, warehouseGoodActor);
            add(warehouseGoodActor);
        }
        warehouseGoodActor.setQuantity(goodsAmount);
    }
}
