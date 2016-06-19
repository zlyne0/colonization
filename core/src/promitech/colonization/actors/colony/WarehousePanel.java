package promitech.colonization.actors.colony;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.actors.ChangeColonyStateListener;
import promitech.colonization.actors.QuantityGoodActor;

class WarehousePanel extends Table implements DragAndDropSourceContainer<AbstractGoods>, DragAndDropTargetContainer<AbstractGoods> {
    private java.util.Map<String, WarehouseGoodsActor> goodActorByType = new HashMap<String, WarehouseGoodsActor>();
    
    private Colony colony;
    private final ChangeColonyStateListener changeColonyStateListener;
    
    WarehousePanel(ChangeColonyStateListener changeColonyStateListener) {
        this.changeColonyStateListener = changeColonyStateListener;
    }

    int capacity() {
        return colony.getWarehouseCapacity();
    }
    
    public void initGoods(Colony aColony, DragAndDrop goodsDragAndDrop) {
        this.colony = aColony;
        
        goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(this, this));
        
        defaults().space(20);
        pad(20);
        
        updateGoodsQuantity(aColony);
        updateDragAndDropSource(goodsDragAndDrop);
    }
    
    private void updateDragAndDropSource(DragAndDrop goodsDragAndDrop) {
    	for (Entry<String, WarehouseGoodsActor> entry : goodActorByType.entrySet()) {
    		goodsDragAndDrop.addSource(new QuantityGoodActor.GoodsDragAndDropSource(entry.getValue()));
    	}
    }
    
    void updateGoodsQuantity(Colony aColony) {
    	List<GoodsType> goodsTypes = Specification.instance.goodsTypes.sortedEntities();
    	for (GoodsType goodsType : goodsTypes) {
    		if (!goodsType.isStorable()) {
    			continue;
    		}
    		System.out.println("goodsType: " + goodsType.getId() + ", " + goodsType.isStorable() + ", " + goodsType.getInsertOrder());
    		
    		int goodsAmount = aColony.getGoodsContainer().goodsAmount(goodsType);
    		setGoodQuantity(goodsType, goodsAmount);
    	}
    }
    
    private void setGoodQuantity(GoodsType goodsType, int goodsAmount) {
    	WarehouseGoodsActor warehouseGoodActor = goodActorByType.get(goodsType.getId());
        if (warehouseGoodActor == null) {
            warehouseGoodActor = new WarehouseGoodsActor(goodsType, goodsAmount);
            warehouseGoodActor.dragAndDropSourceContainer = this;
            goodActorByType.put(goodsType.getId(), warehouseGoodActor);
            add(warehouseGoodActor);
        }
        warehouseGoodActor.setQuantity(goodsAmount);
    }

    @Override
    public void putPayload(AbstractGoods payload, float x, float y) {
        System.out.println("warehousePanel: put good " + payload);
        
        QuantityGoodActor warehouseGoodActor = goodActorByType.get(payload.getTypeId());
        if (warehouseGoodActor == null) {
            throw new IllegalStateException("can not find warehouse good actor by goodId: " + payload.getTypeId());
        }
        
        colony.getGoodsContainer().increaseGoodsQuantity(payload);
        warehouseGoodActor.increaseQuantity(payload);
        changeColonyStateListener.transfereGoods();
    }

    @Override
    public boolean canPutPayload(AbstractGoods unitActor, float x, float y) {
        return true;
    }

    @Override
    public void takePayload(AbstractGoods payload, float x, float y) {
        System.out.println("warehousePanel: take good " + payload);
        
        QuantityGoodActor warehouseGoodActor = goodActorByType.get(payload.getTypeId());
        if (warehouseGoodActor == null) {
            throw new IllegalStateException("can not find warehouse good actor by goodId: " + payload.getTypeId());
        }
        warehouseGoodActor.decreaseQuantity(payload);
        colony.getGoodsContainer().decreaseGoodsQuantity(payload);
        changeColonyStateListener.transfereGoods();
    }
}
