package promitech.colonization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.specification.BuildingType;
import promitech.colonization.actors.map.MapRenderer;
import promitech.colonization.gdx.Frame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class GameResources {
	
	private Properties prop = new Properties();
	
	private Map<String,TextureAtlas> atlasByName = new HashMap<String, TextureAtlas>();	
	private Map<String,Integer> countByPrefix = new HashMap<String, Integer>();
	private Map<String,Frame> frameByName = new HashMap<String,Frame>();
	private Map<String,Frame> centerAdjustFrameTextureByName = new HashMap<String,Frame>();
	
	public int getCountForPrefix(String prefix) {
		Integer count = countByPrefix.get(prefix);
		if (count != null ){
			return count.intValue();
		}
		count = 0;
		for (Entry<Object, Object> entry : prop.entrySet()) {
			if (((String)entry.getKey()).startsWith(prefix)) {
				count++;
			}
		};
		countByPrefix.put(prefix, count);
		return count;
	}
	
	public void load() throws IOException {
		FileHandle fh = Gdx.files.internal("rules/classic/resources.properties");
		
		InputStream stream = fh.read();
		prop.load(stream);
		stream.close();
	}
	
	TextureAtlas layer1;
	TextureAtlas layer2;
	
	private AtlasRegion loadImage(String key) {
		String imagePath = (String)prop.get(key);
		if (imagePath == null) {
			throw new IllegalArgumentException("can not find resource propertie value for key: " + key); 
		}
		
		//
		// model.tile.greatRiver.center0.image=:atlas:atlasPath.pack:regionName
		//
		if (imagePath.startsWith(":atlas")) {
			String atlasString[] = imagePath.split(":");
			String atlasPath = atlasString[2];
			String regionName = atlasString[3];

			TextureAtlas atlas = atlasByName.get(atlasPath);
			if (atlas == null) {
				atlas = new TextureAtlas("rules/classic/" + atlasPath);
				atlasByName.put(atlasPath, atlas);
			}
			AtlasRegion findRegion = atlas.findRegion(regionName);
			if (findRegion == null) {
				throw new IllegalArgumentException("can not find image for key: " + key);
			}
			return findRegion;
		}		
		imagePath = "rules/classic/" + imagePath;
		
		FileHandle imageFileHandle = Gdx.files.internal(imagePath);
		if (!imageFileHandle.exists()) {
			throw new IllegalArgumentException("can not find image for path: " + imagePath + " and resource key " + key);
		}
		Texture texture = new Texture(imageFileHandle);
		return new AtlasRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	public Frame getFrame(String key) {
		return getFrame(key, 0);
	}

	public Frame getFrame(String key, int zIndex) {
		String mapKey = "" + zIndex + ":" + key;
		Frame frame = frameByName.get(mapKey);
		if (frame == null) {
			AtlasRegion region = loadImage(key);
			frame = new Frame(region, zIndex);
			frame.key = key;
			frameByName.put(mapKey, frame);
		}
		return frame;
	}
	
	public Frame getCenterAdjustFrameTexture(String key) {
		String centerKey = "center: " + key;
		Frame frame = frameByName.get(centerKey);
		if (frame != null) {
			return frame;
		}
		AtlasRegion region = loadImage(key);
		int ox = 0, oy = 0;
		if (region.getRegionWidth() != MapRenderer.TILE_WIDTH) {
			ox = MapRenderer.TILE_WIDTH / 2 - region.getRegionWidth() / 2;
		}
		if (region.getRegionHeight() != MapRenderer.TILE_HEIGHT) {
			oy += MapRenderer.TILE_HEIGHT / 2 - region.getRegionHeight() / 2;
		}
		frame = new Frame(region, ox, oy);
		centerAdjustFrameTextureByName.put(centerKey, frame);
		return frame;
	}

    public boolean isEven(int x, int y) {
        return ((y % 8 <= 2) || ((x + y) % 2 == 0 ));
    }
    
    public Frame tileEdge(int edgeStyle, int x, int y, int zIndex) {
    	String key = "model.tile.beach.edge" + edgeStyle + ((isEven(x, y)) ? "_even" : "_odd");
    	return getFrame(key, zIndex);
    }

	public Frame tileCorner(int cornerStyle, int x, int y, int zIndex) {
		String key = "model.tile.beach.corner" + cornerStyle + ((isEven(x, y)) ? "_even" : "_odd");
		return getFrame(key, zIndex);
	}

	public Frame tileResource(ResourceType resourceType) {
		String key = resourceType.getId() + ".image";
		return getCenterAdjustFrameTexture(key);
	}

	public Frame tile(TileType type, int x, int y) {
		String key = null;
		if (isEven(x, y)) {
			key = type.getId() + ".center0.image";
		} else {
			key = type.getId() + ".center1.image";
		}
		return getFrame(key, type.getInsertOrder());
	}
	
	public Frame tileBorder(TileType type, Direction direction, int x, int y, int zIndex) {
		String key = type.getId() + ".border_" + direction + ((isEven(x, y)) ?  "_even" : "_odd") + ".image";
		return getFrame(key, zIndex);
	}

	public Frame unexploredTile(int x, int y) {
		String key = "model.tile.unexplored.center" + (isEven(x, y) ? "0" : "1") + ".image";
		return getFrame(key, Frame.DEEPEST_ORDER);
	}
	
    public Frame unexploredBorder(Direction direction, int x, int y) {
    	String key = "model.tile.unexplored.border_" + direction + (isEven(x, y) ?  "_even" : "_odd") + ".image";
    	return getFrame(key, Frame.DEEPEST_ORDER);
    }
	
    public Frame buildingTypeImage(BuildingType buildingType) {
        return getFrame(buildingType.getId() + ".image");
    }
    
	public Frame tileLastCityRumour() {
		return getCenterAdjustFrameTexture("lostCityRumour.image");
	}

	public Frame plowed() {
		return getCenterAdjustFrameTexture("model.improvement.plow.image");
	}
	
	public Frame riverDelta(Direction direction, TileImprovement tileImprovement) {
		String key = "model.tile.delta_" + direction + (tileImprovement.magnitude == 1 ? "_small" : "_large");
		return getFrame(key);
	}

	public Frame hills() {
		Randomizer randomizer = Randomizer.getInstance();		
		String keyPrefix = "model.tile.hills.overlay";
		int countForPrefix = getCountForPrefix(keyPrefix);
		String key = keyPrefix + Integer.toString(randomizer.randomInt(countForPrefix)) + ".image";
		return getFrame(key);
	}

	public Frame mountainsKey() {
		Randomizer randomizer = Randomizer.getInstance();		
		String keyPrefix = "model.tile.mountains.overlay";
		int countForPrefix = getCountForPrefix(keyPrefix);
		String key = keyPrefix + Integer.toString(randomizer.randomInt(countForPrefix)) + ".image";
		return getFrame(key);
	}
	
	public Frame river(String style) {
		String key = "model.tile.river" + style;
		return getFrame(key);
	}

	public Frame forestImg(TileType type, TileImprovement riverTileImprovement) {
		String key;
		if (riverTileImprovement != null) {
			key = type.getId() + ".forest" + riverTileImprovement.style;
		} else {
			key = type.getId() + ".forest";
		}
		return getFrame(key);
	}

	public Color getColor(String propKey) {
		String colorName = (String)prop.get(propKey);
		if (colorName == null) {
			throw new IllegalArgumentException("can not find prop value for key: " + propKey);
		}
		return colorFromValue(colorName);
	}
	
	public static Color colorFromValue(String colorName) {
		colorName = colorName.replaceAll("urn:color:", "");
		colorName = colorName.toLowerCase();
		if (colorName.startsWith("0x")) {
			colorName = colorName.replaceAll("0x", "");
			return Color.valueOf(colorName);
		} else {
			return new Color((Integer.parseInt(colorName) << 8) | 0xff);
		}
	}
}
