package promitech.colonization.ai.military

import promitech.colonization.ai.military.ThreatModel.Companion.FLOAT_UNKNOWN_VALUE
import promitech.colonization.ai.military.ThreatModel.Companion.UNKNOWN_VALUE

class ColonyThreatWeights(
    var colonyWealth: Int = UNKNOWN_VALUE,
    var threatPower: Float = FLOAT_UNKNOWN_VALUE,
    var threatProjection: Int = UNKNOWN_VALUE
) {

    fun isRequireMoreDefence(obj: ColonyThreatWeights): Boolean {
        return REQUIRE_MORE_DEFENCE_COMPARATOR.compare(this, obj) < 0
    }

    companion object {
        val REQUIRE_MORE_DEFENCE_COMPARATOR = object: Comparator<ColonyThreatWeights> {
            override fun compare(o1: ColonyThreatWeights, o2: ColonyThreatWeights): Int {
                if (o1.threatProjection != UNKNOWN_VALUE && o2.threatProjection != UNKNOWN_VALUE) {
                    if (o1.threatPower > o2.threatPower) {
                        return -1
                    }
                    if (o1.threatPower == o2.threatPower) {
                        if (o1.threatProjection > o2.threatProjection) {
                            return -1
                        }
                        if (o1.threatProjection == o2.threatProjection) {
                            if (o1.colonyWealth >= o2.colonyWealth) {
                                return -1
                            }
                        }
                    }
                    return 1
                } else {
                    if (o1.threatProjection == UNKNOWN_VALUE && o2.threatProjection == UNKNOWN_VALUE) {
                        if (o1.threatPower > o2.threatPower) {
                            return -1
                        }
                        if (o1.threatPower == o2.threatPower) {
                            if (o1.colonyWealth >= o2.colonyWealth) {
                                return -1
                            }
                        }
                        return 1
                    }
                    if (o1.threatProjection == UNKNOWN_VALUE) {
                        return 1
                    }
                    return -1
                }
            }
        }
    }
}