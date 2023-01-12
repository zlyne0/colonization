package net.sf.freecol.common.util

inline fun <T : Any> T?.whenNotNull(action: (T) -> Unit) {
    if (this != null) {
        action.invoke(this)
    }
}