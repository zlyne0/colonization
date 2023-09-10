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
import promitech.colonization.orders.combat.OffencePower

class DefencePrice {

    internal enum class DefenceUnitType(val unitTypeId: UnitTypeId, val unitRoleId: UnitRoleId) {
        Artillery(UnitType.ARTILLERY, UnitRole.DEFAULT_ROLE_ID),
        Soldier(UnitType.FREE_COLONIST, UnitRole.SOLDIER),
        Dragoon(UnitType.FREE_COLONIST, UnitRole.DRAGOON),
        VeteranSoldier(UnitType.VETERAN_SOLDIER, UnitRole.SOLDIER),
        VeteranDragoon(UnitType.VETERAN_SOLDIER, UnitRole.DRAGOON);
    }

    sealed class DefencePurchase(
        open val price: Int,
        open val tileDefenceAfterPurchase: Float
    ) {

        class ColonyBuildingPurchase(
            override val price: Int,
            override val tileDefenceAfterPurchase: Float,
            val buildingType: BuildingType,
        ): DefencePurchase(price, tileDefenceAfterPurchase) {

            override fun toString(): String {
                return "ColonyBuildingPurchase buildingType = $buildingType, price = $price, defence = $tileDefenceAfterPurchase"
            }
        }

        class UnitPurchase(
            override val price: Int,
            override val tileDefenceAfterPurchase: Float,
            val unitType: UnitType,
            val unitRole: UnitRole,
            val defencePower: Float
        ): DefencePurchase(price, tileDefenceAfterPurchase) {
            override fun toString(): String {
                return "UnitPurchase type = $unitType, role = $unitRole, price = $price, defenceAfter = $tileDefenceAfterPurchase, defencePower = $defencePower"
            }
        }
    }

    fun generateRequests(actualDefenceStrength: Float, colony: Colony): List<DefencePurchase> {
        val defencePurchases = mutableListOf<DefencePurchase>()

        for (buildingType in Specification.instance.buildingTypes) {
            if (buildingType.hasModifier(Modifier.DEFENCE)) {
                checkBuildingType(buildingType, colony, actualDefenceStrength)
                    .whenNotNull {
                        defencePurchases.add(it)
                    }
            }
        }

        for (defenceUnitType in DefenceUnitType.values()) {
            val unitType = Specification.instance.unitTypes.getById(defenceUnitType.unitTypeId)
            val unitRole = Specification.instance.unitRoles.getById(defenceUnitType.unitRoleId)

            val defencePower = OffencePower.calculatePaperDefencePower(unitType, unitRole)
            defencePurchases.add(UnitPurchase(
                priceInEurope(colony.owner, unitType, unitRole),
                actualDefenceStrength + defencePower,
                unitType,
                unitRole,
                defencePower
            ))
        }

        return defencePurchases
    }

    private fun checkBuildingType(buildingType: BuildingType, colony: Colony, actualDefenceStrength: Float): ColonyBuildingPurchase? {
        val noBuildReason = colony.getNoBuildReason(buildingType)
        if (noBuildReason != Colony.NoBuildReason.NONE) {
            return null
        }
        val price = colony.getAIPriceForBuilding(buildingType)
        return ColonyBuildingPurchase(
            price,
            buildingType.applyModifier(Modifier.DEFENCE, actualDefenceStrength),
            buildingType
        )
    }

    private fun priceInEurope(player: Player, unitType: UnitType, unitRole: UnitRole): Int {
        if (unitType.equalsId(UnitType.ARTILLERY)) {
            return player.europe.aiUnitPrice(unitType)
        }
        val unitTypePrice = player.europe.aiUnitPrice(unitType)
        val rolePrice = player.market().aiBidPrice(unitRole.requiredGoods, unitRole.maximumCount);
        return unitTypePrice + rolePrice
    }
}