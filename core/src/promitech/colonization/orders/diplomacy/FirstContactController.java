package promitech.colonization.orders.diplomacy;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Align;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import net.sf.freecol.common.model.specification.UnitTypeChange.ChangeType;
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
		guiGameController.showDialog(
			new SpeakResultMsgDialog(indianSettlement, msg).addOnCloseListener(createOnEndScoutActionListener(moveContext))
		);
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

		guiGameController.showDialog(
			new SpeakResultMsgDialog(
				moveContext.destTile.getSettlement(),
				msg
			).addOnCloseListener(createOnEndScoutActionListener(moveContext))
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
	
	public void setScreenMap(Map screenMap) {
		this.screenMap = screenMap;
	}

	public void learnSkill(MoveContext moveContext) {
		final IndianSettlement is = moveContext.destTile.getSettlement().getIndianSettlement();
		
		if (is.getLearnableSkill() == null) {
			guiGameController.showDialog(
				new SpeakResultMsgDialog(
					moveContext.destTile.getSettlement(),
					Messages.msg("indianSettlement.noMoreSkill")
				).addOnCloseListener(createOnEndScoutActionListener(moveContext))
			);
		} else if (!moveContext.unit.unitType.canBeUpgraded(is.getLearnableSkill(), ChangeType.NATIVES)) {
			String msg = StringTemplate.template("indianSettlement.cantLearnSkill")
				.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(moveContext.unit))
				.addName("%skill%", is.getLearnableSkill())
				.eval();
			guiGameController.showDialog(
				new SpeakResultMsgDialog(
					moveContext.destTile.getSettlement(),
					msg
				).addOnCloseListener(createOnEndScoutActionListener(moveContext))
			);
		} else {
			confirmLearnSkill(moveContext, is);
		}
	}

	private void confirmLearnSkill(MoveContext moveContext, final IndianSettlement is) {
		QuestionDialog.OptionAction<MoveContext> yesLearnSkillAction = new QuestionDialog.OptionAction<MoveContext>() {
			@Override
			public void executeAction(MoveContext payload) {
				LearnSkillResult learnSkill = firstContactService.learnSkill(is, payload);
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
					guiGameController.showDialog(
						new SpeakResultMsgDialog(
							payload.destTile.getSettlement(),
							msg
						).addOnCloseListener(createOnEndScoutActionListener(payload))
					);
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
				guiGameController.showDialog(
					new SpeakResultMsgDialog(
						payload.destTile.getSettlement(),
						Messages.msg("learnSkill.leave")
					).addOnCloseListener(createOnEndScoutActionListener(payload))
				);
		    }
		}, moveContext);
		guiGameController.showDialog(questionDialog);
	}
	
}
