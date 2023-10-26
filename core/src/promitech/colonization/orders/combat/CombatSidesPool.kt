package promitech.colonization.orders.combat

import com.badlogic.gdx.utils.Pools

internal object CombatSidesPool {

    @JvmField
    internal val combatSidesPool = Pools.get(CombatSides::class.java)

}