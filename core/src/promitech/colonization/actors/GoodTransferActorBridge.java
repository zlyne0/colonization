package promitech.colonization.actors;

import net.sf.freecol.common.model.specification.AbstractGoods;
import promitech.colonization.actors.colony.WarehousePanel;
import promitech.colonization.actors.europe.MarketPanel;
import promitech.colonization.ui.SimpleMessageDialog;

public class GoodTransferActorBridge {

	private UnitsPanel unitsPanel;
	private MarketPanel marketPanel;
	private WarehousePanel warehousePanel;
	
	public void transferFromMarket(String goodsTypeId) {
		UnitActor selectedCarrierUnit = unitsPanel.getSelectedCarrierUnit();
		if (selectedCarrierUnit == null) {
			System.out.println("no selected carrier unit");
			
			new SimpleMessageDialog()
				.withContent("transfer.goods.no.unit.selected")
				.withButton("ok")
				.show(marketPanel.getStage());
			return;
		}
		
		int maxGoods = selectedCarrierUnit.unit.maxGoodsAmountToFillFreeSlots(goodsTypeId);
		if (maxGoods <= 0) {
			System.out.println("no free space on carrier unit");

			new SimpleMessageDialog()
				.withContent("transfer.goods.no.free.space")
				.withButton("ok")
				.show(marketPanel.getStage());
			return;
		}
		
		AbstractGoods abstractGoods = new AbstractGoods(goodsTypeId, maxGoods);
		GoodTransferQuantityWindow w = new GoodTransferQuantityWindow(abstractGoods, marketPanel, selectedCarrierUnit);
		w.show(marketPanel.getStage());
	}

	public void transferFromWarehouse(String goodsTypeId, int warehouseAmount) {
		UnitActor selectedCarrierUnit = unitsPanel.getSelectedCarrierUnit();
		if (selectedCarrierUnit == null) {
			System.out.println("no selected carrier unit");
			
			new SimpleMessageDialog()
				.withContent("transfer.goods.no.unit.selected")
				.withButton("ok")
				.show(warehousePanel.getStage());
			return;
		}
		
		int maxGoodsCapacity = selectedCarrierUnit.unit.maxGoodsAmountToFillFreeSlots(goodsTypeId);
		if (maxGoodsCapacity <= 0) {
			System.out.println("no free space on carrier unit");
			
			new SimpleMessageDialog()
				.withContent("transfer.goods.no.free.space")
				.withButton("ok")
				.show(warehousePanel.getStage());
			return;
		}
		int maxGoodsToTransfer = Math.min(warehouseAmount, maxGoodsCapacity);
		
		AbstractGoods abstractGoods = new AbstractGoods(goodsTypeId, maxGoodsToTransfer);
		GoodTransferQuantityWindow w = new GoodTransferQuantityWindow(abstractGoods, warehousePanel, selectedCarrierUnit);
		w.show(warehousePanel.getStage());
	}
	
	public void set(MarketPanel marketPanel) {
		this.marketPanel = marketPanel;
	}
	
	public void set(UnitsPanel unitsPanel) {
		this.unitsPanel = unitsPanel;
	}

	public void set(WarehousePanel warehousePanel) {
		this.warehousePanel = warehousePanel;
	}
}
