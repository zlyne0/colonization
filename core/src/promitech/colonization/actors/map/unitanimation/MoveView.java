package promitech.colonization.actors.map.unitanimation;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import net.sf.freecol.common.model.Unit;
import promitech.colonization.actors.map.MapActor;
import promitech.colonization.gamelogic.MoveContext;

public class MoveView {

    private MapActor mapActor;
    
    public void setMapActor(MapActor mapActor) {
        this.mapActor = mapActor;
    }
    
    public void showMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
    	UnitDislocationAnimation animation = getFromPool(UnitDislocationAnimation.class);
    	animation.init( 
            moveContext.unit,
            moveContext.sourceTile,
            moveContext.destTile,
            endOfAnimation
        );
        mapActor.startUnitTileAnimation(animation);
    }
    
    public void showFailedAttackMoveUnblocked(MoveContext moveContext, Runnable endOfAnimation) {
    	UnitFailAttackAnimation animation = getFromPool(UnitFailAttackAnimation.class);
        animation.init(
	        moveContext.unit,
	        moveContext.sourceTile,
	        moveContext.destTile,
	        endOfAnimation
		);
        mapActor.startUnitTileAnimation(animation);
    }

	public void showAttackRetreat(MoveContext moveContext, Runnable endOfAnimation) {
		UnitAttackRetreatAnimation animation = getFromPool(UnitAttackRetreatAnimation.class);
		animation.init(
            moveContext.unit,
            moveContext.sourceTile,
            moveContext.destTile,
            endOfAnimation
		);
		mapActor.startUnitTileAnimation(animation);		
	}
	
	public void showSuccessfulAttackWithMove(MoveContext moveContext, Unit loser, Runnable endOfAnimation) {
		SuccessAttackAnimation animation = getFromPool(SuccessAttackAnimation.class);
		animation.init(
			moveContext.unit, 
			moveContext.sourceTile, 
			moveContext.destTile, 
			loser, endOfAnimation
		);
		mapActor.startUnitTileAnimation(animation);
	}

    private <T> T getFromPool(Class<T> type) {
    	Pool<T> pool = Pools.get(type);
    	return pool.obtain();
    }
}
