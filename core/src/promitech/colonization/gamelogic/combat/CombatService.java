package promitech.colonization.gamelogic.combat;

import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.infrastructure.ThreadsResources;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class CombatService {

    class RunnableCombatConfirmation implements Runnable {
        private MoveContext moveContext;
        private Combat combat;
        private Runnable afterActionListener;
        
        @Override
        public void run() {
            processInitiatedCombat(moveContext, combat);
            afterActionListener.run();
        }
    }
    private final RunnableCombatConfirmation runnableCombatConfirmation = new RunnableCombatConfirmation();
    
    private MoveService moveService;
    
    public void inject(MoveService moveService) {
        this.moveService = moveService;
    }
    
    void doConfirmedCombat(MoveContext moveContext, Combat combat, Runnable afterAction) {
        runnableCombatConfirmation.moveContext = moveContext;
        runnableCombatConfirmation.combat = combat;
        runnableCombatConfirmation.afterActionListener = afterAction;
        ThreadsResources.instance.executeMovement(runnableCombatConfirmation);
    }
    
    public void doCombat(MoveContext moveContext) {
        Combat combat = new Combat();
        combat.init(moveContext.unit, moveContext.destTile);
        
        processInitiatedCombat(moveContext, combat);
    }

    private void processInitiatedCombat(MoveContext moveContext, Combat combat) {
        CombatResult combatResult = combat.generateRandomResult();
        
        if (combatResult.equals(Combat.CombatResult.WIN)) {
            moveService.blockedShowSuccessfulAttackWithMove(moveContext, combat.combatResolver.loser);
        }
        if (combatResult.equals(Combat.CombatResult.LOSE)) {
            moveContext.setUnitKilled();
            moveService.blockedShowFailedAttackMove(moveContext);
        }
        if (combatResult.equals(Combat.CombatResult.EVADE_ATTACK)) {
            moveService.blockedShowAttackRetreat(moveContext);
        }
        
        combat.processAttackResult();
        moveService.postMoveProcessor(moveContext);
    }
}
