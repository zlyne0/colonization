package promitech.colonization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import promitech.colonization.gdx.Frame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

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
			throw new IllegalArgumentException("can not find resource value for key: " + key); 
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
	
}
