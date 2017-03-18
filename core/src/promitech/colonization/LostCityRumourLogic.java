package promitech.colonization;

import java.util.List;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.UnitRole;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.LostCityRumour;
import net.sf.freecol.common.model.map.LostCityRumour.RumourType;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.gamelogic.MoveContext;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.hud.HudStage;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class LostCityRumourLogic {

	private final HudStage mapHudStage;
	private final Game game;
	private final GUIGameController guiGameController;

	public LostCityRumourLogic(GUIGameController guiGameController, HudStage mapHudStage, Game game) {
		this.mapHudStage = mapHudStage;
		this.game = game;
		this.guiGameController = guiGameController;
	}
	
	void handleLostCityRumourType(final MoveContext mc, final RumourType type) {
		final Unit unit = mc.unit;
		final Tile destTile = mc.destTile;
		
		boolean mounds = Randomizer.instance().isHappen(50);
        int difficulty = Specification.options.getIntValue(GameOptions.RUMOUR_DIFFICULTY);
        int dx = 10 - difficulty;
		
		switch (type) {
		case BURIAL_GROUND:
			destTile.getOwner().modifyTension(unit.getOwner(), Tension.Level.HATEFUL.getLimit());
			unit.getOwner().changeStance(destTile.getOwner(), Stance.WAR);
			
			if (unit.getOwner().isHuman()) {
				StringTemplate st = StringTemplate.template("lostCityRumour.burialGround")
					.addStringTemplate("%nation%", destTile.getOwner().getNationName());
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent(st)
					.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
			break;
		case EXPEDITION_VANISHES:
			unit.getOwner().removeUnit(unit);
			mc.setUnitKilled();
			
			if (unit.getOwner().isHuman()) {
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.expeditionVanishes")
					.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
			break;
		case LEARN:
			String newUnitTypeId = Randomizer.instance().randomMember(unit.unitType.getUpgradesUnitTypeIds(ChangeType.LOST_CITY));
			UnitType newUnitType = Specification.instance.unitTypes.getById(newUnitTypeId);
			unit.changeUnitType(newUnitType);

			if (unit.getOwner().isHuman()) {
				String oldName = Messages.message(UnitLabel.getPlainUnitLabel(unit));
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template("lostCityRumour.learn")
						.add("%unit%", oldName)
						.addName("%type%", newUnitType)
					).withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
			break;
		
		case TRIBAL_CHIEF: {
	        int gold = Randomizer.instance().randomInt(dx*10) + dx * 5;
			unit.getOwner().addGold(gold);
			
			if (unit.getOwner().isHuman()) {
				String msgKey = (mounds) ? "lostCityRumour.moundsTrinkets" : "lostCityRumour.tribalChief";
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template(msgKey)
							.addAmount("%money%", gold)
					).withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
		} break;
		
		case COLONIST: {
			List<UnitType> unitTypes = Specification.instance.getUnitTypesWithAbility(Ability.FOUND_IN_LOST_CITY);
			UnitType unitType = Randomizer.instance().randomMember(unitTypes);
			Unit newColonistUnit = new Unit(
                Game.idGenerator.nextId(Unit.class), 
                unitType,
                Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
                unit.getOwner()
            );
            newColonistUnit.changeUnitLocation(destTile);

            if (unit.getOwner().isHuman()) {
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.colonist")
					.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
            }
		} break;
		
		case CIBOLA: {
			String cityOfCibolaName = game.removeNextCityOfCibola();
			int treasureAmount = Randomizer.instance().randomInt(dx * 600) + dx * 300;
			List<UnitType> treasureUnitTypes = Specification.instance.getUnitTypesWithAbility(Ability.CARRY_TREASURE);
			UnitType treasureUnitType = Randomizer.instance().randomMember(treasureUnitTypes);
			
			Unit newTreasureUnit = new Unit(
                Game.idGenerator.nextId(Unit.class), 
                treasureUnitType,
                Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
                unit.getOwner()
            );
            newTreasureUnit.changeUnitLocation(destTile);
            newTreasureUnit.setTreasureAmount(treasureAmount);
			
            if (unit.getOwner().isHuman()) {
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template("lostCityRumour.cibola")
						.addKey("%city%", cityOfCibolaName)
						.addAmount("%money%", treasureAmount)
					).withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
            }
		} break;
		
		case RUINS: {
			int treasureAmount = Randomizer.instance().randomInt(dx * 2) * 300 + 50;
			if (treasureAmount < 500) {
				unit.getOwner().addGold(treasureAmount);
			} else {
				List<UnitType> treasureUnitTypes = Specification.instance.getUnitTypesWithAbility(Ability.CARRY_TREASURE);
				UnitType treasureUnitType = Randomizer.instance().randomMember(treasureUnitTypes);
				
				Unit newTreasureUnit = new Unit(
	                Game.idGenerator.nextId(Unit.class), 
	                treasureUnitType,
	                Specification.instance.unitRoles.getById(UnitRole.DEFAULT_ROLE_ID),
	                unit.getOwner()
	            );
	            newTreasureUnit.changeUnitLocation(destTile);
	            newTreasureUnit.setTreasureAmount(treasureAmount);
			}

			if (unit.getOwner().isHuman()) {
				String msgKey = ((mounds) ? "lostCityRumour.moundsTreasure" : "lostCityRumour.ruins");
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template(msgKey)
						.addAmount("%money%", treasureAmount)
					).withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
		} break;
		
		case FOUNTAIN_OF_YOUTH: {
			unit.getOwner().getEurope().emigrantsFountainOfYoung(dx);
			
			if (unit.getOwner().isHuman()) {			
				mapHudStage.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.fountainOfYouth")
					.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
						@Override
						public void buttonPressed(SimpleMessageDialog dialog) {
							endOfExploration(mc, type);
							dialog.hide();
						}
					})
				);
			}
		} break;
		
		case NO_SUCH_RUMOUR:
		case NOTHING:
		default: {
			if (unit.getOwner().isHuman()) {
				if (mounds) {
					mapHudStage.showDialog(new SimpleMessageDialog()
						.withContent("lostCityRumour.moundsNothing")
						.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
							@Override
							public void buttonPressed(SimpleMessageDialog dialog) {
								endOfExploration(mc, type);
								dialog.hide();
							}
						})
					);
				} else {
					int keyMessagePrefixCount = Messages.keyMessagePrefixCount("lostCityRumour.nothing.");
					int msgIdx = Randomizer.instance().randomInt(keyMessagePrefixCount);
					
					mapHudStage.showDialog(new SimpleMessageDialog()
						.withContent("lostCityRumour.nothing." + msgIdx)
						.withButton("ok", new SimpleMessageDialog.ButtonActionListener() {
							@Override
							public void buttonPressed(SimpleMessageDialog dialog) {
								endOfExploration(mc, type);
								dialog.hide();
							}
						})
					);
				}
			}
		} break;
		}
		
		if (unit.getOwner().isAi()) {
			endOfExploration(mc, type);
		}
	}

	private void endOfExploration(MoveContext mc, RumourType type) {
		//guiGameController.log
		// logic nextunit
		
		mc.destTile.removeLostCityRumour();
		guiGameController.resetMapModelOnTile(mc.destTile);
	}

	public LostCityRumourLogic handle(MoveContext moveContext) {
		if (moveContext.unit.getOwner().isAi()) {
			handleApprovedExploration(moveContext);
			return this;
		}
		QuestionDialog.OptionAction<MoveContext> exploreLostCityRumourYesAnswer = new QuestionDialog.OptionAction<MoveContext>() {
			@Override
			public void executeAction(MoveContext mc) {
				handleApprovedExploration(mc);
			}
		}; 
		
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addQuestion(StringTemplate.label("exploreLostCityRumour.text"));
        questionDialog.addAnswer("exploreLostCityRumour.yes", exploreLostCityRumourYesAnswer, moveContext);
        questionDialog.addAnswer("exploreLostCityRumour.no", QuestionDialog.DO_NOTHING_ACTION, moveContext);
        mapHudStage.showDialog(questionDialog);
		
		return this;
	}
	
	private void handleApprovedExploration(MoveContext mc) {
		mc.handleMove();
		guiGameController.startAnimateMove(mc);
		// TODO: wywolanie moveLogic
		// gdy koniec animacji obsluga typu ruin i wtedy ewentualne usuniecie jednostki
		
		
		LostCityRumour lostCityRumour = new LostCityRumour();
		RumourType type = lostCityRumour.type(game, mc.unit, mc.destTile);
		System.out.println("EXPLORE_LOST_CITY_RUMOUR RumourType.type " + type);
		
		// TODO: usuniecie
		type = RumourType.EXPEDITION_VANISHES;
		//type = RumourType.BURIAL_GROUND;
		
		handleLostCityRumourType(mc, type);
	}
}
