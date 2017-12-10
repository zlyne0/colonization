package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.WanderMission;
import net.sf.freecol.common.model.player.Player;
import promitech.colonization.Direction;
import promitech.colonization.Randomizer;
import promitech.colonization.SpiralIterator;
import promitech.colonization.move.MoveContext;
import promitech.colonization.move.MoveService;
import promitech.map.Boolean2dArray;

public class WanderMissionHandler implements MissionHandler<WanderMission> {

	private final Boolean2dArray settlementWanderRange;
	private Player wanderRangeForPlayer;
	
	private final Game game;
	private final MoveService moveService;
	
	private final List<Direction> allowedDirections = new ArrayList<Direction>(Direction.allDirections);
	private final MoveContext moveContext = new MoveContext();
	
	public WanderMissionHandler(Game game, MoveService moveService) {
		this.game = game;
		this.moveService = moveService;
		this.settlementWanderRange = new Boolean2dArray(game.map.width, game.map.height);
	}

	private void prepareSettlementWanderRange(Player player) {
		if (wanderRangeForPlayer != null && wanderRangeForPlayer.equalsId(player)) {
			return;
		}
		wanderRangeForPlayer = player;
		settlementWanderRange.set(false);
		SpiralIterator spiral = new SpiralIterator(game.map.width, game.map.height);
		for (Settlement s : player.settlements.entities()) {
			spiral.reset(s.tile.x, s.tile.y, true, s.settlementType.getWanderingRadius());
			while (spiral.hasNext()) {
				settlementWanderRange.set(spiral.getX(), spiral.getY(), true);
				spiral.next();
			}
		}
	}
	
	@Override
	public void handle(PlayerMissionsContainer playerMissionsContainer, WanderMission mission) {
		prepareSettlementWanderRange(mission.unit.getOwner());
		
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
	            moveService.aiConfirmedMoveProcessor(moveContext);
				
				mission.previewDirection = moveDirection;					
				
				if (mission.unit.hasMovesPoints()) {
					canMove = true;
					sourceTile = destTile;
				}
			} else {
				canMove = false;
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
			if (!settlementWanderRange.get(neighbourTile.x, neighbourTile.y)) {
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
