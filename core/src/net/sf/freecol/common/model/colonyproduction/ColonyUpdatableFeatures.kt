package net.sf.freecol.common.model.colonyproduction

import net.sf.freecol.common.model.Colony
import net.sf.freecol.common.model.ObjectWithFeatures
import net.sf.freecol.common.model.specification.Ability
import net.sf.freecol.common.model.specification.Modifier

class ColonyUpdatableFeatures {
    val features = ObjectWithFeatures("")

    fun warehouseCapacity(): Int = features.applyModifier(Modifier.WAREHOUSE_STORAGE, 0)
    fun minimumColonySize(): Int = features.applyModifier(Modifier.MINIMUM_COLONY_SIZE, 0)
    fun canExportGoods(): Boolean = features.hasAbility(Ability.EXPORT)
    fun canRepairUnits(): Boolean = features.hasAbility(Ability.REPAIR_UNITS)
    fun canBombardEnemyShip(): Boolean = features.hasAbility(Ability.BOMBARD_SHIPS)
    fun canProduceInWater(): Boolean = features.hasAbility(Ability.PRODUCE_IN_WATER)
    fun hasAbilitiesRequiredFrom(requiredAbilities: ObjectWithFeatures): Boolean
        = features.hasAbilitiesRequiredFrom(requiredAbilities)

    fun addModifiersTo(mods: ObjectWithFeatures, modifierCode: String) {
        mods.addModifierFrom(features, modifierCode)
    }

    fun applyModifiers(modifierCode: String, value: Int): Int {
        return features.applyModifier(modifierCode, value)
    }

    fun updateColonyFeatures(colony: Colony) {
        features.clear()
        if (colony.isCoastland()) {
            features.addAbility(Ability.HAS_PORT_ABILITY)
        }
        for (b in colony.buildings) {
            features.addFeatures(b.buildingType)
        }
        for (ff in colony.owner.foundingFathers.entities()) {
            features.addFeatures(ff)
        }
        features.addFeatures(colony.settlementType)
        features.addModifier(colony.productionBonus())
    }

    fun updateColonyFeatures(colony: Colony, defaultColonySettingProvider: DefaultColonySettingProvider) {
        features.clear()
        if (colony.isCoastland) {
            features.addAbility(Ability.HAS_PORT_ABILITY)
        }
        for (buildingProduction in defaultColonySettingProvider.buildings()) {
            features.addFeatures(buildingProduction.buildingType)
        }
        for (ff in colony.owner.foundingFathers.entities()) {
            features.addFeatures(ff)
        }
        features.addFeatures(colony.settlementType)
        features.addModifier(colony.productionBonus())
    }

}