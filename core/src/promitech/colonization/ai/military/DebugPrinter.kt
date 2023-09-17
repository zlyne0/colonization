package promitech.colonization.ai.military

import net.sf.freecol.common.model.ai.MapTileDebugInfo
import promitech.map.forEach

fun DefencePlaner.printWarRange(mapTileDebugInfo: MapTileDebugInfo) {
    warRange.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun DefencePlaner.printPowerBalance(mapTileDebugInfo: MapTileDebugInfo) {
    powerBalance.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun DefencePlaner.printThreat(mapTileDebugInfo: MapTileDebugInfo) {
    threatInfluenceMap.printInfluencePower(mapTileDebugInfo)
}

fun DefencePlaner.printSingleTileDefence(mapTileDebugInfo: MapTileDebugInfo) {
    singleTileDefence.forEach { x, y, value ->
        mapTileDebugInfo.str(x, y, value.toString())
    }
}

fun DefencePlaner.printColonyDefencePriority(mapTileDebugInfo: MapTileDebugInfo) {
    val calculateColonyDefencePriority = calculateColonyDefencePriority()
    calculateColonyDefencePriority.forEachIndexed { index, colonyThreat ->
        mapTileDebugInfo.str(colonyThreat.colony.tile.x, colonyThreat.colony.tile.y, index.toString())
    }
}

