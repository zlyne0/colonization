package net.sf.freecol.common.model

inline fun <T: Identifiable> MapIdEntities<T>.getByIdOrCreate(id: String, supplier: () -> T): T {
    var entity = this.getByIdOrNull(id)
    if (entity == null) {
        entity = supplier.invoke()
        this.add(entity)
    }
    return entity
}

inline fun <T: Identifiable> MapIdEntities<T>.removeByPredicate(predicate: (T) -> Boolean) {
    var idsToRemove: ArrayList<String>? = null
    for (entity in this) {
        if (predicate.invoke(entity)) {
            if (idsToRemove == null) {
                idsToRemove = ArrayList(this.size())
            }
            idsToRemove.add(entity.id)
        }
    }
    if (idsToRemove != null) {
        for (id in idsToRemove) {
            this.removeId(id)
        }
    }
}