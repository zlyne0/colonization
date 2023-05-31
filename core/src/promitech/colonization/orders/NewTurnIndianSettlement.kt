package promitech.colonization.orders

import net.sf.freecol.common.model.IndianSettlement
import net.sf.freecol.common.model.IndianSettlementProduction
import net.sf.freecol.common.model.IndianSettlementWantedGoods
import promitech.colonization.screen.map.hud.GUIGameModel

class NewTurnIndianSettlement(
    var guiGameModel: GUIGameModel
) {

    val indianSettlementProduction = IndianSettlementProduction()
    val indianWantedGoods = IndianSettlementWantedGoods(indianSettlementProduction)

    fun newTurn(settlement: IndianSettlement) {
        indianSettlementProduction.init(guiGameModel.game.map, settlement)

        indianSettlementProduction.updateSettlementGoodsProduction(settlement)
        indianSettlementProduction.updateSettlementPopulationGrowth(guiGameModel.game, settlement)

        settlement.generateTension(guiGameModel.game)
        settlement.conversion(guiGameModel.game.map)
        indianWantedGoods.updateWantedGoods(settlement)
        settlement.spreadMilitaryGoods()
        settlement.equipMilitaryRoles()
    }
}