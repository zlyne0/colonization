package promitech.colonization.ai.purchase

import net.sf.freecol.common.model.Game
import net.sf.freecol.common.model.colonyproduction.ColonyProductionLogger.logger
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.ai.ColonyProductionPlaner
import promitech.colonization.ai.TurnRateOfReturn

class PurchasePlaner(private val game: Game) {
    private val maximalTurnRateOfReturn = TurnRateOfReturn(30)
    private lateinit var colonyProductionPlaner: ColonyProductionPlaner
    private lateinit var player: Player

    var avoidPurchasesAndCollectGold: Boolean = false

    fun init(player: Player, colonyProductionPlaner: ColonyProductionPlaner) {
        this.avoidPurchasesAndCollectGold = false
        this.colonyProductionPlaner = colonyProductionPlaner
        this.player = player
    }

    fun buyBuildings() {
        if (avoidPurchasesAndCollectGold) {
            return
        }
        val productionRecommendations = colonyProductionPlaner.generateColonyPlanProductionRecommendations()

        for (recommendation in productionRecommendations) {
            recommendation.buildingTypeScore.whenNotNull { buildingTypeScore ->
                if (buildingTypeScore.turnRateOfReturn.isBetter(maximalTurnRateOfReturn) && buildingTypeScore.price <= player.gold) {
                    if (logger.isDebug) {
                        logger.debug(
                            "player[%s].colony[%s, %s].pay[%d].forBuildingType[%s]",
                            player.id, buildingTypeScore.colony.id, buildingTypeScore.colony.name,
                            buildingTypeScore.price, buildingTypeScore.buildingType
                        )
                    }
                    buildingTypeScore.colony.aiPayForBuilding(buildingTypeScore.buildingType, game)
                }
            }
        }
    }

    fun avoidPurchasesAndCollectGold() {
        avoidPurchasesAndCollectGold = true
    }

}