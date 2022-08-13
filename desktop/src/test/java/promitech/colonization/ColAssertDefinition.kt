package promitech.colonization

import net.sf.freecol.common.model.Identifiable
import net.sf.freecol.common.model.ObjectWithId

import org.assertj.core.api.Assertions.assertThat

class ColAssertDefinition {

    fun Any?.isNotNull() {
        assertThat(this).isNotNull
    }

    infix fun ObjectWithId.eqId(obj: Identifiable) {
        assert(this.equalsId(obj))
    }

    infix fun ObjectWithId.eqId(id: String) {
        assert(this.equalsId(id))
    }

    companion object {
        fun assertThat(init: ColAssertDefinition.() -> Unit) {
            val colAssertDefinition = ColAssertDefinition()
            colAssertDefinition.init()
        }
    }

}