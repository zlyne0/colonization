package promitech.colonization.actors.colony;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.ProductionConsumption;
import net.sf.freecol.common.model.Tile;

public class ProductionPanel extends HorizontalGroup {

	private ProductionQuantityDrawModel drawModel = new ProductionQuantityDrawModel();
	private ProductionQuantityDrawer productionQuantityDrawer;
	
	public ProductionPanel() {
		setWidth(40);
	}
	
	@Override
	public float getPrefHeight() {
		return 40;
	}
	
    public void init(Colony colony, Tile colonyTile) {
        ProductionConsumption pc = colony.productionSummary();
        
        drawModel.init(pc.realProduction);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
    	if (productionQuantityDrawer == null) {
    		productionQuantityDrawer = new ProductionQuantityDrawer(getWidth(), getHeight());
    	}
    	productionQuantityDrawer.drawHorizontaly(batch, drawModel, getX(), getY());
    }
    
}
