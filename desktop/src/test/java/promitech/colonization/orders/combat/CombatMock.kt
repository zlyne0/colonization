package promitech.colonization.orders.combat

import promitech.colonization.MockedRandomizer
import promitech.colonization.Randomizer

fun givenCombatWinProbability() {
    Randomizer.changeRandomObject(MockedRandomizer()
        .withFloatsResults(0f)
        .withIntsResults(0)
    )
}

fun givenCombatLossProbability() {
    Randomizer.changeRandomObject(MockedRandomizer()
        .withFloatsResults(1f)
        .withIntsResults(1)
    )
}
