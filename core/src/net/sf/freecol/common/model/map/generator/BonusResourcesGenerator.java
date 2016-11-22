package net.sf.freecol.common.model.map.generator;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileResource;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;

public class BonusResourcesGenerator {

    private final Map map;
    private final int bonusPercentProbability;
    private final TileImprovementType fishBonusLandType;
    private final TileImprovementType fishBonusRiverType;
    
    private int neighboursLand = 0;
    private boolean neighbourRivers = false;
    private boolean added = false;
    private Tile tile, neighbourTile;
    
    public BonusResourcesGenerator(Map map) {
        this.map = map;
        
        bonusPercentProbability = Specification.options.getIntValue(MapGeneratorOptions.BONUS_NUMBER);
        fishBonusLandType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.FISH_BONUS_LAND);
        fishBonusRiverType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.FISH_BONUS_RIVER);
    }
    
    
    public void generate() {
        int x, y;
        for (y=0; y<map.height; y++) {
            for (x=0; x<map.width; x++) {
                tile = map.getTile(x, y);
                if (tile == null) {
                    continue;
                }
                if (tile.getType().isLand()) {
                    generateLandResource();
                } else {
                    generateSeaResource();
                }
            }
        }
    }

    private void generateSeaResource() {
        neighboursLand = 0;
        neighbourRivers = false;
        added = false;
        
        for (Direction direction : Direction.allDirections) {
            neighbourTile = map.getTile(tile, direction);
            if (neighbourTile != null && neighbourTile.getType().isLand()) {
                neighboursLand++;
                if (neighbourTile.hasImprovementType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID)) {
                    neighbourRivers = true;
                }
            }
        }
        // In Col1, ocean tiles with less than 3 land neighbours
        // produce 2 fish, all others produce 4 fish
        if (neighboursLand > 2) {
            added = true;
            tile.addImprovement(new TileImprovement(Game.idGenerator, fishBonusLandType));
        }
        
        // In Col1, the ocean tile in front of a river mouth would
        // get an additional +1 bonus
        if (neighbourRivers) {
            added = true;
            tile.addImprovement(new TileImprovement(Game.idGenerator, fishBonusRiverType));
        }
        
        if (added == false && Randomizer.instance().isHappen(bonusPercentProbability) && tile.getType().allowedResourceTypes.isNotEmpty()) {
            ResourceType resourceType = tile.getType().exposeResource();
            tile.addResource(new TileResource(Game.idGenerator, resourceType, resourceType.initQuantity()));
        }
    }

    private void generateLandResource() {
        if (Randomizer.instance().isHappen(bonusPercentProbability) && tile.getType().allowedResourceTypes.isNotEmpty()) {
            ResourceType resourceType = tile.getType().exposeResource();
            tile.addResource(new TileResource(Game.idGenerator, resourceType, resourceType.initQuantity()));
        }
    }
    
    
}
