package promitech.colonization.ai.military

import net.sf.freecol.common.model.Colony

class ColonyThreat(
    val colony: Colony,
    val colonyThreatWeights: ColonyThreatWeights,
    val war: Boolean,
    val colonyDefencePower: Float
) {

    companion object {
        val REQUIRE_MORE_DEFENCE_COMPARATOR = Comparator<ColonyThreat> { p1, p2 ->
            ColonyThreatWeights.REQUIRE_MORE_DEFENCE_COMPARATOR.compare(p1.colonyThreatWeights, p2.colonyThreatWeights)
        }
    }
}