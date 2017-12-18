package promitech.colonization.gamelogic.combat;

import promitech.colonization.gamelogic.combat.Combat.CombatResult;
import promitech.colonization.infrastructure.ThreadsResources;
import promitech.colonization.orders.move.MoveContext;
import promitech.colonization.orders.move.MoveService;

public class CombatService {

    class Xxx implements Runnable {
        private MoveContext moveContext;
        private Combat combat;
        private Runnable afterActionListener;
        
        @Override
        public void run() {
            processInitiatedCombat(moveContext, combat);
            afterActionListener.run();
        }
    }
    
    private MoveService moveService;
    
    public void inject(MoveService moveService) {
        this.moveService = moveService;
    }
    
    void doConfirmedCombat(MoveContext moveContext, Combat combat, Runnable afterAction) {
        // TODO: refactoring
        Xxx x = new Xxx();
        x.moveContext = moveContext;
        x.combat = combat;
        x.afterActionListener = afterAction;
        ThreadsResources.instance.executeMovement(x);
    }
    
    public void doCombat(MoveContext moveContext) {
        Combat combat = new Combat();
        combat.init(moveContext.unit, moveContext.destTile);
        
        processInitiatedCombat(moveContext, combat);
    }

    private void processInitiatedCombat(MoveContext moveContext, Combat combat) {
        // TODO: random
        CombatResult combatResult = combat.generateGreatWin();
        
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
