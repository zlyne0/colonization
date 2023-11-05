package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.Specification
import net.sf.freecol.common.model.UnitRole
import net.sf.freecol.common.model.UnitType
import net.sf.freecol.common.model.player.Player
import net.sf.freecol.common.model.specification.BuildingType
import net.sf.freecol.common.model.specification.Modifier
import net.sf.freecol.common.util.whenNotNull
import promitech.colonization.ai.UnitRoleId
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.ai.military.DefencePrice.DefencePurchase.ColonyBuildingPurchase
import promitech.colonization.ai.military.DefencePrice.DefencePurchase.UnitPurchase
import promitech.colonization.orders.combat.DefencePower
import promitech.colonization.orders.combat.DefencePower.calculatePaperDefencePower

class ColonyDefenceSimulation(
    private val colonyDefenceSnapshot: DefencePower.ColonyDefenceSnapshot
) {
    val purchases = ArrayList<DefencePrice.DefencePurchase>()

    private var colonyBuildingPurchase: ColonyBuildingPurchase? = null
    private var unitsPower = 0f

    fun hasBuilding(): Boolean = colonyBuildingPurchase != null

    fun addPurchase(purchase: DefencePrice.DefencePurchase) {
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

object DefencePrice {

    internal enum class DefenceUnitType(val unitTypeId: UnitTypeId, val unitRoleId: UnitRoleId) {
        Artillery(UnitType.ARTILLERY, UnitRole.DEFAULT_ROLE_ID),
        Soldier(UnitType.FREE_COLONIST, UnitRole.SOLDIER),
        Dragoon(UnitType.FREE_COLONIST, UnitRole.DRAGOON),
        VeteranSoldier(UnitType.VETERAN_SOLDIER, UnitRole.SOLDIER),
        VeteranDragoon(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON);
    }

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
        }

        class UnitPurchase(
            override val price: Int,
            val unitType: UnitType,
            val unitRole: UnitRole,
            val defencePower: Float
        ): DefencePurchase(price) {

            override fun powerAfterPurchase(defenceSimulation: ColonyDefenceSimulation): Float {
                return defenceSimulation.powerWith(this)
            }

            override fun toString(): String {
                return "UnitPurchase type = $unitType, role = $unitRole, price = $price, defencePower = $defencePower"
            }
        }
    }

    fun generateRequests(colony: Colony): List<DefencePurchase> {
        val defencePurchases = mutableListOf<DefencePurchase>()

        for (buildingType in Specification.instance.buildingTypes) {
            if (buildingType.hasModifier(Modifier.DEFENCE)) {
                checkBuildingType(buildingType, colony)
                    .whenNotNull {
                        defencePurchases.add(it)
                    }
            }
        }

        for (defenceUnitType in DefenceUnitType.values()) {
            val unitType = Specification.instance.unitTypes.getById(defenceUnitType.unitTypeId)
            val unitRole = Specification.instance.unitRoles.getById(defenceUnitType.unitRoleId)

            val defencePower = calculatePaperDefencePower(unitType, unitRole)
            defencePurchases.add(UnitPurchase(
                priceInEurope(colony.owner, unitType, unitRole),
                unitType,
                unitRole,
                defencePower
            ))
        }

        return defencePurchases
    }

    private fun checkBuildingType(buildingType: BuildingType, colony: Colony): ColonyBuildingPurchase? {
        val noBuildReason = colony.getNoBuildReason(buildingType)
        if (noBuildReason != Colony.NoBuildReason.NONE) {
            return null
        }
        val price = colony.getAIPriceForBuilding(buildingType)
        return ColonyBuildingPurchase(
            price,
            buildingType
        )
    }

    private fun priceInEurope(player: Player, unitType: UnitType, unitRole: UnitRole): Int {
        return player.europe.aiUnitPrice(unitType, unitRole);
    }
}