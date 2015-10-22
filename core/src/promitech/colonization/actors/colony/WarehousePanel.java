package promitech.colonization.actors.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ObjectWithId;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;

class WarehousePanel extends Table implements DragAndDropSourceContainer<AbstractGoods>, DragAndDropTargetContainer<AbstractGoods> {
    private java.util.Map<String, GoodActor> goodActorByType = new HashMap<String, GoodActor>();
    
    private Colony colony;
    
    WarehousePanel() {
    }

    public void initGoods(Specification specification, Colony aColony, DragAndDrop goodsDragAndDrop) {
        this.colony = aColony;
        
        goodsDragAndDrop.addTarget(new GoodActor.GoodsDragAndDropTarget(this, this));
        
        List<GoodsType> goodsTypes = new ArrayList<GoodsType>(specification.goodsTypes.entities());
        Collections.sort(goodsTypes, ObjectWithId.INSERT_ORDER_ASC_COMPARATOR);
        
        defaults().space(20);
        pad(20);
        
        for (GoodsType goodsType : goodsTypes) {
            if (!goodsType.isStorable()) {
                continue;
            }
            System.out.println("goodsType: " + goodsType.getId() + ", " + goodsType.isStorable() + ", " + goodsType.getInsertOrder());
            
            int goodsAmount = aColony.getGoodsContainer().goodsAmount(goodsType);
            setGoodQuantity(goodsType, goodsAmount);
        }
        updateDragAndDropSource(goodsDragAndDrop);
    }
    
    private void updateDragAndDropSource(DragAndDrop goodsDragAndDrop) {
    	for (Entry<String, GoodActor> entry : goodActorByType.entrySet()) {
    		goodsDragAndDrop.addSource(new GoodActor.GoodsDragAndDropSource(entry.getValue()));
    	}
    }
    
    private void setGoodQuantity(GoodsType goodsType, int goodsAmount) {
        GoodActor warehouseGoodActor = goodActorByType.get(goodsType.getId());
        if (warehouseGoodActor == null) {
            warehouseGoodActor = new GoodActor(goodsType.getId(), goodsAmount);
            warehouseGoodActor.dragAndDropSourceContainer = this;
            goodActorByType.put(goodsType.getId(), warehouseGoodActor);
            add(warehouseGoodActor);
        }
        warehouseGoodActor.setQuantity(goodsAmount);
    }

    @Override
    public void putPayload(AbstractGoods payload, float x, float y) {
        System.out.println("warehousePanel: put good " + payload);
        
        GoodActor warehouseGoodActor = goodActorByType.get(payload.getTypeId());
        if (warehouseGoodActor == null) {
            throw new IllegalStateException("can not find warehouse good actor by goodId: " + payload.getTypeId());
        }
        
        colony.getGoodsContainer().increaseGoodsQuantity(payload);
        warehouseGoodActor.increaseQuantity(payload);
    }

    @Override
    public boolean canPutPayload(AbstractGoods unitActor, float x, float y) {
        return true;
    }

    @Override
    public void takePayload(AbstractGoods payload, float x, float y) {
        System.out.println("warehousePanel: take good " + payload);
        
        GoodActor warehouseGoodActor = goodActorByType.get(payload.getTypeId());
        if (warehouseGoodActor == null) {
            throw new IllegalStateException("can not find warehouse good actor by goodId: " + payload.getTypeId());
        }
        warehouseGoodActor.decreaseQuantity(payload);
        colony.getGoodsContainer().decreaseGoodsQuantity(payload);
    }
}
