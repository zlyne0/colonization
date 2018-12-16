package promitech.colonization.orders.diplomacy;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.player.Tension;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
import promitech.colonization.GameResources;
import promitech.colonization.orders.move.HumanPlayerInteractionSemaphore;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.Map;
import promitech.colonization.screen.map.diplomacy.SettlementImageLabel;
import promitech.colonization.screen.map.diplomacy.SpeakResultMsgDialog;
import promitech.colonization.screen.map.hud.DiplomacyContactDialog;
import promitech.colonization.screen.map.hud.FirstContactDialog;
import promitech.colonization.screen.map.hud.GUIGameController;
import promitech.colonization.screen.map.hud.GUIGameModel;
import promitech.colonization.ui.ModalDialog;
import promitech.colonization.ui.QuestionDialog;
import promitech.colonization.ui.QuestionDialog.OptionAction;
import promitech.colonization.ui.resources.Messages;
import promitech.colonization.ui.resources.StringTemplate;

public class FirstContactController {

	private FirstContactService firstContactService;
	private GUIGameController guiGameController;
	private Map screenMap;
	private MoveService moveService;
	private GUIGameModel guiGameModel;
	
    private final HumanPlayerInteractionSemaphore humanPlayerInteractionSemaphore = new HumanPlayerInteractionSemaphore();
	
    public FirstContactController() {
    }
    
	public void inject(
			GUIGameController guiGameController, 
			MoveService moveService,
			GUIGameModel guiGameModel
	) {
		this.guiGameController = guiGameController;
		this.moveService = moveService;
		this.guiGameModel = guiGameModel;
		this.firstContactService = new FirstContactService(this, guiGameModel);
	}

    /**
     * Method wait until human answer on first contact
     * @param humanPlayer
     * @param aiPlayer
     * @param humanPlayerInteractionSemaphore 
     */
    public void blockedShowFirstContactDialog(Player humanPlayer, Player aiPlayer) {
    	showFirstContactDialog(humanPlayer, aiPlayer);
        humanPlayerInteractionSemaphore.waitForInteraction();
    }

    private ModalDialog<?> showFirstContactDialog(Player humanPlayer, Player aiPlayer) {
    	ModalDialog<?> dialog = null; 
    	if (aiPlayer.isEuropean()) {
    		dialog = new DiplomacyContactDialog(screenMap, guiGameModel.game, 
				humanPlayer, aiPlayer, 
				humanPlayerInteractionSemaphore
			).addPeaceOffer();
    	} else {
    		dialog = new FirstContactDialog(humanPlayer, aiPlayer, humanPlayerInteractionSemaphore);
    	}
        guiGameController.showDialog(dialog);
        return dialog;
    }
	
