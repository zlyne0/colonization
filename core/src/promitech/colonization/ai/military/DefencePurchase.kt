package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.Modifier
import promitech.colonization.ai.UnitRoleId
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.ai.military.DefencePurchaseGenerator.DefencePurchase.ColonyBuildingPurchase
import promitech.colonization.ai.military.DefencePurchaseGenerator.DefencePurchase.UnitPurchase
import promitech.colonization.orders.combat.DefencePower
import promitech.colonization.orders.combat.DefencePower.calculatePaperDefencePower

class ColonyDefenceSimulation(
    private val colonyDefenceSnapshot: DefencePower.ColonyDefenceSnapshot
) {
    val purchases = ArrayList<DefencePurchaseGenerator.DefencePurchase>()

    private var colonyBuildingPurchase: ColonyBuildingPurchase? = null
    private var unitsPower = 0f

    fun hasBuilding(): Boolean = colonyBuildingPurchase != null

    fun addPurchase(purchase: DefencePurchaseGenerator.DefencePurchase) {
        purchases.add(purchase)
        if (purchase is ColonyBuildingPurchase) {
            colonyBuildingPurchase = purchase
        }
        if (purchase is UnitPurchase) {
            unitsPower += purchase.defencePower
        }
    }

    fun power(): Float {
        if (colonyBuildingPurchase != null) {
            return colonyDefenceSnapshot.unitsPowerWithBuilding(unitsPower, colonyBuildingPurchase!!.buildingType)
        } else {
            return colonyDefenceSnapshot.unitsPowerWithBuilding(unitsPower)
        }
    }

    fun powerWith(purchase: ColonyBuildingPurchase): Float {
        return colonyDefenceSnapshot.unitsPowerWithBuilding(unitsPower, purchase.buildingType)
    }

    fun powerWith(purchase: UnitPurchase): Float {
        if (colonyBuildingPurchase != null) {
            return colonyDefenceSnapshot.unitsPowerWithBuilding(unitsPower + purchase.defencePower, colonyBuildingPurchase!!.buildingType)
        } else {
            return colonyDefenceSnapshot.unitsPowerWithBuilding(unitsPower + purchase.defencePower)
        }
    }
}

object DefencePurchaseGenerator {

    sealed class DefencePurchase(
        open val price: Int,
    ) {
        abstract fun powerAfterPurchase(defenceSimulation: ColonyDefenceSimulation): Float

        fun isBetterDefencePower(purchase: DefencePurchase, defenceSimulation: ColonyDefenceSimulation): Boolean {
            return powerAfterPurchase(defenceSimulation) > purchase.powerAfterPurchase(defenceSimulation)
        }

        class ColonyBuildingPurchase(
            override val price: Int,
            val buildingType: BuildingType,
        ): DefencePurchase(price) {

            override fun powerAfterPurchase(defenceSimulation: ColonyDefenceSimulation): Float {
                return defenceSimulation.powerWith(this)
            }

            override fun toString(): String {
                return "ColonyBuildingPurchase buildingType = $buildingType, price = $price"
            }

            companion object {

                fun generateBuildings(
                    defencePurchases: MutableList<DefencePurchase>,
                    colony: Colony
                ) {
                    for (buildingType in Specification.instance.buildingTypes) {
                        if (isBuildingTypeAllowed(colony, buildingType)) {
                            val price = colony.getAIPriceForBuilding(buildingType)
                            defencePurchases.add(ColonyBuildingPurchase(price, buildingType))
                        }
                    }
                }

                private fun isBuildingTypeAllowed(colony: Colony, buildingType: BuildingType): Boolean {
                    val noBuildReason = colony.getNoBuildReason(buildingType)
                    return buildingType.hasModifier(Modifier.DEFENCE) && noBuildReason == Colony.NoBuildReason.NONE
                }
            }
        }

        class UnitPurchase(
            override val price: Int,
            val unitType: UnitType,
            val unitRole: UnitRole,
            val defencePower: Float
        ): DefencePurchase(price) {

            internal enum class DefenceUnitType(val unitTypeId: UnitTypeId, val unitRoleId: UnitRoleId) {
                Artillery(UnitType.ARTILLERY, UnitRole.DEFAULT_ROLE_ID),
                Soldier(UnitType.FREE_COLONIST, UnitRole.SOLDIER),
                Dragoon(UnitType.FREE_COLONIST, UnitRole.DRAGOON),
                // temporary do not buy veteran because they are expensive and now i can't determine player is rich enough
//                VeteranSoldier(UnitType.VETERAN_SOLDIER, UnitRole.SOLDIER),
//                VeteranDragoon(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON);
            }

            override fun powerAfterPurchase(defenceSimulation: ColonyDefenceSimulation): Float {
                return defenceSimulation.powerWith(this)
            }

            override fun toString(): String {
                return "UnitPurchase type = $unitType, role = $unitRole, price = $price, defencePower = $defencePower"
            }

            companion object {

                fun generateUnits(
                    defencePurchases: MutableList<DefencePurchase>,
                    colony: Colony
                ) {
                    for (defenceUnitType in DefenceUnitType.values()) {
                        if (!isUnitAllowedInColony(defenceUnitType, colony)) {
                            continue
                        }
                        val unitType = Specification.instance.unitTypes.getById(defenceUnitType.unitTypeId)
                        val unitRole = Specification.instance.unitRoles.getById(defenceUnitType.unitRoleId)

                        val defencePower = calculatePaperDefencePower(unitType, unitRole)
                        defencePurchases.add(
                            UnitPurchase(
                                priceInEurope(colony.owner, unitType, unitRole),
                                unitType,
                                unitRole,
                                defencePower
                            )
                        )
                    }
                }

                private fun isUnitAllowedInColony(defenceUnitType: DefenceUnitType, colony: Colony): Boolean {
                    if (defenceUnitType == DefenceUnitType.Artillery) {
                        return colony.hasStockade()
                    }
                    return true
                }

                private fun priceInEurope(player: Player, unitType: UnitType, unitRole: UnitRole): Int {
                    return player.europe.aiUnitPrice(unitType, unitRole);
                }
            }
        }
    }

    fun generateRequests(colony: Colony): List<DefencePurchase> {
        val defencePurchases = mutableListOf<DefencePurchase>()
        ColonyBuildingPurchase.generateBuildings(defencePurchases, colony)
        UnitPurchase.generateUnits(defencePurchases, colony)
        return defencePurchases
    }

}