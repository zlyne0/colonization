package promitech.colonization.screen.colony;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.GameResources;
import promitech.colonization.screen.ui.ChangeColonyStateListener;
import promitech.colonization.screen.ui.GoodTransferActorBridge;
import promitech.colonization.screen.ui.QuantityGoodActor;
import promitech.colonization.ui.DoubleClickedListener;
import promitech.colonization.ui.KnobResizeableScrollPane;

public class WarehousePanel extends Container<ScrollPane> implements DragAndDropSourceContainer<AbstractGoods>, DragAndDropTargetContainer<AbstractGoods> {
    private java.util.Map<String, WarehouseGoodsActor> goodActorByType = new HashMap<String, WarehouseGoodsActor>();
    
    private final GoodTransferActorBridge goodTransferActorBridge;
    private Colony colony;
    private final ChangeColonyStateListener changeColonyStateListener;
    private final Table scrollPaneContent = new Table();

    private DoubleClickedListener warehouseGoodsDoubleClickListener = new DoubleClickedListener() {
    	public void doubleClicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
    		final WarehouseGoodsActor warehouseGoodsActor = (WarehouseGoodsActor)event.getListenerActor();
    		if (!warehouseGoodsActor.isEmpty()) {
    			goodTransferActorBridge.transferFromWarehouse(warehouseGoodsActor.getGoodsType().getId(), warehouseGoodsActor.getQuantity());
    		}
    	};
    };
    
    WarehousePanel(ChangeColonyStateListener changeColonyStateListener, GoodTransferActorBridge goodTransferActorBridge) {
        this.changeColonyStateListener = changeColonyStateListener;
        this.goodTransferActorBridge = goodTransferActorBridge;
        
		ScrollPane scrollPane = new KnobResizeableScrollPane(scrollPaneContent, GameResources.instance.getUiSkin());
		scrollPane.setForceScroll(false, false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setOverscroll(true, true);
		scrollPane.setScrollBarPositions(true, true);
		scrollPane.setScrollingDisabled(false, true);
		setActor(scrollPane);
    }

    public void initGoods(Colony aColony, DragAndDrop goodsDragAndDrop) {
        this.colony = aColony;
        
        goodsDragAndDrop.addTarget(new QuantityGoodActor.GoodsDragAndDropTarget(this, this));
        
        updateGoodsQuantity();
        updateDragAndDropSource(goodsDragAndDrop);
    }
    
    private void updateDragAndDropSource(DragAndDrop goodsDragAndDrop) {
    	for (Entry<String, WarehouseGoodsActor> entry : goodActorByType.entrySet()) {
    		goodsDragAndDrop.addSource(new QuantityGoodActor.GoodsDragAndDropSource(entry.getValue()));
    	}
    }
    
    void updateGoodsQuantity() {
    	for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
    		if (!goodsType.isStorable()) {
    			continue;
    		}
    		int goodsAmount = colony.getGoodsContainer().goodsAmount(goodsType);
    		setGoodQuantity(goodsType, goodsAmount, colony.exportInfo(goodsType).isExport());
    	}
    }
    
    private void setGoodQuantity(GoodsType goodsType, int goodsAmount, boolean exported) {
    	WarehouseGoodsActor warehouseGoodActor = goodActorByType.get(goodsType.getId());
        if (warehouseGoodActor == null) {
            warehouseGoodActor = new WarehouseGoodsActor(goodsType, goodsAmount);
            warehouseGoodActor.addListener(warehouseGoodsDoubleClickListener);
            warehouseGoodActor.dragAndDropSourceContainer = this;
            
            goodActorByType.put(goodsType.getId(), warehouseGoodActor);
            scrollPaneContent.add(warehouseGoodActor)
				.width(warehouseGoodActor.getPrefWidth() + 20 + 10)
				.height(warehouseGoodActor.getPrefHeight() + 20);
            
        }
        warehouseGoodActor.setQuantity(goodsAmount);
        warehouseGoodActor.setExported(exported);
        warehouseGoodActor.setWarehouseCapacity(colony.warehouseCapacity());
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
    
	@Override
	public void onDragPayload(float x, float y) {
	}

	@Override
	public void onLeaveDragPayload() {
	}
    
}
