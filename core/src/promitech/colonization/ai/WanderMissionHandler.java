package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.MapIdEntities;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.gamelogic.MoveContext;

public class WanderMissionHandler {

	private final Game game;
	private final AIMoveDrawer aiMoveDrawer;
	
	private final List<Direction> allowedDirections = new ArrayList<Direction>(Direction.allDirections);
	private final MoveContext moveContext = new MoveContext();
	
	public WanderMissionHandler(Game game, AIMoveDrawer aiMoveDrawer) {
		this.game = game;
		this.aiMoveDrawer = aiMoveDrawer;
	}

	public void executeMission(Player player, MapIdEntities<WanderMission> missions) {
		if (missions.isEmpty()) {
			return;
		}
		
		for (WanderMission wanderMission : missions.entities()) {
			executeMission(wanderMission);
		}
	}
	
	public void executeMission(final WanderMission mission) {
		Tile sourceTile = mission.unit.getTile();
		
		boolean canMove = false;
		do {
			canMove = false;
			
			Direction moveDirection = generateRandomDirection(mission, sourceTile);
			if (moveDirection == null) {
				mission.previewDirection = null;
				return;
			}
			
			Tile destTile = game.map.getTile(sourceTile, moveDirection);
			moveContext.init(sourceTile, destTile, mission.unit, moveDirection);
			
			if (moveContext.canHandleMove()) {
				moveContext.handleMove();
				mission.unit.getOwner().revealMapAfterUnitMove(game.map, mission.unit);
				mission.previewDirection = moveDirection;					
				
				aiMoveDrawer.startAIUnitDislocationAnimation(moveContext);
				
				if (mission.unit.hasMovesPoints()) {
					canMove = true;
					sourceTile = destTile;
				}
			} 
		} while (canMove);
	}
	
	private Direction generateRandomDirection(final WanderMission mission, final Tile sourceTile) {
		Direction comeDirection = null;
		if (mission.previewDirection != null) {
			comeDirection = mission.previewDirection.getReverseDirection();
		}
		allowedDirections.clear();
		for (Direction direction : Direction.allDirections) {
			Tile neighbourTile = game.map.getTile(sourceTile, direction);
			if (neighbourTile == null) {
				continue;
			}
			if (comeDirection != null && comeDirection == direction) {
				continue;
			}
			moveContext.init(sourceTile, neighbourTile, mission.unit, direction);
			
			if (moveContext.canHandleMove()) {
				allowedDirections.add(direction);
			}
		}
		
		if (allowedDirections.isEmpty()) {
			return null;
		}
		return Randomizer.instance().randomMember(allowedDirections);
	}
	
}
