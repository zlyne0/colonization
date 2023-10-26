package promitech.colonization.ai.military

import net.sf.freecol.common.model.ai.MapTileDebugInfo
import promitech.map.forEach
import java.math.RoundingMode
import java.text.DecimalFormat

fun ThreatModel.printWarRange(mapTileDebugInfo: MapTileDebugInfo) {
    warRange.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun ThreatModel.printPowerBalance(mapTileDebugInfo: MapTileDebugInfo) {
    powerBalance.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun ThreatModel.printThreat(mapTileDebugInfo: MapTileDebugInfo) {
    threatInfluenceMap.printInfluencePower(mapTileDebugInfo)
}

fun ThreatModel.printSingleTileDefence(mapTileDebugInfo: MapTileDebugInfo) {
    singleTileDefence.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun ThreatModel.printColonyDefencePriority(mapTileDebugInfo: MapTileDebugInfo) {
    val calculateColonyDefencePriority = calculateColonyDefencePriority()

    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.DOWN

    calculateColonyDefencePriority.forEachIndexed { index, colonyThreat ->
        println("$index ${colonyThreat.colony.name}, " +
                "war = ${colonyThreat.war}, " +
                "colonyDefencePower = " + df.format(colonyThreat.colonyDefencePower) +
                ", colonyWealth = ${colonyThreat.colonyThreatWeights.colonyWealth}, " +
                "threatPower = " + df.format(colonyThreat.colonyThreatWeights.threatPower)
        )
    }

    calculateColonyDefencePriority.forEachIndexed { index, colonyThreat ->
        mapTileDebugInfo.str(colonyThreat.colony.tile.x, colonyThreat.colony.tile.y, index.toString())
    }
}

