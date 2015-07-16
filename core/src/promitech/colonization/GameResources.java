package promitech.colonization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import net.sf.freecol.common.model.ResourceType;
import net.sf.freecol.common.model.TileImprovement;
import net.sf.freecol.common.model.TileType;
import promitech.colonization.gdx.Frame;

public class GameResources {
	
	private Properties prop = new Properties();
	
	private Map<String,Texture> imagesByName = new HashMap<String, Texture>();
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
	
	private Texture loadImage(String key) {
		String imagePath = (String)prop.get(key);
		if (imagePath == null) {
			throw new IllegalArgumentException("can not find resource propertie value for key: " + key); 
		}
		imagePath = "rules/classic/" + imagePath;
		
		FileHandle imageFileHandle = Gdx.files.internal(imagePath);
		if (!imageFileHandle.exists()) {
			throw new IllegalArgumentException("can not find image for path: " + imagePath + " and resource key " + key);
		}
		Texture img = new Texture(imageFileHandle);
		imagesByName.put(key, img);
		return img;
	}

	public Texture getImage(String key) {
		Texture texture = imagesByName.get(key);
		if (texture == null) {
			texture = loadImage(key);
			//throw new IllegalArgumentException("can not find image by key: " + key);
		}
		return texture;
	}
	
	public Frame getFrame(String key) {
		Frame frame = frameByName.get(key);
		if (frame == null) {
			Texture texture = getImage(key);
			if (texture == null) {
				return null;
			}
			frame = new Frame(texture);
			frameByName.put(key, frame);
		}
		return frame;
	}
	
	public Frame getCenterAdjustFrameTexture(String key) {
		Frame frame = centerAdjustFrameTextureByName.get(key);
		if (frame == null) {
			Texture texture = getImage(key);
			int ox = 0, oy = 0;
			if (texture.getWidth() != MapRenderer.TILE_WIDTH) {
				ox = MapRenderer.TILE_WIDTH / 2 - texture.getWidth() / 2;
			}
			if (texture.getHeight() != MapRenderer.TILE_HEIGHT) {
				oy += MapRenderer.TILE_HEIGHT / 2 - texture.getHeight() / 2;
			}
			frame = new Frame(texture, ox, oy);
			centerAdjustFrameTextureByName.put(key, frame);
		}
		return frame;
	}

    public boolean isEven(int x, int y) {
        return ((y % 8 <= 2) || ((x + y) % 2 == 0 ));
    }
    
    public Texture tileEdge(int edgeStyle, int x, int y) {
    	String key = "model.tile.beach.edge" + edgeStyle + ((isEven(x, y)) ? "_even" : "_odd");
    	return getImage(key);
    }

	public Texture tileCorner(int cornerStyle, int x, int y) {
		String key = "model.tile.beach.corner" + cornerStyle + ((isEven(x, y)) ? "_even" : "_odd");
		return getImage(key);
	}

	public Frame tileResource(ResourceType resourceType) {
		String key = resourceType.getId() + ".image";
		return getCenterAdjustFrameTexture(key);
	}

	public Texture tile(TileType type, int x, int y) {
		String key = null;
		if (isEven(x, y)) {
			key = type.getTypeStr() + ".center0.image";
		} else {
			key = type.getTypeStr() + ".center1.image";
		}
		return getImage(key);
	}
	
	public Texture tileBorder(TileType type, Direction direction, int x, int y) {
		String key = type.getTypeStr() + ".border_" + direction + ((isEven(x, y)) ?  "_even" : "_odd") + ".image";
		return getImage(key);
	}

	public Texture unexploredTile(int x, int y) {
		String key = "model.tile.unexplored.center" + (isEven(x, y) ? "0" : "1") + ".image";
		return getImage(key);
	}

    public Texture unexploredBorder(Direction direction, int x, int y) {
    	String key = "model.tile.unexplored.border_" + direction + (isEven(x, y) ?  "_even" : "_odd") + ".image";
    	return getImage(key);
    }
	
	public Frame tileLastCityRumour() {
		return getCenterAdjustFrameTexture("lostCityRumour.image");
	}

	public Frame plowed() {
		return getCenterAdjustFrameTexture("model.improvement.plow.image");
	}
	
	public Texture riverDelta(Direction direction, TileImprovement tileImprovement) {
		String key = "model.tile.delta_" + direction + (tileImprovement.magnitude == 1 ? "_small" : "_large");
		return getImage(key);
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
			key = type.getTypeStr() + ".forest" + riverTileImprovement.style;
		} else {
			key = type.getTypeStr() + ".forest";
		}
		return getFrame(key);
	}

	public Color getColor(String propKey) {
		String colorName = (String)prop.get(propKey);
		if (colorName == null) {
			throw new IllegalArgumentException("can not find prop value for key: " + propKey);
		}
		colorName = colorName.replaceAll("urn:color:", "");
		colorName = colorName.toLowerCase();
		if (colorName.startsWith("0x")) {
			colorName = colorName.replaceAll("0x", "");
			return Color.valueOf(colorName);
		}
		throw new IllegalArgumentException("not implemented getColor by key: " + propKey + ", val: " + colorName);
	}
}
