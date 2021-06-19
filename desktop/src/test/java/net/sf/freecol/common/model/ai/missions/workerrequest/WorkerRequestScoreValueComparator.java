package net.sf.freecol.common.model.ai.missions.workerrequest;

import net.sf.freecol.common.model.Tile;

import java.util.function.Predicate;

public class WorkerRequestScoreValueComparator {

    public static Predicate<WorkerRequestScoreValue> eq(Tile tile, String unitTypeId) {
        return new Predicate<WorkerRequestScoreValue>() {
            @Override
            public boolean test(WorkerRequestScoreValue workerRequestScoreValue) {
                return workerRequestScoreValue.getLocation().equalsCoordinates(tile)
                    && workerRequestScoreValue.getWorkerType().equalsId(unitTypeId);
            }
        };
    }

    public static Predicate<WorkerRequestScoreValue> unitTypeEq(String unitTypeId) {
        return new Predicate<WorkerRequestScoreValue>() {
            @Override
            public boolean test(WorkerRequestScoreValue workerRequestScoreValue) {
                return workerRequestScoreValue.getWorkerType().equalsId(unitTypeId);
            }
        };
    }
}
