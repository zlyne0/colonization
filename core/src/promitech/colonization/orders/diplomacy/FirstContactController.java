package promitech.colonization.orders.diplomacy;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.MoveType;
import net.sf.freecol.common.model.UnitLabel;
import net.sf.freecol.common.model.player.Player;
import net.sf.freecol.common.model.specification.GoodsType;
import promitech.colonization.orders.move.HumanPlayerInteractionSemaphore;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;
import promitech.colonization.screen.map.Map;
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
        
        StringTemplate head = StringTemplate.template("scoutColony.text")
    		.addStringTemplate("%unit%", UnitLabel.getPlainUnitLabel(moveContext.unit))
    		.add("%colony%", moveContext.destTile.getSettlement().getName());
        QuestionDialog questionDialog = new QuestionDialog();
		questionDialog.addQuestion(head);
        questionDialog.addAnswer("scoutColony.negotiate", negotiateAnswer, moveContext);
        questionDialog.addAnswer("scoutColony.spy", spyAnswer, moveContext);
        questionDialog.addAnswer("scoutColony.attack", attackAnswer, moveContext);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
	}
	
	public void showScoutMoveToIndianSettlementQuestion(MoveContext moveContext) {
		
        final QuestionDialog.OptionAction<MoveContext> nothing = new QuestionDialog.OptionAction<MoveContext>() {
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
		questionDialog.addQuestion(headStr);
        questionDialog.addAnswer("scoutSettlement.speak", nothing, moveContext);
        questionDialog.addAnswer("scoutSettlement.tribute", nothing, moveContext);
        questionDialog.addAnswer("scoutSettlement.attack", nothing, moveContext);
        questionDialog.addOnlyCloseAnswer("cancel");
        
        guiGameController.showDialog(questionDialog);
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

		final EventListener onEndScoutActionListener = new EventListener() {
			@Override
			public boolean handle(Event event) {
				guiGameController.nextActiveUnitWhenNoMovePointsAsGdxPostRunnable(moveContext);						
				return true;
			}
		};		
		guiGameController.showDialog(
			new SpeakResultMsgDialog(msg).addOnCloseListener(onEndScoutActionListener)
		);
	}

	public void setScreenMap(Map screenMap) {
		this.screenMap = screenMap;
	}
	
}
