package promitech.colonization.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Map;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.ai.missions.AbstractMission;
import net.sf.freecol.common.model.ai.missions.IndianBringGiftMission;
import net.sf.freecol.common.model.ai.missions.PlayerMissionsContainer;
import net.sf.freecol.common.model.ai.missions.WanderMission;
import net.sf.freecol.common.model.map.path.Path;
import net.sf.freecol.common.model.map.path.PathFinder;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.AbstractGoods;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.RandomChoice;
import promitech.colonization.Randomizer;
import static promitech.colonization.ai.MissionHandlerLogger.logger;

public class NativeMissionPlaner {

	private final int bringGiftsThreshold = 25;
	private final int bringGiftsMinimumAmount = 10;
	private final int bringGiftsMaximumAmount = 80;
    private final int maxDistanceToBringGifts = 5;
	
	private final List<Unit> units = new ArrayList<Unit>();
	
	private final PathFinder pathFinder;

	public NativeMissionPlaner(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
	}
	
	public void prepareIndianWanderMissions(Player player, PlayerMissionsContainer missionsContainer) {
		for (Settlement settlement : player.settlements.entities()) {
			IndianSettlement tribe = (IndianSettlement)settlement;
			
			units.clear();
			for (Unit tribeUnit : tribe.getUnits().entities()) {
				if (!missionsContainer.isUnitBlockedForMission(tribeUnit)) {
					units.add(tribeUnit);
				}
			}
			
			if (units.size() > tribe.settlementType.getMinimumSize() - 1) {
				for (int i = tribe.settlementType.getMinimumSize() - 1; i < units.size(); i++) {
					Unit unitToWander = units.get(i);
					unitToWander.changeUnitLocation(settlement.tile);
					WanderMission wanderMission = new WanderMission(unitToWander);
					missionsContainer.blockUnitsForMission(wanderMission);
					missionsContainer.addMission(wanderMission);
				}
			}
		}
	}
	
	public void prepareBringGiftsMission(Map map, Player player, PlayerMissionsContainer missionsContainer) {
        final int giftProbability = Specification.options.getIntValue(GameOptions.GIFT_PROBABILITY);
        
        for (Settlement settlement : player.settlements.entities()) {
			if (!Randomizer.instance().isHappen(giftProbability)) {
				continue;
			}
			if (isMissionBringGiftExists(settlement, missionsContainer)) {
				continue;
			}
			
			AbstractGoods chooseGoodsToGift = chooseGoodsAsGift(settlement);
			if (chooseGoodsToGift == null) {
				continue;
			}
			
			Unit unitToBringGift = chooseUnitToBringGift(settlement, missionsContainer);
			if (unitToBringGift == null) {
				continue;
			}
			Colony colony = chooseColonyToBringGift(map, settlement.asIndianSettlement(), unitToBringGift);
			if (colony == null) {
				continue;
			}
			
			if (logger.isDebug()) {
				logger.debug(
					"nativeMissionPlaner[%s] create IndianBringGiftMission from %s to %s, gift %s:%s", 
					settlement.getOwner().getId(),
					settlement.getId(),
					colony.getId(),
					chooseGoodsToGift.getTypeId(),
					chooseGoodsToGift.getQuantity()
				);
			}
			missionsContainer.addMission(
				new IndianBringGiftMission(settlement.asIndianSettlement(), colony, unitToBringGift, chooseGoodsToGift)
			);
		}
	}

	private boolean isMissionBringGiftExists(Settlement settlement, PlayerMissionsContainer missionsContainer) {
		for (AbstractMission ab : missionsContainer.getMissions().entities()) {
			if (ab.is(IndianBringGiftMission.class)) {
				if (settlement.equalsId(((IndianBringGiftMission)ab).getIndianSettlement())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Colony chooseColonyToBringGift(Map map, IndianSettlement indianSettlement, Unit transportUnit) {
		List<RandomChoice<Colony>> colonies = new ArrayList<RandomChoice<Colony>>();
		
		for (Tile tile : map.neighbourTiles(indianSettlement.tile, maxDistanceToBringGifts)) {
			if (tile.hasSettlement() && tile.getSettlement().isColony()) {
				Colony colony = tile.getSettlement().asColony();
				if (indianSettlement.getOwner().hasContacted(colony.getOwner())) {
					Path path = pathFinder.findToTile(map, indianSettlement.tile, colony.tile, transportUnit, false);
					if (path.isReachedDestination()) {
						int alarm = Math.max(1, indianSettlement.getTension(colony.getOwner()).getValue());
						int turns = path.totalTurns();
						if (turns == 0) {
							turns = 1;
						}
						colonies.add(new RandomChoice<Colony>(colony, 1000000 / alarm / turns));
					}
				}
			}
		}
		RandomChoice<Colony> randomColony = Randomizer.instance().randomOne(colonies);
		if (randomColony == null) {
			return null;
		}
		return randomColony.probabilityObject();
	}
	
	private Unit chooseUnitToBringGift(Settlement settlement, PlayerMissionsContainer missionsContainer) {
		List<Unit> unitsToExecute = new ArrayList<Unit>();
		for (Unit unit : settlement.asIndianSettlement().getUnits().entities()) {
			if (!missionsContainer.isUnitBlockedForMission(unit)) {
				unitsToExecute.add(unit);
			}
		}
		return Randomizer.instance().randomMember(unitsToExecute);
	}
	
	/**
	 * Random one goods to gift. Can return null. 
	 */
	private AbstractGoods chooseGoodsAsGift(Settlement settlement) {
		List<AbstractGoods> glist = new ArrayList<AbstractGoods>();
		for (GoodsType goodsType : Specification.instance.goodsTypes.entities()) {
			if (goodsType.isNewWorldGoodsType()) {
				int n = settlement.getGoodsContainer().goodsAmount(goodsType);
				if (n >= bringGiftsThreshold) {
					int amount = Math.min(
						Randomizer.instance().randomInt(bringGiftsMinimumAmount, n),
						bringGiftsMaximumAmount
					);
					glist.add(new AbstractGoods(goodsType.getId(), amount));
				}
			}
		}
		return Randomizer.instance().randomMember(glist);
	}
}
