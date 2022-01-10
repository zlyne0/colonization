package promitech.colonization.orders;

import java.util.List;

import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitFactory;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.map.LostCityRumour;
import net.sf.freecol.common.model.map.LostCityRumour.RumourType;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Stance;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.Ability;
import net.sf.freecol.common.model.specification.GameOptions;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.Randomizer;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveInThreadService;
import promitech.colonization.orders.move.MoveService.AfterMoveProcessor;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.SimpleMessageDialog;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class LostCityRumourService {

	private final Game game;
	private final GUIGameController guiGameController;

	public LostCityRumourService(GUIGameController guiGameController, Game game) {
		this.game = game;
		this.guiGameController = guiGameController;
	}
	
	private void handleLostCityRumourType(final MoveContext mc, final RumourType type) {
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
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(st)
					.withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
			break;
		case EXPEDITION_VANISHES:
			unit.getOwner().removeUnit(unit);
			
			if (unit.getOwner().isHuman()) {
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.expeditionVanishes")
					.withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
			break;
		case LEARN:
			String newUnitTypeId = Randomizer.instance().randomMember(unit.unitType.getUpgradesUnitTypeIds(ChangeType.LOST_CITY));
			UnitType newUnitType = Specification.instance.unitTypes.getById(newUnitTypeId);
			unit.changeUnitType(newUnitType);

			if (unit.getOwner().isHuman()) {
				String oldName = Messages.message(UnitLabel.getPlainUnitLabel(unit));
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template("lostCityRumour.learn")
						.add("%unit%", oldName)
						.addName("%type%", newUnitType)
					).withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
			break;
		
		case TRIBAL_CHIEF: {
	        int gold = Randomizer.instance().randomInt(dx*10) + dx * 5;
			unit.getOwner().addGold(gold);
			
			if (unit.getOwner().isHuman()) {
				String msgKey = (mounds) ? "lostCityRumour.moundsTrinkets" : "lostCityRumour.tribalChief";
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template(msgKey)
							.addAmount("%money%", gold)
					).withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
		} break;
		
		case COLONIST: {
			List<UnitType> unitTypes = Specification.instance.getUnitTypesWithAbility(Ability.FOUND_IN_LOST_CITY);
			UnitType unitType = Randomizer.instance().randomMember(unitTypes);
            UnitFactory.create(unitType, unit.getOwner(), destTile);

            if (unit.getOwner().isHuman()) {
            	guiGameController.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.colonist")
					.withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
            }
		} break;
		
		case CIBOLA: {
			String cityOfCibolaName = game.removeNextCityOfCibola();
			int treasureAmount = Randomizer.instance().randomInt(dx * 600) + dx * 300;
			
			UnitFactory.createTreasureTrain(unit.getOwner(), destTile, treasureAmount);
			
            if (unit.getOwner().isHuman()) {
            	guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template("lostCityRumour.cibola")
						.addKey("%city%", cityOfCibolaName)
						.addAmount("%money%", treasureAmount)
					).withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
            }
		} break;
		
		case RUINS: {
			int treasureAmount = Randomizer.instance().randomInt(dx * 2) * 300 + 50;
			if (treasureAmount < 500) {
				unit.getOwner().addGold(treasureAmount);
			} else {
				UnitFactory.createTreasureTrain(unit.getOwner(), destTile, treasureAmount);
			}

			if (unit.getOwner().isHuman()) {
				String msgKey = ((mounds) ? "lostCityRumour.moundsTreasure" : "lostCityRumour.ruins");
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(StringTemplate.template(msgKey)
						.addAmount("%money%", treasureAmount)
					).withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
		} break;
		
		case FOUNTAIN_OF_YOUTH: {
			unit.getOwner().getEurope().emigrantsFountainOfYoung(dx);
			
			if (unit.getOwner().isHuman()) {			
				guiGameController.showDialog(new SimpleMessageDialog()
					.withContent("lostCityRumour.fountainOfYouth")
					.withButton("ok")
					.addOnCloseListener(createOnCloseActionListener())
				);
			}
		} break;
		
		case NO_SUCH_RUMOUR:
		case NOTHING:
		default: {
			if (unit.getOwner().isHuman()) {
				if (mounds) {
					guiGameController.showDialog(new SimpleMessageDialog()
						.withContent("lostCityRumour.moundsNothing")
						.withButton("ok")
						.addOnCloseListener(createOnCloseActionListener())
					);
				} else {
					int keyMessagePrefixCount = Messages.keyMessagePrefixCount("lostCityRumour.nothing.");
					int msgIdx = Randomizer.instance().randomInt(keyMessagePrefixCount);
					
					guiGameController.showDialog(new SimpleMessageDialog()
						.withContent("lostCityRumour.nothing." + msgIdx)
						.withButton("ok")
						.addOnCloseListener(createOnCloseActionListener())
					);
				}
			}
		} break;
		}
	}

	private Runnable createOnCloseActionListener() {
		return guiGameController.ifRequiredNextActiveUnitRunnable();
	}	
	
	public LostCityRumourService showLostCityRumourConfirmation(final MoveInThreadService moveInThreadService, MoveContext moveContext) {
		if (moveContext.isAi()) {
			throw new IllegalStateException("can run only by human");
		}
		
		QuestionDialog.OptionAction<MoveContext> exploreLostCityRumourYesAnswer = new QuestionDialog.OptionAction<MoveContext>() {
			@Override
			public void executeAction(final MoveContext mc) {
				moveInThreadService.confirmedMoveProcessor(mc, new AfterMoveProcessor() {
					@Override
					public void afterMove(final MoveContext mc) {
						RumourType explorationResult = processExploration(mc);
						System.out.println(String.format("player[%s].exploration lost city rumour result %s", mc.unit.getOwner().getId(), explorationResult));
						guiGameController.resetMapModelOnTile(mc.destTile);
					}
				});
			}
		}; 
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addQuestion(StringTemplate.label("exploreLostCityRumour.text"));
        questionDialog.addAnswer("exploreLostCityRumour.yes", exploreLostCityRumourYesAnswer, moveContext);
        questionDialog.addAnswer("exploreLostCityRumour.no", QuestionDialog.DO_NOTHING_ACTION, moveContext);
        guiGameController.showDialog(questionDialog);
		return this;
	}
	
	public RumourType processExploration(MoveContext mc) {
		mc.destTile.removeLostCityRumour();
		
		LostCityRumour lostCityRumour = new LostCityRumour();
		RumourType type = lostCityRumour.type(game, mc.unit, mc.destTile);
		handleLostCityRumourType(mc, type);
		return type;
	}
	
	/**
	 * Cashin treasure and send notification to wagon owner and other players.
	 * @param treasureWagon {@link Unit} - treasure wagon
	 */
    public static void cashInTreasure(Game game, Unit treasureWagon, GUIGameController guiGameController) {
    	String messageId = null;
    	int cashInAmount = 0;
    	int fullAmount = treasureWagon.getTreasureAmount();
    	
    	if (treasureWagon.getOwner().isColonial()) {
    		// Charge transport fee and apply tax
    		cashInAmount = (fullAmount - treasureWagon.treasureTransportFee()) * (100 - treasureWagon.getOwner().getTax()) / 100;
    		messageId = "model.unit.cashInTreasureTrain.colonial";
    	} else {
            // No fee possible, no tax applies.
            cashInAmount = fullAmount;
            messageId = "model.unit.cashInTreasureTrain.independent";
    	}
    	treasureWagon.getOwner().addGold(cashInAmount);
    	
    	if (treasureWagon.getOwner().isHuman()) {
    		StringTemplate msgSt = StringTemplate.template(messageId)
				.addAmount("%amount%", fullAmount)
				.addAmount("%cashInAmount%", cashInAmount);
    		
    		if (treasureWagon.isAtLocation(Europe.class)) {
    			treasureWagon.getOwner().eventsNotifications.addMessageNotification(msgSt);
    		} else {
    			guiGameController.showDialog(new SimpleMessageDialog()
					.withContent(msgSt)
					.withButton("ok")
					.addOnCloseListener(guiGameController.ifRequiredNextActiveUnitRunnable())
				);
    		}
    	}
    	
    	if (treasureWagon.getOwner().isRebel() || treasureWagon.getOwner().isIndependent()) {
    		messageId = "model.unit.cashInTreasureTrain.other.independent";
    	} else {
    		messageId = "model.unit.cashInTreasureTrain.other.colonial";
    	}
    	StringTemplate msgSt = StringTemplate.template(messageId)
            .addAmount("%amount%", fullAmount)
            .addStringTemplate("%nation%", treasureWagon.getOwner().getNationName());
    	
    	for (Player p : game.players.entities()) {
    		if (p.isLiveEuropeanPlayer() && p.notEqualsId(treasureWagon.getOwner())) {
    			p.eventsNotifications.addMessageNotification(msgSt);
    		}
    	}
    	treasureWagon.getOwner().removeUnit(treasureWagon);
    }
	
}
