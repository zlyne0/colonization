package promitech.colonization.actors.colony;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;

import net.sf.freecol.common.model.ColonyTile;

class UnitTerrainDragAndDropTarget extends Target {

	public UnitTerrainDragAndDropTarget(TerrainPanel actor) {
		super(actor);
	}

	@Override
	public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
		TerrainPanel terrainPanel = (TerrainPanel)getActor();
		return terrainPanel.canPutWorkerOnTerrain(x, y);
	}

	@Override
	public void drop(Source source, Payload payload, float x, float y, int pointer) {
		if (source.getActor() instanceof UnitActor) {
			UnitActor worker = (UnitActor)source.getActor();
			TerrainPanel terrainPanel = (TerrainPanel)getActor();
			
			if (source.getActor().getParent() instanceof TerrainPanel) {
				ColonyTile destColonyTile = terrainPanel.getColonyTileByScreenCords(x, y);
				terrainPanel.moveWorkerToTile(worker, destColonyTile);
				return;
			}
			if (source.getActor().getParent() instanceof BuildingActor) {
				BuildingActor sourceBuildingActor = (BuildingActor)source.getActor().getParent();
				
				ColonyTile destColonyTile = terrainPanel.getColonyTileByScreenCords(x, y);
				sourceBuildingActor.takeUnit(worker);
				terrainPanel.putWorkerOnTile(worker, destColonyTile);
				return;
			}
		}
	}

}
