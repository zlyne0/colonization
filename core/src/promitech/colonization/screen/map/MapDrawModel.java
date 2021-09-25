package promitech.colonization.screen.map;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.screen.map.unitanimation.UnitTileAnimation;

public class MapDrawModel {
	
	public static interface ChangeSelectedUnitListener {
		void changeSelectedUnitAction(Unit newSelectedUnit);
	}
	
	private int width;
	private int height;
	
	private final List<ChangeSelectedUnitListener> changeSelectedUnitListeners = new ArrayList<ChangeSelectedUnitListener>();
	
	private TileDrawModel[][] tilesDrawModel;
	public Tile selectedTile;
	private Unit selectedUnit;
	public Player playingPlayer;
	protected Map map;
	
	protected UnitTileAnimation unitTileAnimation = UnitTileAnimation.NoAnimation;
	
	public Path unitPath;
	
	public MapDrawModel() {
	}
	
	protected void initialize(TileDrawModelInitializer initializer, Game game) {
		this.map = game.map;
		this.playingPlayer = game.playingPlayer;
		width = map.width;
		height = map.height;
		
		if (tilesDrawModel == null) {
			tilesDrawModel = new TileDrawModel[height][width];
			
			for (int i=0; i<height; i++) {
				TileDrawModel row[] = tilesDrawModel[i];
				for (int j=0; j<width; j++) {
					row[j] = new TileDrawModel();
				}
			}
		} else {
			for (int i=0; i<height; i++) {
				TileDrawModel row[] = tilesDrawModel[i];
				for (int j=0; j<width; j++) {
					row[j].clear();
				}
			}
		}
		
		initializer.initMapTiles(this, game);
	}
	
	protected TileDrawModel getTileDrawModel(int x, int y, Direction direction) {
		int stepX = direction.stepX(x, y);
		int stepY = direction.stepY(x, y);
		if (!isCoordinateValid(stepX, stepY)) {
			return null;
		}
		return getTileDrawModel(stepX, stepY);
	}
	
	protected TileDrawModel getTileDrawModel(int x, int y) {
		return tilesDrawModel[y][x];
	}

	private boolean isCoordinateValid(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}

	public void resetUnexploredBorders(TileDrawModelInitializer initializer) {
		initializer.initBordersForUnexploredTiles();
	}

	public void resetUnexploredBorders(TileDrawModelInitializer initializer, MoveExploredTiles exploredTiles) {
		initializer.initBordersForUnexploredTiles(exploredTiles);
	}
	
	public void addChangeSelectedUnitListener(ChangeSelectedUnitListener listener) {
		this.changeSelectedUnitListeners.add(listener);
	}

	public Unit getSelectedUnit() {
		return selectedUnit;
	}
	
	public void setSelectedUnit(Unit selectedUnit) {
		this.selectedUnit = selectedUnit;
		for (ChangeSelectedUnitListener l : changeSelectedUnitListeners) {
			l.changeSelectedUnitAction(selectedUnit);
		}
	}
	
	public String tileDrawModelToString(int x, int y) {
		if (!isCoordinateValid(x, y)) {
			return "tileDrawModel[" + x + "," + y + "] = null";
		}
		TileDrawModel tile = tilesDrawModel[y][x];
		return tile.toString();
	}
}
