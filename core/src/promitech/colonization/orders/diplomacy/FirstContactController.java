package promitech.colonization.orders.diplomacy;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

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
	
	public void showScoutMoveToForeignColonyQuestion(final MoveContext moveContext) {
		final EventListener onEndScoutActionListener = new EventListener() {
			@Override
			public boolean handle(Event event) {
				moveContext.unit.reduceMovesLeftToZero();
				guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(moveContext);						
				return true;
			}
		};

        final QuestionDialog.OptionAction<MoveContext> negotiateAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
            	ModalDialog<?> dialog = new DiplomacyContactDialog(screenMap, guiGameModel.game, 
    				payload.unit.getOwner(), 
    				payload.destTile.getSettlement().getOwner(), 
    				humanPlayerInteractionSemaphore
    			).addOnCloseListener(onEndScoutActionListener);
                guiGameController.showDialog(dialog);
            }
        };
        final QuestionDialog.OptionAction<MoveContext> spyAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(final MoveContext payload) {
				guiGameController.showColonyScreenSpyMode(payload.destTile, onEndScoutActionListener);
            }
        };
        final QuestionDialog.OptionAction<MoveContext> attackAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
            	payload.moveType = MoveType.ATTACK_SETTLEMENT;
            	moveService.preMoveProcessorInNewThread(payload, guiGameController.ifRequiredNextActiveUnit());
            }
        };
        
        Settlement colony = moveContext.destTile.getSettlement();
        
        StringTemplate head = StringTemplate.template("scoutColony.text")
    		.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(moveContext.unit))
    		.add("%colony%", colony.getName());
        QuestionDialog questionDialog = new QuestionDialog();
        questionDialog.addDialogActor(new SettlementImageLabel(colony)).align(Align.center).row();
		questionDialog.addQuestion(head);
        questionDialog.addAnswer("scoutColony.negotiate", negotiateAnswer, moveContext);
        questionDialog.addAnswer("scoutColony.spy", spyAnswer, moveContext);
        questionDialog.addAnswer("scoutColony.attack", attackAnswer, moveContext);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
	}
	
	public void showScoutMoveToIndianSettlementQuestion(final MoveContext moveContext) {
        final QuestionDialog.OptionAction<MoveContext> speakToChiefAction = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
            	int oldGold = payload.unit.getOwner().getGold();
            	SpeakToChiefResult speakResult = firstContactService.scoutSpeakWithIndianSettlementChief(
        			payload.destTile.getSettlement().getIndianSettlement(), 
        			payload.unit
    			);
            	System.out.println("speak to chief result " + speakResult);
            	speakToChiefResultMessage(speakResult, payload, oldGold);
            }
        };

        final QuestionDialog.OptionAction<MoveContext> attackAnswer = new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
            	payload.moveType = MoveType.ATTACK_SETTLEMENT;
            	moveService.preMoveProcessorInNewThread(payload, guiGameController.ifRequiredNextActiveUnit());
            }
        };
        
        IndianSettlement indianSettlement = moveContext.destTile.getSettlement().getIndianSettlement();
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
        questionDialog.addAnswer("scoutSettlement.speak", speakToChiefAction, moveContext);
        questionDialog.addAnswer("scoutSettlement.tribute", new QuestionDialog.OptionAction<MoveContext>() {
            @Override
            public void executeAction(MoveContext payload) {
            	demandTributeFromIndian(payload);
            }
        }, moveContext);
        questionDialog.addAnswer("scoutSettlement.attack", attackAnswer, moveContext);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
	}

	public void demandTributeFromSettlement(MoveContext moveContext) {
		if (moveContext.destTile.getSettlement().isColony()) {
			demandTributeFromColony(moveContext);
		} else {
			demandTributeFromIndian(moveContext);
		}
	}
	
	private void demandTributeFromColony(MoveContext moveContext) {
		ModalDialog<?> dialog;
		Player colonyOwner = moveContext.destTile.getSettlement().getOwner();
		if (!colonyOwner.hasGold()) {
			String msg = StringTemplate.template("confirmTribute.broke")
				.addStringTemplate("%nation%", colonyOwner.getNationName())
				.eval();
			dialog = new SpeakResultMsgDialog(moveContext.destTile.getSettlement(), msg)
				.addOnCloseListener(createOnEndScoutActionListener(moveContext));
		} else {
			dialog = new DiplomacyContactDialog(screenMap, guiGameModel.game, 
				moveContext.unit.getOwner(), 
				colonyOwner, 
				humanPlayerInteractionSemaphore
			).addDemandTributeAggrement(1000);
		}
		guiGameController.showDialog(dialog);
	}
	
	private void demandTributeFromIndian(final MoveContext moveContext) {
    	IndianSettlement indianSettlement = moveContext.destTile.getSettlement().getIndianSettlement();
		int demandedGold = firstContactService.demandTributeFromIndian(
			guiGameModel.game, 
			indianSettlement, 
			moveContext.unit
		);
    	String msg;
    	if (demandedGold != 0) {
    		msg = StringTemplate.template("scoutSettlement.tributeAgree")
				.addAmount("%amount%", demandedGold)
				.eval();
    	} else {
    		msg = Messages.msg("scoutSettlement.tributeDisagree");
    	}
    	showSpeakResultMsg(moveContext, indianSettlement, msg);
	}
	
	private void speakToChiefResultMessage(SpeakToChiefResult speakResult, final MoveContext moveContext, int oldGold) {
		String msg = null;
		
		switch (speakResult) {
		case DIE:
			msg = Messages.msg("scoutSettlement.speakDie");
			break;
		case NOTHING:
			msg = StringTemplate.template("scoutSettlement.speakNothing")
				.addStringTemplate("%nation%", moveContext.unit.getOwner().getNationName())
				.eval();
			break;
		case BEADS:
			msg = StringTemplate.template("scoutSettlement.speakBeads")
					.addAmount("%amount%", moveContext.unit.getOwner().getGold() - oldGold)
					.eval();
			break;
		case EXPERT:
			msg = StringTemplate.template("scoutSettlement.expertScout")
				.addName("%unit%", moveContext.unit.unitType)
				.eval();
			break;
		case TALES:
			screenMap.resetMapModel();
			msg = Messages.msg("scoutSettlement.speakTales");
			break;
		default:
			throw new IllegalStateException("speak result " + speakResult + " not implemented");
		}

		showSpeakResultMsg(moveContext, moveContext.destTile.getSettlement(), msg);
	}

	public void setScreenMap(Map screenMap) {
		this.screenMap = screenMap;
	}

	public void learnSkill(MoveContext moveContext) {
		final IndianSettlement is = moveContext.destTile.getSettlement().getIndianSettlement();
		
		if (is.getLearnableSkill() == null) {
			showSpeakResultMsg(moveContext, moveContext.destTile.getSettlement(), Messages.msg("indianSettlement.noMoreSkill"));
		} else if (!moveContext.unit.unitType.canBeUpgraded(is.getLearnableSkill(), ChangeType.NATIVES)) {
			String msg = StringTemplate.template("indianSettlement.cantLearnSkill")
				.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(moveContext.unit))
				.addName("%skill%", is.getLearnableSkill())
				.eval();
			showSpeakResultMsg(moveContext, moveContext.destTile.getSettlement(), msg);
		} else {
			confirmLearnSkill(moveContext, is);
		}
	}

	private void confirmLearnSkill(MoveContext moveContext, final IndianSettlement is) {
		QuestionDialog.OptionAction<MoveContext> yesLearnSkillAction = new QuestionDialog.OptionAction<MoveContext>() {
			@Override
			public void executeAction(MoveContext payload) {
				LearnSkillResult learnSkill = firstContactService.learnSkill(is, payload.unit);
				String msg = null;
				switch (learnSkill) {
				case KILL:
					msg = Messages.msg("learnSkill.die");
					break;
				case LEARN:
					guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(payload);
					break;
				case NOTHING:
					msg = Messages.msg("learnSkill.leave");
					break;
				}
				if (msg != null) {
			    	showSpeakResultMsg(payload, payload.destTile.getSettlement(), msg);
				}
			}
		};
		
		String headStr = StringTemplate.template("learnSkill.text")
			.addName("%skill%", is.getLearnableSkill())
			.eval();
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(new SettlementImageLabel(is)).align(Align.center).row();
		questionDialog.addQuestion(headStr);
		questionDialog.addAnswer("learnSkill.yes", yesLearnSkillAction, moveContext);
		questionDialog.addAnswer("learnSkill.no", new QuestionDialog.OptionAction<MoveContext>() {
		    @Override
		    public void executeAction(MoveContext payload) {
		    	showSpeakResultMsg(payload, payload.destTile.getSettlement(), Messages.msg("learnSkill.leave"));
		    }
		}, moveContext);
		guiGameController.showDialog(questionDialog);
	}

	public void indianSettlementMissionary(MoveContext moveContext) {
		final IndianSettlement is = moveContext.destTile.getSettlement().getIndianSettlement();
		
		String headStr = StringTemplate.template("missionarySettlement.question")
			.add("%settlement%", is.getName())
			.eval();
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(new SettlementImageLabel(is)).align(Align.center).row();
		questionDialog.addQuestion(headStr);
		if (!is.hasMissionary()) {
			questionDialog.addAnswer("missionarySettlement.establish", new QuestionDialog.OptionAction<MoveContext>() {
				@Override
				public void executeAction(MoveContext payload) {
					firstContactService.establishMission(is, payload.unit);
					successfullyEstablishMissionMsg(payload, is);
				}
			}, moveContext);
		}
		if (is.hasMissionaryNotPlayer(moveContext.unit.getOwner())) {
			questionDialog.addAnswer("missionarySettlement.heresy", new QuestionDialog.OptionAction<MoveContext>() {
			    @Override
			    public void executeAction(MoveContext payload) {
			    	DenouceMissionResult denounceMission = firstContactService.denounceMission(is, payload.unit);
			    	if (denounceMission == DenouceMissionResult.FAILURE) {
			    		String msg = StringTemplate.template("indianSettlement.mission.noDenounce")
		    				.addStringTemplate("%nation%", payload.unit.getOwner().getNationName())
		    				.eval();
			    		showSpeakResultMsg(payload, payload.destTile.getSettlement(), msg);
			    	} else if (denounceMission == DenouceMissionResult.SUCCESS) {
			    		successfullyEstablishMissionMsg(payload, is);
			    	}
			    }
			}, moveContext);
		}
		questionDialog.addAnswer("missionarySettlement.incite", new QuestionDialog.OptionAction<MoveContext>() {
		    @Override
		    public void executeAction(MoveContext payload) {
		    	showIncitePlayers(payload);
		    }
		}, moveContext);
		questionDialog.addOnlyCloseAnswer("cancel");
		guiGameController.showDialog(questionDialog);
	}

	protected void showIncitePlayers(final MoveContext moveContext) {
		final Image unitImage = new Image(new TextureRegionDrawable(
			GameResources.instance.getFrame(moveContext.unit.resourceImageKey()).texture
		), Scaling.none, Align.center);
		
		final OptionAction<Player> incitePlayerAction = new OptionAction<Player>() {
			@Override
			public void executeAction(Player enemy) {
				incitePriceConfirmation(moveContext.destTile.getSettlement().getOwner(), enemy, moveContext.unit);
			}
		};
		
		QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addDialogActor(unitImage).align(Align.center).row();
		questionDialog.addQuestion(StringTemplate.template("missionarySettlement.inciteQuestion"));
		for (Player player : guiGameModel.game.players.entities()) {
			if (player.isLiveEuropeanPlayer()) {
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
	
	private void successfullyEstablishMissionMsg(MoveContext moveContext, IndianSettlement is) {
		Tension tension = is.getTension(moveContext.unit.getOwner());
		String msg = StringTemplate.template("indianSettlement.mission." + tension.getKey())
			.addStringTemplate("%nation%", moveContext.unit.getOwner().getNationName())
			.eval();
		showSpeakResultMsg(moveContext, is, msg);
	}
	
	private void showSpeakResultMsg(MoveContext moveContext, Settlement settlement, String msg) {
		guiGameController.showDialog(
			new SpeakResultMsgDialog(settlement, msg)
				.addOnCloseListener(createOnEndScoutActionListener(moveContext))
		);
	}

	private EventListener createOnEndScoutActionListener(final MoveContext moveContext) {
		return new EventListener() {
			@Override
			public boolean handle(Event event) {
				guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(moveContext);						
				return true;
			}
		};
	}
	
}