	public void showScoutMoveToForeignColonyQuestion(final Colony colony, final Unit scout) {
		final EventListener onEndScoutActionListener = new EventListener() {
			@Override
			public boolean handle(Event event) {
				scout.reduceMovesLeftToZero();
				guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();						
				return true;
			}
		};

        final QuestionDialog.OptionAction<Unit> negotiateAnswer = new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(Unit scout) {
            	ModalDialog<?> dialog = new DiplomacyContactDialog(screenMap, guiGameModel.game, 
    				scout.getOwner(), 
    				colony.getOwner(), 
    				humanPlayerInteractionSemaphore
    			).addOnCloseListener(onEndScoutActionListener);
                guiGameController.showDialog(dialog);
            }
        };
        final QuestionDialog.OptionAction<Unit> spyAnswer = new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(final Unit scout) {
				guiGameController.showColonyScreenSpyMode(colony.tile, onEndScoutActionListener);
            }
        };
        final QuestionDialog.OptionAction<Unit> attackAnswer = new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(Unit scout) {
            	MoveContext mc = new MoveContext(scout.getTile(), colony.tile, scout);
            	mc.moveType = MoveType.ATTACK_SETTLEMENT;
            	moveService.preMoveProcessorInNewThread(mc, guiGameController.ifRequiredNextActiveUnit());
            }
        };
        
        StringTemplate head = StringTemplate.template("scoutColony.text")
    		.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(scout))
    		.add("%colony%", colony.getName());
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addDialogActor(new SettlementImageLabel(colony)).align(Align.center).row();
		questionDialog.addQuestion(head);
        questionDialog.addAnswer("scoutColony.negotiate", negotiateAnswer, scout);
        questionDialog.addAnswer("scoutColony.spy", spyAnswer, scout);
        questionDialog.addAnswer("scoutColony.attack", attackAnswer, scout);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
	}
	
	public void showScoutMoveToIndianSettlementQuestion(
		final IndianSettlement indianSettlement, 
		final Unit scout 
	) {
        final QuestionDialog.OptionAction<Unit> speakToChiefAction = new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(Unit scout) {
            	int oldGold = scout.getOwner().getGold();
            	SpeakToChiefResult speakResult = firstContactService.scoutSpeakWithIndianSettlementChief(
        			indianSettlement, 
        			scout
    			);
            	System.out.println("speak to chief result " + speakResult);
            	speakToChiefResultMessage(speakResult, indianSettlement, scout, oldGold);
            }
        };

        final QuestionDialog.OptionAction<Unit> attackAnswer = new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(Unit scout) {
            	MoveContext moveContext = new MoveContext(scout.getTile(), indianSettlement.tile, scout);
            	moveContext.moveType = MoveType.ATTACK_SETTLEMENT;
            	moveService.preMoveProcessorInNewThread(moveContext, guiGameController.ifRequiredNextActiveUnit());
            }
        };
        
        String settlementTypeKey = indianSettlement.getOwner().nationType().regularSettlementTypePluralMsgKey();
        
        String headStr = StringTemplate.template("scoutSettlement.greetings")
        	.addStringTemplate("%nation%", indianSettlement.getOwner().getNationName())
        	.add("%settlement%", indianSettlement.getName())
        	.add("%number%", Integer.toString(indianSettlement.getOwner().settlements.size()))
        	.addKey("%settlementType%", settlementTypeKey)
        	.eval();
        
        if (indianSettlement.getLearnableSkill() != null) {
        	headStr += "\r\n" + StringTemplate.template("scoutSettlement.skill")
        		.addName("%skill%", indianSettlement.getLearnableSkill())
        		.eval();
        }
        
        List<GoodsType> wantedGoods = indianSettlement.getWantedGoods();
        if (wantedGoods.size() > 0) {
        	StringTemplate template = StringTemplate.template("scoutSettlement.trade." + wantedGoods.size());
        	for (int i=0; i<wantedGoods.size(); i++) {
        		template.addName("%goods" + Integer.toString(i+1) + "%", wantedGoods.get(i));
        	}
        	headStr += "\r\n" + template.eval();
        }
        
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addDialogActor(new SettlementImageLabel(indianSettlement)).align(Align.center).row();
		questionDialog.addQuestion(headStr);
        questionDialog.addAnswer("scoutSettlement.speak", speakToChiefAction, scout);
        questionDialog.addAnswer("scoutSettlement.tribute", new QuestionDialog.OptionAction<Unit>() {
            @Override
            public void executeAction(Unit scout) {
            	demandTributeFromIndian(indianSettlement, scout);
            }
        }, scout);
        questionDialog.addAnswer("scoutSettlement.attack", attackAnswer, scout);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
	}

	public void demandTributeFromSettlement(Settlement settlement, Unit unit) {
		if (settlement.isColony()) {
			demandTributeFromColony(settlement.getColony(), unit);
		} else {
			demandTributeFromIndian(settlement.getIndianSettlement(), unit);
		}
	}
	
	private void demandTributeFromColony(Colony colony, Unit unit) {
		ModalDialog<?> dialog;
		Player colonyOwner = colony.getOwner();
		if (!colonyOwner.hasGold()) {
			String msg = StringTemplate.template("confirmTribute.broke")
				.addStringTemplate("%nation%", colonyOwner.getNationName())
				.eval();
			dialog = new SpeakResultMsgDialog(colony, msg)
				.addOnCloseListener(guiGameController.ifRequiredNextActiveUnitRunnable());
		} else {
			dialog = new DiplomacyContactDialog(screenMap, guiGameModel.game, 
				unit.getOwner(), 
				colonyOwner, 
				humanPlayerInteractionSemaphore
			).addDemandTributeAggrement(1000);
		}
		guiGameController.showDialog(dialog);
	}
	
	private void demandTributeFromIndian(IndianSettlement indianSettlement, Unit unit) {
		int demandedGold = firstContactService.demandTributeFromIndian(
			guiGameModel.game, 
			indianSettlement, 
			unit
		);
    	String msg;
    	if (demandedGold != 0) {
    		msg = StringTemplate.template("scoutSettlement.tributeAgree")
				.addAmount("%amount%", demandedGold)
				.eval();
    	} else {
    		msg = Messages.msg("scoutSettlement.tributeDisagree");
    	}
    	showSpeakResultMsg(indianSettlement, msg);
	}
	
	private void speakToChiefResultMessage(
		SpeakToChiefResult speakResult, 
		IndianSettlement indianSettlement,
		Unit unit,
		int oldGold
	) {
		String msg = null;
		
		switch (speakResult) {
		case DIE:
			msg = Messages.msg("scoutSettlement.speakDie");
			break;
		case NOTHING:
			msg = StringTemplate.template("scoutSettlement.speakNothing")
				.addStringTemplate("%nation%", unit.getOwner().getNationName())
				.eval();
			break;
		case BEADS:
			msg = StringTemplate.template("scoutSettlement.speakBeads")
					.addAmount("%amount%", unit.getOwner().getGold() - oldGold)
					.eval();
			break;
		case EXPERT:
			msg = StringTemplate.template("scoutSettlement.expertScout")
				.addName("%unit%", unit.unitType)
				.eval();
			break;
		case TALES:
			screenMap.resetMapModel();
			msg = Messages.msg("scoutSettlement.speakTales");
			break;
		default:
			throw new IllegalStateException("speak result " + speakResult + " not implemented");
		}

		showSpeakResultMsg(indianSettlement, msg);
	}

	public void setScreenMap(Map screenMap) {
		this.screenMap = screenMap;
	}

	public void learnSkill(final Unit studentUnit, final IndianSettlement is) {
		if (is.getLearnableSkill() == null) {
			showSpeakResultMsg(is, Messages.msg("indianSettlement.noMoreSkill"));
		} else if (!studentUnit.unitType.canBeUpgraded(is.getLearnableSkill(), ChangeType.NATIVES)) {
			String msg = StringTemplate.template("indianSettlement.cantLearnSkill")
				.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(studentUnit))
				.addName("%skill%", is.getLearnableSkill())
				.eval();
			showSpeakResultMsg(is, msg);
		} else {
			confirmLearnSkill(studentUnit, is);
		}
	}

	private void confirmLearnSkill(Unit studentUnit, final IndianSettlement is) {
		QuestionDialog.OptionAction<Unit> yesLearnSkillAction = new QuestionDialog.OptionAction<Unit>() {
			@Override
			public void executeAction(Unit studentUnit) {
				LearnSkillResult learnSkill = firstContactService.learnSkill(is, studentUnit);
				String msg = null;
				switch (learnSkill) {
				case KILL:
					msg = Messages.msg("learnSkill.die");
					break;
				case LEARN:
					guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable();
					break;
				case NOTHING:
					msg = Messages.msg("learnSkill.leave");
					break;
				}
				if (msg != null) {
			    	showSpeakResultMsg(is, msg);
				}
			}
		};
		
		String headStr = StringTemplate.template("learnSkill.text")
			.addName("%skill%", is.getLearnableSkill())
			.eval();
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(new SettlementImageLabel(is)).align(Align.center).row();
		questionDialog.addQuestion(headStr);
		questionDialog.addAnswer("learnSkill.yes", yesLearnSkillAction, studentUnit);
		questionDialog.addAnswer("learnSkill.no", new QuestionDialog.OptionAction<Unit>() {
		    @Override
		    public void executeAction(Unit studentUnit) {
		    	showSpeakResultMsg(is, Messages.msg("learnSkill.leave"));
		    }
		}, studentUnit);
		guiGameController.showDialog(questionDialog);
	}

	public void indianSettlementMissionary(Unit missionary, final IndianSettlement is) {
		String headStr = StringTemplate.template("missionarySettlement.question")
			.add("%settlement%", is.getName())
			.eval();
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(new SettlementImageLabel(is)).align(Align.center).row();
		questionDialog.addQuestion(headStr);
		if (!is.hasMissionary()) {
			questionDialog.addAnswer("missionarySettlement.establish", new QuestionDialog.OptionAction<Unit>() {
				@Override
				public void executeAction(Unit missionary) {
					firstContactService.establishMission(is, missionary);
					successfullyEstablishMissionMsg(missionary, is);
				}
			}, missionary);
		}
		if (is.hasMissionaryNotPlayer(missionary.getOwner())) {
			questionDialog.addAnswer("missionarySettlement.heresy", new QuestionDialog.OptionAction<Unit>() {
			    @Override
			    public void executeAction(Unit missionary) {
			    	DenouceMissionResult denounceMission = firstContactService.denounceMission(is, missionary);
			    	if (denounceMission == DenouceMissionResult.FAILURE) {
			    		String msg = StringTemplate.template("indianSettlement.mission.noDenounce")
		    				.addStringTemplate("%nation%", missionary.getOwner().getNationName())
		    				.eval();
			    		showSpeakResultMsg(is, msg);
			    	} else if (denounceMission == DenouceMissionResult.SUCCESS) {
			    		successfullyEstablishMissionMsg(missionary, is);
			    	}
			    }
			}, missionary);
		}
		questionDialog.addAnswer("missionarySettlement.incite", new QuestionDialog.OptionAction<Unit>() {
		    @Override
		    public void executeAction(Unit missionary) {
		    	showIncitePlayers(missionary, is);
		    }
		}, missionary);
		questionDialog.addOnlyCloseAnswer("cancel");
		guiGameController.showDialog(questionDialog);
	}

	protected void showIncitePlayers(final Unit inciteUnit, final Settlement settlement) {
		final Image unitImage = new Image(new TextureRegionDrawable(
			GameResources.instance.getFrame(inciteUnit.resourceImageKey()).texture
		), Scaling.none, Align.center);
		
		final OptionAction<Player> incitePlayerAction = new OptionAction<Player>() {
			@Override
			public void executeAction(Player enemy) {
				incitePriceConfirmation(settlement.getOwner(), enemy, inciteUnit);
			}
		};
		
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(unitImage).align(Align.center).row();
		questionDialog.addQuestion(StringTemplate.template("missionarySettlement.inciteQuestion"));
		for (Player player : guiGameModel.game.players.entities()) {
			if (player.isLiveEuropeanPlayer() && player.notEqualsId(inciteUnit.getOwner())) {
				questionDialog.addAnswer(player.getNationName(), incitePlayerAction, player);
			}
		}
		questionDialog.addOnlyCloseAnswer("missionarySettlement.cancel");
		guiGameController.showDialog(questionDialog);
	}

	private void incitePriceConfirmation(final Player nativePlayer, final Player enemy, final Unit missionary) {
		final int incitePrice = firstContactService.inciteIndianPrice(
			nativePlayer, 
			enemy, 
			missionary
		);

		final Image unitImage = new Image(new TextureRegionDrawable(
			GameResources.instance.getFrame(missionary.resourceImageKey()).texture
		), Scaling.none, Align.center);
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(unitImage).align(Align.center).row();
		
		if (missionary.getOwner().hasNotGold(incitePrice)) {
			questionDialog.addQuestion(
				StringTemplate.template("missionarySettlement.inciteGoldFail")
					.add("%player%", enemy.getName())
					.addAmount("%amount%", incitePrice)
			);
			questionDialog.addOnlyCloseAnswer("ok");
		} else {
			questionDialog.addQuestion(
				StringTemplate.template("missionarySettlement.inciteConfirm")
	                .add("%player%", enemy.getName())
	                .addAmount("%amount%", incitePrice)
			);
			questionDialog.addAnswer("yes", new OptionAction<Player>() {
				@Override
				public void executeAction(Player payload) {
					firstContactService.inciteIndian(nativePlayer, enemy, missionary, incitePrice);
				}
			}, enemy);
			questionDialog.addOnlyCloseAnswer("no");
		}
		guiGameController.showDialog(questionDialog);
	}
	
	private void successfullyEstablishMissionMsg(Unit missionary, IndianSettlement is) {
		Tension tension = is.getTension(missionary.getOwner());
		String msg = StringTemplate.template("indianSettlement.mission." + tension.getKey())
			.addStringTemplate("%nation%", missionary.getOwner().getNationName())
			.eval();
		showSpeakResultMsg(is, msg);
	}
	
	private void showSpeakResultMsg(Settlement settlement, String msg) {
		guiGameController.showDialog(
			new SpeakResultMsgDialog(settlement, msg)
				.addOnCloseListener(guiGameController.ifRequiredNextActiveUnitRunnable())
		);
	}
}
