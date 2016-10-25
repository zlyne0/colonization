package promitech.colonization;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.cellular.CellularAutomataGenerator;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;

public class MapGenerator {
    public final static int POLAR_HEIGHT = 2;
	
	public static final float WATER_CELL = 0f;
	public static final float LAND_CELL = 1f;
	public static final String TEMPORARY_LAND_TYPE = "model.tile.grassland";

	private SpiralIterator mapSpiralIterator;
	
    private int minimumLatitude;
    private int maximumLatitude;
    private float latitudePerRow;

	private int poleTemperature = -20;
	private int equatorTemperature= 40;
    
	private int landTilesCount = 0;
	
	public void generate(Game game) {
		// TODO: w and h should be as parameters
		int w = 40;
		int h = 100;
		
		mapSpiralIterator = new SpiralIterator(w, h);
		Grid grid = createLandMass(w, h);
		
		printMapToConsole(grid);
		
		Map map = new Map("map", w, h);
		
		Tile tile;
		TileType tileType;
		int x, y;
		float gidValue;
		for (y=0; y<h; y++) {
			for (x=0; x<w; x++) {
				gidValue = grid.get(x, y);
				if (gidValue == WATER_CELL) {
					tileType = Specification.instance.tileTypes.getById(TileType.OCEAN);
				} else {
					tileType = Specification.instance.tileTypes.getById(TEMPORARY_LAND_TYPE);
				}
				tile = new Tile("tile:" + x + "," + y, x, y, tileType, 0);
				
				map.createTile(x, y, tile);
			}
		}
		postLandGeneration(map);
		generateRandomTileTypes(map);
		generateMountains(map);
		game.map = map;
	}

    private void generateMountains(Map map) {
    	// 50% of user settings will be allocated for random hills
    	// here and there the rest will be allocated for large
    	// mountain ranges
        float randomHillsRatio = 0.5f;
        int number = (int)((1.0f - randomHillsRatio) * (landTilesCount / Specification.options.getIntValue(MapGeneratorOptions.MOUNTAIN_NUMBER)));
        
        int maximumLength = Math.max(map.width, map.height) / 10; 
        System.out.println("Number of mountain tiles is " + number);
        System.out.println("Maximum length of mountain ranges is " + maximumLength);
        
        TileType hills = Specification.instance.tileTypes.getById(TileType.HILLS);
        TileType mountains = Specification.instance.tileTypes.getById(TileType.MOUNTAINS);
        
        
        // Generate the mountain ranges
        int counter = 0;
        for (int it=0; it<200 && counter < number; it++) {
        	Tile landTile = getRandomLandTile(map);
        	if (landTile == null) {
        		continue;
        	}
        	if (landTile.getType().equalsId(TileType.HILLS) || landTile.getType().equalsId(TileType.MOUNTAINS)) {
        		continue;
        	}
        	// do not start a mountain range too close to another
        	if (foundNeighbours(map, landTile.x, landTile.y, TileType.MOUNTAINS, 3)) {
        		continue;
        	}
            // Do not add a mountain range too close to the
            // ocean/lake this helps with good locations for
            // building colonies on shore
        	if (foundNeighbours(map, landTile.x, landTile.y, TileType.OCEAN, 2)) {
        		continue;
        	}
        	int length = Randomizer.instance().randomInt(maximumLength/2, maximumLength);
        	
        	Direction direction = Randomizer.instance().randomMember(Direction.allDirections);
        	Tile nextTile = landTile;
        	for (int index = 0; index < length; index++) {
        		if (nextTile == null) {
        			continue;
        		}
        		nextTile = map.getTile(nextTile.x, nextTile.y, direction);
        		if (nextTile == null || nextTile.getType().isWater()) {
        			continue;
        		}
        		nextTile.changeTileType(mountains);
        		counter++;
        		
        		mapSpiralIterator.reset(nextTile.x, nextTile.y, true, 1);
        		while (mapSpiralIterator.hasNext()) {
        			Tile neighbour = map.getTile(mapSpiralIterator.getX(), mapSpiralIterator.getY());
        			if (neighbour.getType().isLand() && !neighbour.getType().equalsId(TileType.MOUNTAINS)) {
	        			int r = Randomizer.instance().randomInt(8);
	        			if (r == 0) {
	        				neighbour.changeTileType(mountains);
	        				counter++;
	        			} else if (r >= 6) {
	        				neighbour.changeTileType(hills);
	        			}
        			}
        			mapSpiralIterator.next();
        		}
        	}
        }
		
        number = (int) (landTilesCount * randomHillsRatio) / Specification.options.getIntValue(MapGeneratorOptions.MOUNTAIN_NUMBER);
        counter = 0;
        
        for (int it=0; it<1000 && counter < number; it++) {
        	Tile landTile = getRandomLandTile(map);
        	if (landTile == null) {
        		continue;
        	}
        	if (landTile.getType().equalsId(TileType.HILLS) || landTile.getType().equalsId(TileType.MOUNTAINS)) {
        		continue;
        	}
        	// do not start a mountain range too close to another
        	if (foundNeighbours(map, landTile.x, landTile.y, TileType.MOUNTAINS, 3)) {
        		continue;
        	}
        	if (foundNeighbours(map, landTile.x, landTile.y, TileType.OCEAN, 2)) {
        		continue;
        	}
            // 25% mountains, 75% hills
        	if (Randomizer.instance().isHappen(25)) {
        		landTile.changeTileType(mountains);
        	} else {
        		landTile.changeTileType(hills);
        	}
            counter++;
        }
    }

