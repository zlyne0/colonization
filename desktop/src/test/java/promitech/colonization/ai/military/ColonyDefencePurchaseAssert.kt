package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.specification.BuildingTypeId
import org.assertj.core.api.AbstractAssert
import promitech.colonization.ai.UnitTypeId
import promitech.colonization.ai.military.DefencePurchasePlaner.ColonyDefencePurchase

class ColonyDefencePurchaseAssert(
    defencePurchase: ColonyDefencePurchase
) : AbstractAssert<ColonyDefencePurchaseAssert, ColonyDefencePurchase> (
        defencePurchase,
        ColonyDefencePurchaseAssert::class.java
    )
{
    fun isColony(colony: Colony): ColonyDefencePurchaseAssert {
        if (!actual.colony.equalsId(colony)) {
            failWithMessage("expected colony ${colony.name} but got ${actual.colony.name}")
        }
        return this
    }

    fun isBuilding(buildingTypeId: BuildingTypeId): ColonyDefencePurchaseAssert {
        if (actual.purchase is DefencePrice.DefencePurchase.ColonyBuildingPurchase) {
            val colonyBuildingPurchase = actual.purchase as DefencePrice.DefencePurchase.ColonyBuildingPurchase
            if (!colonyBuildingPurchase.buildingType.equalsId(buildingTypeId)) {
                failWithMessage("expected building type ${buildingTypeId} but got ${colonyBuildingPurchase.buildingType.id}")
            }
        } else {
            failWithMessage("expected colony building type purchase ${buildingTypeId} but got ${actual.purchase.javaClass.name}")
        }
        return this
    }

    fun isUnitType(unitTypeId: UnitTypeId): ColonyDefencePurchaseAssert {
        if (actual.purchase is DefencePrice.DefencePurchase.UnitPurchase) {
            val unitPurchase = actual.purchase as DefencePrice.DefencePurchase.UnitPurchase
            if (!unitPurchase.unitType.equalsId(unitTypeId)) {
                failWithMessage("expected unitType purchase ${unitTypeId} but got ${unitPurchase.unitType.id}")
            }
        } else {
            failWithMessage("expected UnitPurchase type but got ${actual.purchase.javaClass.name}")
        }
        return this
    }

    companion object {
        @JvmStatic
        fun assertThat(defencePurchase: ColonyDefencePurchase): ColonyDefencePurchaseAssert {
            return ColonyDefencePurchaseAssert(defencePurchase)
        }
    }
}