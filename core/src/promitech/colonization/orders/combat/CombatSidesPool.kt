package promitech.colonization.orders.combat

import com.badlogic.gdx.utils.Pools

internal object CombatSidesPool {

    @JvmField
    internal val combatSidesPool = Pools.get(CombatSides::class.java)

    inline fun use(block: (CombatSides) -> Unit) {
        val combatSides = combatSidesPool.obtain()
        try {
            block(combatSides)
        } finally {
            combatSidesPool.free(combatSides)
        }
    }
}