	private int limitToRange(int value, int lower, int upper) {
        return Math.max(lower, Math.min(value, upper));
    }
	
    private void initMinMaxLatitude(int mapHeight) {
    	minimumLatitude = Specification.options.getIntValue(MapGeneratorOptions.MINIMUM_LATITUDE);
    	maximumLatitude = Specification.options.getIntValue(MapGeneratorOptions.MAXIMUM_LATITUDE);
    	minimumLatitude = limitToRange(minimumLatitude, -90, 90);
    	maximumLatitude = limitToRange(maximumLatitude, -90, 90);
    	minimumLatitude = Math.min(minimumLatitude, maximumLatitude);
    	maximumLatitude = Math.max(minimumLatitude, maximumLatitude);
    	
    	latitudePerRow = 1f * (maximumLatitude - minimumLatitude) / (mapHeight - 1);
    }
    
    private int getLatitude(int row) {
        return minimumLatitude + (int) (row * latitudePerRow);
    }
    
	private void generateRandomTileTypes(Map map) {
		initMinMaxLatitude(map.height);
		initTemperaturePreference();
		
		final int mapHumidity = Specification.options.getIntValue(MapGeneratorOptions.HUMIDITY);
		final int forestChance = Specification.options.getIntValue(MapGeneratorOptions.FOREST_NUMBER);
		
		List<TileType> candidateTileTypes = new ArrayList<TileType>(Specification.instance.tileTypes.size());
		
		
		int x, y, latitude;
		Tile tile;
		for (y=0; y<map.height; y++) {
			latitude = getLatitude(y);
			
			
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				if (tile.getType().isWater()) {
					continue;
				}
				int temperatureRange = equatorTemperature - poleTemperature;
				int localeTemperature = poleTemperature + (90 - Math.abs(latitude)) * temperatureRange/90;
				int temperatureDeviation = 7; // +/- 7 degrees randomization
				localeTemperature += Randomizer.instance().randomInt(temperatureDeviation * 2) - temperatureDeviation;
				localeTemperature = limitToRange(localeTemperature, poleTemperature, equatorTemperature);
				
				int localeHumidity = mapHumidity;
		        int humidityDeviation = 20; // +/- 20% randomization
		        localeHumidity += Randomizer.instance().randomInt(humidityDeviation*2) - humidityDeviation;
		        localeHumidity = limitToRange(localeHumidity, 0, 100);
				
		        candidateTileTypes.clear();
				for (TileType tileType : Specification.instance.tileTypes.entities()) {
					if (tileType.isWater() || tileType.isElevation()) {
						continue;
					}
					if (tileType.getGenerationValues().match(localeTemperature, localeHumidity)) {
						if (tileType.isForested()) {
							if (Randomizer.instance().isHappen(forestChance)) {
								candidateTileTypes.add(tileType);
							}
						} else {
							candidateTileTypes.add(tileType);
						}
					}
				}
				TileType randomTileType = Randomizer.instance().randomMember(candidateTileTypes);
				tile.changeTileType(randomTileType);
			}
		}
	}
	
	private void initTemperaturePreference() {
		final int temperaturePreference = Specification.options.getIntValue(MapGeneratorOptions.TEMPERATURE);
		
		poleTemperature = -20;
		equatorTemperature = 40;
		
        switch (temperaturePreference) {
        case MapGeneratorOptions.TEMPERATURE_COLD:
            poleTemperature = -20;
            equatorTemperature = 25;
            break;
        case MapGeneratorOptions.TEMPERATURE_CHILLY:
            poleTemperature = -20;
            equatorTemperature = 30;
            break;
        case MapGeneratorOptions.TEMPERATURE_TEMPERATE:
            poleTemperature = -10;
            equatorTemperature = 35;
            break;
        case MapGeneratorOptions.TEMPERATURE_WARM:
            poleTemperature = -5;
            equatorTemperature = 40;
            break;
        case MapGeneratorOptions.TEMPERATURE_HOT:
            poleTemperature = 0;
            equatorTemperature = 40;
            break;
        default:
            break;
        }
	}
	
	public void postLandGeneration(Map map) {
		// all oceans in middle on land change to lake
		int x, y;
		Tile tile;
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				// remove all single islands
				if (tile.getType().isLand()) {
					landTilesCount++;
					if (!foundNeighbours(map, x, y, TEMPORARY_LAND_TYPE)) {
						tile.changeTileType(Specification.instance.tileTypes.getById(TileType.OCEAN));
						landTilesCount--;
					}
				} else {
					// remove single ocean tile in land 
					if (!foundNeighbours(map, x, y, TileType.OCEAN)) {
						tile.changeTileType(Specification.instance.tileTypes.getById(TileType.LAKE));
					}
				}
			}
		}

		// create beatch
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				if (tile.getType().isWater()) {
					encodeStyle(map, tile);
				}
			}
		}
	}
	
    public void encodeStyle(Map map, Tile tile) {
        EnumMap<Direction, Boolean> connections = new EnumMap<Direction, Boolean>(Direction.class);

        Tile t;
        // corners
        for (Direction d : Direction.corners) {
        	t = map.getTile(tile.x, tile.y, d);
            connections.put(d, t != null && t.getType().isLand());
        }
        // edges
        for (Direction d : Direction.longSides) {
        	t = map.getTile(tile.x, tile.y, d);
            if (t != null && t.getType().isLand()) {
                connections.put(d, Boolean.TRUE);
                // ignore adjacent corners
                connections.put(d.getNextDirection(), Boolean.FALSE);
                connections.put(d.getPreviousDirection(), Boolean.FALSE);
            } else {
                connections.put(d, Boolean.FALSE);
            }
        }
        int result = 0;
        int index = 0;
        for (Direction d : Direction.corners) {
            if (connections.get(d)) {
            	result += (int)Math.pow(2, index);
            }
            index++;
        }
        for (Direction d : Direction.longSides) {
            if (connections.get(d)) {
            	result += (int)Math.pow(2, index);
            }
            index++;
        }
        tile.setStyle(result);
    }

    private Tile getRandomLandTile(Map map) {
        final int SLOSH = 5;
        if (map.width <= SLOSH*2 || map.height <= SLOSH*2) {
        	throw new IllegalStateException("map is too small");
        }

        int x = Randomizer.instance().randomInt(SLOSH, map.width-SLOSH);
        int y = Randomizer.instance().randomInt(SLOSH, map.height-SLOSH);
        mapSpiralIterator.reset(x, y, true, Integer.MAX_VALUE);
        
        while (mapSpiralIterator.hasNext()) {
        	Tile tile = map.getTile(mapSpiralIterator.getX(), mapSpiralIterator.getY());
        	if (tile != null && tile.getType().isLand()) {
        		return tile;
        	}
        	mapSpiralIterator.next();
        }
        return null;
    }

    private boolean foundNeighbours(Map map, int x, int y, String tileType) {
    	return foundNeighbours(map, x, y, tileType, 1);
    }
    
	private boolean foundNeighbours(Map map, int x, int y, String tileType, int radius) {
		mapSpiralIterator.reset(x, y, true, radius);
		while (mapSpiralIterator.hasNext()) {
			Tile tile = map.getTile(mapSpiralIterator.getX(), mapSpiralIterator.getY());
			if (tile != null && tile.getType().equalsId(tileType)) {
				return true;
			}
			mapSpiralIterator.next();
		}
		return false;
	}
    
	
	public void printMapToConsole(Grid grid) {
		int x, y;
		float gidValue;
		for (y=0; y<grid.getHeight(); y++) {
			System.out.println();
			for (x=0; x<grid.getWidth(); x++) {
				gidValue = grid.get(x, y);
				if (gidValue > 0) {
					System.out.print("#"); 
				} else {
					System.out.print("."); 
				}
			}
		}
		
	}
	
	private Grid createLandMass(int w, int h) {
		float aliveChance = 0.8f;
	    int radius = 3;
	    int birthLimit = 11;
	    int deathLimit = 11;
	    int iterationAmount = 3;
		
		CellularAutomataGenerator cellularGenerator = new MyCellularAutomataGenerator();
        cellularGenerator.setAliveChance(aliveChance);
        cellularGenerator.setRadius(radius);
        cellularGenerator.setBirthLimit(birthLimit);
        cellularGenerator.setDeathLimit(deathLimit);
        cellularGenerator.setIterationsAmount(iterationAmount);
        
        Grid grid = new Grid(w, h);
        CellularAutomataGenerator.initiate(grid, cellularGenerator);
        for (int x=0; x<6; x++) {
            grid.fillColumn(x, 0f);
            grid.fillColumn(grid.getWidth()-x-1, 0f);
        }
        cellularGenerator.setInitiate(false);
        cellularGenerator.generate(grid);
        return grid;
	}
}