package promitech.colonization.screen.map;

import java.util.Collections;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import promitech.colonization.gdx.Frame;

class TileDrawModel {
	private final LinkedList<Frame> backgroundTerainTextures = new LinkedList<Frame>();
	private final LinkedList<Frame> foregroundTerainTextures = new LinkedList<Frame>();
	private final LinkedList<Frame> unexploredBorders = new LinkedList<Frame>();
	Frame settlementImage;
	IndianSettlementChip settlementChip;
	private boolean explored = false;
	private boolean fogOfWar = true;
	
	public boolean isExplored() {
		return explored;
	}
	
	public boolean isNotExplored() {
		return !explored;
	}
	
	public void setExplored(boolean explored) {
		this.explored = explored;
	}
	
	public void setFogOfWar(boolean fogOfWar) {
		this.fogOfWar = fogOfWar;
	}
	
	public void setTileVisibility(boolean explored, boolean fogOfWar) {
		this.explored = explored;
		this.fogOfWar = fogOfWar;
	}

	public boolean hasFogOfWar() {
		return fogOfWar;
	}
	
	public void draw(Batch batch, float rx, float ry) {
		for (Frame frame : backgroundTerainTextures) {
			batch.draw(frame.texture, rx, ry);
		}
		for (Frame frame : unexploredBorders) {
			batch.draw(frame.texture, rx, ry);
		}
	}
	
	public void drawOverlay(Batch batch, float rx, float ry) {
		for (Frame frame : foregroundTerainTextures) {
			batch.draw(frame.texture, rx + frame.offsetX, ry + frame.offsetY);
		}
	}

	public void drawSettlementImage(Batch batch, ShapeRenderer shapeRenderer, float rx, float ry) {
		if (settlementImage != null) {
			batch.draw(settlementImage.texture, rx + settlementImage.offsetX, ry + settlementImage.offsetY);
			if (settlementChip != null) {
				settlementChip.draw(batch, shapeRenderer, rx, ry);
			}
		}
	}
	
	public void addBackgroundTerainTexture(Frame texture) {
		backgroundTerainTextures.add(texture);
		Collections.sort(backgroundTerainTextures);
	}

	public void addForegroundTerainTexture(Frame frame) {
		if (frame == null) {
			throw new IllegalArgumentException("frame should not be null");
		}
		foregroundTerainTextures.add(frame);
	}

	public void addUnexploredBorders(Frame frame) {
		unexploredBorders.add(frame);
	}
	
	public void clear() {
		backgroundTerainTextures.clear();
		foregroundTerainTextures.clear();
		settlementImage = null;
		settlementChip = null;
	}

	public void resetUnexploredBorders() {
		unexploredBorders.clear();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("drawModel backgroundTerains: " + backgroundTerainTextures.size() + "\n");
		for (Frame f : backgroundTerainTextures){
			sb.append(f.toString()).append("\n");
		}
		return sb.toString() + ", foregroundTerains: " + foregroundTerainTextures.size();
	}
}