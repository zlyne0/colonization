package net.sf.freecol.common.model;

import com.badlogic.gdx.utils.ObjectIntMap;

import net.sf.freecol.common.model.specification.BuildableType;
import net.sf.freecol.common.model.specification.RequiredGoods;

import promitech.colonization.ui.resources.StringTemplate;

public class BuildProgress {
    public static final int LACK_OF_RESOURCES = Integer.MAX_VALUE;

    private final ObjectIntMap<String> requiredTurnsForGood = new ObjectIntMap<>(2);
    private int turnsToComplete = -1;

    public static BuildProgress calculateBuildProgress(Colony colony, BuildableType buildableType) {
        BuildProgress turnProgress = new BuildProgress();

        ProductionSummary production = colony.productionSummary();
        GoodsContainer warehouse = colony.goodsContainer;

        for (RequiredGoods requiredGood : buildableType.requiredGoods()) {
            int warehouseAmount = warehouse.goodsAmount(requiredGood.getId());
            int productionAmount = production.getQuantity(requiredGood.getId());
            int goodRequiredTurn;

            if (warehouseAmount < requiredGood.amount) {
                if (productionAmount > 0) {
                    int reqToProduce = requiredGood.amount - warehouseAmount;
                    goodRequiredTurn = reqToProduce / productionAmount;
                    if (reqToProduce % productionAmount != 0) {
                        goodRequiredTurn++;
                    }
                } else {
                    goodRequiredTurn = BuildProgress.LACK_OF_RESOURCES;
                }
            } else {
                goodRequiredTurn = 0;
            }
            turnProgress.put(requiredGood.getId(), goodRequiredTurn);
        }
        return turnProgress;
    }

    private BuildProgress() {
    }

    private void put(String goodsTypeId, int turns) {
        requiredTurnsForGood.put(goodsTypeId, turns);
        if (turns > turnsToComplete) {
            turnsToComplete = turns;
        }
    }

    public int get(String goodsTypeId) {
        return requiredTurnsForGood.get(goodsTypeId, LACK_OF_RESOURCES);
    }

    public boolean noComponentsInProduction() {
        return turnsToComplete == LACK_OF_RESOURCES;
    }

    public boolean isCompleted() {
        return turnsToComplete == 0;
    }

    public int getTurnsToComplete() {
        return turnsToComplete;
    }

    public boolean isProgressStopedBecauseOfLackOfComponents() {
        boolean inProgress = false;
        boolean lack = false;
        for (ObjectIntMap.Entry<String> turns : requiredTurnsForGood) {
            if (turns.value == LACK_OF_RESOURCES) {
                lack = true;
            }
            if (turns.value != 0 && turns.value != LACK_OF_RESOURCES) {
                inProgress = true;
            }
        }
        return lack && !inProgress;
    }

    public void neverFinishBuildingNotification(Colony colony, BuildableType buildableType) {
        for (RequiredGoods requiredGood : buildableType.requiredGoods()) {
            int turnsForGoodsType = requiredTurnsForGood.get(requiredGood.getId(), -1);
            if (turnsForGoodsType == LACK_OF_RESOURCES) {
                int amount = requiredGood.amount - colony.goodsContainer.goodsAmount(requiredGood.getId());

                StringTemplate st = StringTemplate.template("model.colony.buildableNeedsGoods")
                    .addName("%goodsType%", requiredGood.getId())
                    .addAmount("%amount%", amount)
                    .add("%colony%", colony.getName())
                    .addName("%buildable%", buildableType.getId());
                colony.owner.eventsNotifications.addMessageNotification(st);
                break;
            }
        }
    }
}
