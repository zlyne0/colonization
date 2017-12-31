package promitech.colonization.gamelogic.combat;

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.specification.Ability;
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
    
    public boolean canBombardTile(Colony colony, Tile tile, Unit tileUnit) {
		if (tileUnit == null) {
			return false;
		}
		if (tileUnit.isOwner(colony.getOwner())) {
			return false;
		}
		return colony.getOwner().atWarWith(tileUnit.getOwner()) || tileUnit.hasAbility(Ability.PIRACY);
    }

	public void bombardTileCombat(Colony bombardingColony, Tile bombardedTile, Unit bombardedUnit) {
		Combat combat = new Combat();
		combat.init(bombardingColony, bombardedTile, bombardedUnit);
		
		CombatResult combatResult = combat.generateRandomResult();
		switch (combatResult) {
		case WIN:
			moveService.blockedShowSuccessfulBombardAttack(bombardingColony, bombardedTile, combat.combatResolver.loser);
			break;
		case EVADE_ATTACK:
			moveService.blockedShowFailedBombardAttack(bombardingColony.getOwner(), bombardingColony.tile, bombardedTile);
			break;
		default:
			throw new IllegalStateException("unrecoginized combatResult: " + combatResult);
		}
		combat.processAttackResult();
	}
}
