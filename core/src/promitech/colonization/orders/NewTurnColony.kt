package promitech.colonization.orders

import net.sf.freecol.common.model.Colony
import promitech.colonization.screen.map.hud.GUIGameModel

class NewTurnColony(
    val guiGameModel: GUIGameModel
) {

    fun newTurn(colony: Colony) {
        if (NewTurnLogger.logger.isDebug) {
            NewTurnLogger.logger.debug("player[%s].colony[%s].newTurn.name[%s]", colony.owner.id, colony.id, colony.name)
        }

        colony.ifPossibleAddFreeBuildings()
        colony.updateColonyFeatures()
        colony.increaseWarehouseByProduction()
        colony.reduceTileResourceQuantity()

        colony.increaseColonySize()
        colony.buildBuildings()

        colony.exportGoods(guiGameModel.game)
        colony.removeExcessedStorableGoods()
        colony.handleLackOfResources(guiGameModel.game)
        colony.calculateSonsOfLiberty()

        colony.increaseWorkersExperience()
    }

}