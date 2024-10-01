package net.sf.freecol.common.model.map.generator;

import com.github.czyzby.noise4j.map.Grid;
import com.github.czyzby.noise4j.map.generator.cellular.CellularAutomataGenerator;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileImprovementType;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.TileTypeTransformation;
import net.sf.freecol.common.model.Turn;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.map.Region;
import net.sf.freecol.common.model.map.Region.RegionType;
import net.sf.freecol.common.model.map.generator.WaveDistanceCreator.Consumer;
import net.sf.freecol.common.model.map.generator.WaveDistanceCreator.Initiator;
import net.sf.freecol.common.model.player.MoveExploredTiles;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.EuropeanNationType;
import net.sf.freecol.common.model.specification.EuropeanStartingAbstractUnit;
import net.sf.freecol.common.model.specification.GameOptions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.SpiralIterator;

public class MapGenerator {
	public static final float WATER_CELL = 0f;
	public static final float LAND_CELL = 1f;
	public static final String TEMPORARY_LAND_TYPE = "model.tile.grassland";
	private final int MAX_TILE_TYPE_SMOOTING = 2;
	
	private final TileImprovementType riverImprovementType;
	
	private SpiralIterator mapSpiralIterator;
	private List<Tile> randomableLandTiles;
	
    private int minimumLatitude;
    private int maximumLatitude;
    private float latitudePerRow;

	private int poleTemperature = -20;
	private int equatorTemperature= 40;
    
	protected int landTilesCount = 0;
	
