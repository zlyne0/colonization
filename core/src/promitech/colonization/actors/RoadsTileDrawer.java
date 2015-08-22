package promitech.colonization.actors;

import java.util.HashMap;

import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import promitech.colonization.Direction;
import promitech.colonization.GameResources;
import promitech.colonization.actors.MapRenderer.TileDrawer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;

class RoadsTileDrawer extends TileDrawer {
	private int SAMPLE_POINTS = 100;
	private float SAMPLE_POINT_DISTANCE = 1f / SAMPLE_POINTS;
	
	// tile width and height
	private int w = MapRenderer.TILE_WIDTH;
	private int h = MapRenderer.TILE_HEIGHT;
	
	private java.util.Map<Direction,Vector2> edgePoints = new HashMap<Direction, Vector2>();
	private final Color roadColor;
	
	// paths from direction to direction
	private Path<Vector2>[][] paths = null;
	private Vector2 centerOfTile = new Vector2(w/2, h/2);
	Player renderForPlayer;

	private final Vector2 p1 = new Vector2();
	private final Vector2 p2 = new Vector2();
	private boolean tmpPointInited = false;
	private int borderRoadCount = 0;
	
	public RoadsTileDrawer(GameResources gameResources) {
		roadColor = gameResources.getColor("road.color");
		
		edgePoints.put(Direction.N,  new Vector2(w / 2, h));
		edgePoints.put(Direction.NE, new Vector2(w * 0.75f, h * 0.75f ));
		edgePoints.put(Direction.E,  new Vector2(w, h / 2));
		edgePoints.put(Direction.SE, new Vector2(w * 0.75f, h * 0.25f ));
		edgePoints.put(Direction.S,  new Vector2(w / 2, 0));
		edgePoints.put(Direction.SW, new Vector2(w * 0.25f, h * 0.25f ));
		edgePoints.put(Direction.W,  new Vector2(0, h / 2));
		edgePoints.put(Direction.NW, new Vector2(w * 0.25f, h * 0.75f ));
		
		initPaths();
	}
	
	private void initPaths() {
		paths = new Path[Direction.values().length][Direction.values().length];
		
		for (Direction dSrc : Direction.values()) {
			for (Direction dDest : Direction.values()) {
				paths[dSrc.ordinal()][dDest.ordinal()] = new Bezier<Vector2>(edgePoints.get(dSrc), centerOfTile, edgePoints.get(dDest));
			}
		}
	}
	
	private void draw(Direction src, Direction dest) {
		tmpPointInited = false;
		if (src == dest) {
			throw new IllegalArgumentException("can not draw road from the same source and destination direction (" + src + ")");
		} 
		Path<Vector2> road = paths[src.ordinal()][dest.ordinal()];
		
		float val = 0f;
		while (val <= 1f) {
			if (!tmpPointInited) {
				road.valueAt(p1, val);
				val += SAMPLE_POINT_DISTANCE;
				road.valueAt(p2, val);
				tmpPointInited = true;
			} else {
				p1.set(p2);
				road.valueAt(p2, val);
			}
			shapeRenderer.line(
					p1.x + screenPoint.x, p1.y + screenPoint.y, 
					p2.x + screenPoint.x, p2.y + screenPoint.y
			);
			val += SAMPLE_POINT_DISTANCE;
		}
	}

	@Override
	public void draw() {
		if (renderForPlayer.isTileUnExplored(tile) || !tile.hasRoad()) {
			return;
		}
		borderRoadCount = 0;
		shapeRenderer.setColor(roadColor);
		Direction directionForOneRoad = null;
		
		for (int iSrc = 0; iSrc < Direction.values().length; iSrc++) {
			Direction srcDirection = Direction.values()[iSrc];
			Tile srcTile = map.getTile(mapx, mapy, srcDirection);
			if (srcTile == null || !srcTile.hasRoad()) {
				continue;
			}
			borderRoadCount++;
			directionForOneRoad = srcDirection;
			
			for (int iDest = iSrc + 1; iDest < Direction.values().length; iDest++) {
				Direction destDirection = Direction.values()[iDest];
				Tile destTile = map.getTile(mapx, mapy, destDirection);
				if (destTile == null) {
					continue;
				}
				if (!destTile.hasRoad()) {
					continue;
				}
				draw(srcDirection, destDirection);
			}
		}
		if (borderRoadCount == 0) {
			if (!tile.hasSettlement()) {
				shapeRenderer.ellipse(
						centerOfTile.x + screenPoint.x - 25, centerOfTile.y + screenPoint.y - 12, 
						50, 25
				);
			}
		} else {
			if (borderRoadCount == 1) {
				Vector2 p2 = edgePoints.get(directionForOneRoad);
				shapeRenderer.line(
						centerOfTile.x + screenPoint.x, centerOfTile.y + screenPoint.y,
						p2.x + screenPoint.x, p2.y + screenPoint.y
				);
			}
		}
	}
}
