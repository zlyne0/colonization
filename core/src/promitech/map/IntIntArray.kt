package promitech.map

inline fun IntIntArray.forEach(action: (x: Int, y: Int, value: Int) -> Unit) {
    for (y in 0..this.height - 1) {
        for (x in 0..this.width - 1) {
            action(x, y, this.get(x, y))
        }
    }
}