	public MapGenerator() {
		riverImprovementType = Specification.instance.tileImprovementTypes.getById(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
	}
	
	public Map generate(MapIdEntities<Player> players) {
		// TODO: set players on new game 
		// TODO: w and h should be as parameters
		// TODO: clear units and settlements
		int w = 40;
		int h = 100;
		
		mapSpiralIterator = new SpiralIterator(w, h);
		Map map = createLandMass(w, h);
		
		createStandardRegions(map);
		generateRandomTileTypes(map);
		
		initRandomableLandTiles(map);
		
		generateMountains(map);
		createHighSea(map);
		generateRivers(map);
		new BonusResourcesGenerator(map).generate();
		new IndianSettlementsGenerator(this, players, map).makeNativeSettlements();
		
		map.initPlayersMap(players, new Turn(0));
		generatePlayersStartPositions(map, players);
		generateLostCityRumours(map);
		
		map.updateReferences();
		return map;
	}

	private void generateLostCityRumours(Map map) {
		final int rumourNumber = Specification.options.getIntValue(MapGeneratorOptions.RUMOUR_NUMBER);
		int number = landTilesCount / rumourNumber;
		
		System.out.println("RUMOUR_NUMBER " + rumourNumber + ", number " + number);
		
		for (int i = 0; i < number; i++) {
			for (int tries = 0; tries < 100; tries++) {
				Tile tile = getRandomLandTile(map);
				if (tile == null || map.isPolar(tile)) {
					continue;
				}
				if (tile.getUnits().size() > 0 || tile.hasSettlement() || tile.hasLostCityRumour()) {
					continue;
				}
				if (hasLostCityRumoursInRange(map, tile.x, tile.y, 2)) {
					continue;
				}
				tile.addLostCityRumors();
				break;
			}
		}
	}

	private boolean hasLostCityRumoursInRange(Map map, int x, int y, int radius) {
		mapSpiralIterator.reset(x, y, true, radius);
		while (mapSpiralIterator.hasNext()) {
			Tile tile = map.getTile(mapSpiralIterator.getX(), mapSpiralIterator.getY());
			if (tile != null) {
				if (tile.hasLostCityRumour()) {
					return true;
				}
			}
			mapSpiralIterator.next();
		}
		return false;
	}
	
	private void generatePlayersStartPositions(Map map, MapIdEntities<Player> gamePlayers) {
		List<Player> players = new ArrayList<Player>();
		for (Player p : gamePlayers) {
			if (p.isLiveEuropeanPlayer()) {
				players.add(p);
			}
		}
		Randomizer.instance().shuffle(players);

		// generate entry location
		int x = -1, i = 0;
		int startY = Map.POLAR_HEIGHT;
		int playerDistance = (map.height - 2*Map.POLAR_HEIGHT) / (players.size()+1);
		for (Player p : players) {
			startY += playerDistance;
			x = -1; 
			i=0;
			while (x == -1 && i<10) {
				x = findAllowedEntryLocationX(map, startY+i);
				if (x != -1) {
					p.setEntryLocation(x, startY);
				}
				i++;
			}
			if (x == -1) {
				throw new IllegalStateException("ERROR: can not find entry location for player " + p + " for startY " + startY);
			}
		}
		
        List<Unit> carriers = new ArrayList<Unit>(2);
        List<Unit> passengers = new ArrayList<Unit>(2);
		
		boolean expertsUnits = Specification.options.getBoolean(GameOptions.EXPERT_STARTING_UNITS);
		for (Player player : players) {
			carriers.clear();
			passengers.clear();
			
			EuropeanNationType pe = ((EuropeanNationType)player.nationType());
			MapIdEntities<EuropeanStartingAbstractUnit> startedUnits = pe.getStartedUnits(expertsUnits);
			
			for (EuropeanStartingAbstractUnit eu : startedUnits.entities()) {
				Unit unit = new Unit(Game.idGenerator.nextId(Unit.class), eu.getType(), eu.getRole(), player);
				player.units.add(unit);

				if (unit.isCarrier()) {
					unit.setState(Unit.UnitState.ACTIVE);
					carriers.add(unit);
				} else {
					unit.setState(Unit.UnitState.SENTRY);
					passengers.add(unit);
				}
			}
			if (carriers.size() > 0) {
				Unit carrier = carriers.get(0);
				Tile tile = map.getSafeTile(player.getEntryLocation());
				carrier.changeUnitLocation(tile);
				player.revealMapAfterUnitMove(map, carrier, new MoveExploredTiles(), new Turn(1));
				
				for (Unit passengerUnit : passengers) {
					passengerUnit.changeUnitLocation(carrier);
				}
			}
		}
	}
	
	private int findAllowedEntryLocationX(Map map, int y) {
		Tile tile;
		for (int x=map.width-2; x>map.width/2; x--) {
			tile = map.getTile(x, y);
			if (!tile.getType().isDirectlyHighSeasConnected()) {
				return x+1;
			}
		}
		return -1;
	}

	private void createStandardRegions(Map map) {
		int x, y;
		for (y=0; y<Map.STANDARD_REGION_NAMES.length; y++) {
			for (x=0; x<Map.STANDARD_REGION_NAMES[0].length; x++) {
				Region r = new Region(Map.STANDARD_REGION_NAMES[y][x], RegionType.LAND);
				r.setPrediscovered(true);
				map.regions.add(r);
			}
		}
		Region region = new Region(Region.ARCTIC, RegionType.LAND);
		region.setPrediscovered(true);
		map.regions.add(region);
		region = new Region(Region.ANTARCTIC, RegionType.LAND);
		region.setPrediscovered(true);
		map.regions.add(region);
	}

	private void generateRivers(final Map map) {
		int riverNumber = landTilesCount / Specification.options.getIntValue(MapGeneratorOptions.RIVER_NUMBER);
		int generatedRiverAmount = 0;
		
    	for (int i=0; i<riverNumber; i++) {
    		for (int tryCount=0; tryCount<20; tryCount++) {
    			Tile landTile = getRandomLandTile(map);
    			if (!canBeRiverSource(map, landTile, riverImprovementType)) {
    				continue;
    			}
    			RiverFlowGenerator river = new RiverFlowGenerator(map, riverImprovementType);
    			if (river.startFlow(landTile)) {
    				generatedRiverAmount++;
    				drawRiver(river, riverImprovementType);
    				break;
    			}
    		}
    	}
    	System.out.println("generated river " + generatedRiverAmount + " from max " + riverNumber);
    	updateRiverStyles(map);
	}
	
	private void updateRiverStyles(Map map) {
		TileImprovement riverImprovement;
		
		Tile tile, neighbourTile;
		int x, y;
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getTile(x, y);
				riverImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
				
				if (riverImprovement != null) {
					for (Direction d : Direction.longSides) {
						neighbourTile = map.getTile(tile, d);
						if (neighbourTile == null) {
							continue;
						}
						if (neighbourTile.hasImprovementType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID) || neighbourTile.getType().isWater()) {
							riverImprovement.addConnection(d);
						}
					}
					riverImprovement.updateStyle();
					if (riverImprovement.getStyle().equals("0000")) {
						System.out.println("WARN: Incorrect river style " + tile + ". Removed.");
						tile.removeTileImprovement(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
					}
				}
			}
		}
	}

	private void drawRiver(RiverFlowGenerator river, TileImprovementType riverImprovement) {
		for (Tile tile : river.riverTiles) {
			if (!tile.hasImprovementType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID)) {
	    		TileImprovement tileImprovement = new TileImprovement(Game.idGenerator, riverImprovement);
	    		tile.addImprovement(tileImprovement);
			}
		}
	}

	private boolean canBeRiverSource(Map map, Tile tile, TileImprovementType riverImprovementType) {
		if (tile == null) {
			return false;
		}
		if (foundNeighbours(map, tile.x, tile.y, TileType.OCEAN, 2)) {
			return false;
		}
		if (tile.hasImprovementType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID)) {
			return false;
		}
		if (hasNeighbourRiverImprovementInRange(map, tile.x, tile.y, 2)) {
			return false;
		}
		if (!tile.getType().isTileImprovementAllowed(riverImprovementType)) {
			return false;
		}
		return true;
	}
	
	private void createHighSea(final Map map) {
    	final float highSeaDistance = Specification.options.getIntValue(MapGeneratorOptions.DISTANCE_TO_HIGH_SEA);
    	WaveDistanceCreator wdc = new WaveDistanceCreator(map.width, map.height, highSeaDistance);
    	wdc.init(new Initiator() {
			@Override
			public float val(int x, int y) {
				if (map.getTile(x, y).getType().isWater()) {
					return WaveDistanceCreator.DEST;
				} else {
					return WaveDistanceCreator.SRC;
				}
			}
    	});
    	wdc.generate();
    	
    	final float maxRangeOfHighSeas = 0.25f;
    	final float minw = map.width * maxRangeOfHighSeas;
    	final float maxw = map.width * (1 - maxRangeOfHighSeas);
    	final TileType highSeas = Specification.instance.tileTypes.getById(TileType.HIGH_SEAS);
    	
    	wdc.consume(new Consumer() {
			@Override
			public void val(int x, int y, float v) {
				if (v != WaveDistanceCreator.SRC && v > highSeaDistance && (x < minw || x > maxw)) {
					map.getTile(x, y).changeTileType(highSeas);
				}
			}
		});
    	
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
        System.out.println("max number of random hills : " + number);
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
        	if (foundNeighbours(map, landTile.x, landTile.y, TileType.OCEAN, 1)) {
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
		
		List<TileType> candidateTileTypes = new ArrayList<TileType>(Specification.instance.tileTypes.size());
		
		
		int x, y, latitude;
		Tile tile;
		for (y=0; y<map.height; y++) {
			latitude = getLatitude(y);
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
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
					if (tileType.isWater() || tileType.isElevation() || tileType.isForested()) {
						continue;
					}
					if (tileType.getGenerationValues().match(localeTemperature, localeHumidity)) {
						candidateTileTypes.add(tileType);
					}
				}
				TileType randomTileType = Randomizer.instance().randomMember(candidateTileTypes);
				tile.changeTileType(randomTileType);
			}
		}
		
		SmoothingTileTypes smoothingTileTypes = new SmoothingTileTypes(map, Specification.instance.tileTypes);
		smoothingTileTypes.smoothing(MAX_TILE_TYPE_SMOOTING);
		
		addForest(map);
	}

	private void addForest(Map map) {
		int x;
		int y;
		Tile tile;
		List<Tile> allLandTiles = new ArrayList<Tile>(map.width * map.height);
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
				if (tile.getType().isLand() && !tile.getType().equalsId(TileType.ARCTIC)) {
					allLandTiles.add(tile);
				}
			}
		}

		java.util.Map<String, TileType> forestTypeByNonForestType = new HashMap<String, TileType>();
		TileImprovementType clearForestImpr = Specification.instance.tileImprovementTypes.getById(TileImprovementType.CLEAR_FOREST_IMPROVEMENT_TYPE_ID);
		for (TileTypeTransformation transformation : clearForestImpr.getTileTypeTransformation().entities()) {
			forestTypeByNonForestType.put(
				transformation.getToType().getId(), 
				Specification.instance.tileTypes.getById(transformation.getId())
			);
		}
		
		final int forestChance = Specification.options.getIntValue(MapGeneratorOptions.FOREST_NUMBER);
		int forestTilesCount = (int)(allLandTiles.size() * ((float)forestChance / 100f));
		if (forestTilesCount > allLandTiles.size()) {
			forestTilesCount = allLandTiles.size();
		}
		System.out.println("forest chance: " + forestChance + 
				", land tiles: " + allLandTiles.size() + 
				", forest tiles: " + forestTilesCount
		);
		
		Randomizer.instance().shuffle(allLandTiles);
		for (int i=0; i<forestTilesCount; i++) {
			tile = allLandTiles.get(i);
			TileType forestTileType = forestTypeByNonForestType.get(tile.getType().getId());
			if (forestTileType != null) {
				tile.changeTileType(forestTileType);
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
	
	private void postLandGeneration(Map map) {
		// all oceans in middle on land change to lake
		int x, y;
		Tile tile;
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
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

		// create beach
		for (y=0; y<map.height; y++) {
			for (x=0; x<map.width; x++) {
				tile = map.getSafeTile(x, y);
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

    private void initRandomableLandTiles(Map map) {
        final int SLOSH = 5;
        if (map.width <= SLOSH*2 || map.height <= SLOSH*2) {
        	throw new IllegalStateException("map is too small");
        }

        randomableLandTiles = new ArrayList<Tile>((map.height-SLOSH) * (map.width-SLOSH));
    	
    	Tile tile;
		int x, y;
		for (y=SLOSH; y<map.height-SLOSH; y++) {
			for (x=SLOSH; x<map.width-SLOSH; x++) {
				tile = map.getSafeTile(x, y);
	        	if (tile != null && tile.getType().isLand()) {
	        		randomableLandTiles.add(tile);
	        	}
			}
		}
    }
    
    Tile getRandomLandTile(Map map) {
    	return randomableLandTiles.get(Randomizer.instance().randomInt(randomableLandTiles.size()));
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
    
	private boolean hasNeighbourRiverImprovementInRange(Map map, int x, int y, int radius) {
		mapSpiralIterator.reset(x, y, true, radius);
		while (mapSpiralIterator.hasNext()) {
			Tile tile = map.getTile(mapSpiralIterator.getX(), mapSpiralIterator.getY());
			if (tile != null) {
				TileImprovement riverTileImprovement = tile.getTileImprovementByType(TileImprovementType.RIVER_IMPROVEMENT_TYPE_ID);
				if (riverTileImprovement != null) {
					return true;
				}
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
		System.out.println();
	}
	
	private Map createLandMass(int w, int h) {
		Grid grid = createBinaryLandMass(w, h);
		
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
		return map;
	}

	private Grid createBinaryLandMass(int w, int h) {
		// very big islands
		float aliveChance = 0.8f;
	    int radius = 3;
	    int birthLimit = 11;
	    int deathLimit = 11;
	    int iterationAmount = 3;
		
	    int landType = Specification.options.getStringValueAsInt(MapGeneratorOptions.LAND_GENERATOR_TYPE);
	    landType = MapGeneratorOptions.LAND_GENERATOR_ARCHIPELAGO;
		switch (landType) {
			case MapGeneratorOptions.LAND_GENERATOR_CONTINENT: {
			    aliveChance = 0.6f;
			    radius = 2;
			    birthLimit = 10;
			    deathLimit = 10;
			    iterationAmount = 5;
			} break;
			case MapGeneratorOptions.LAND_GENERATOR_ARCHIPELAGO: {
				aliveChance = 0.7f;
				radius = 1;
				birthLimit = 3;
				deathLimit = 3;
				iterationAmount = 4;
			} break;
			case MapGeneratorOptions.LAND_GENERATOR_ISLANDS: {
				aliveChance = 0.6f;
				radius = 2;
				birthLimit = 11;
				deathLimit = 8;
				iterationAmount = 3;	    
			} break;
		}
	    
		CellularAutomataGenerator cellularGenerator = new IsometricCellularAutomataGenerator();
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
