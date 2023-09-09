package promitech.map

inline fun IntIntArray.forEach(action: (x: Int, y: Int, value: Int) -> Unit) {
    var v: Int
    for (y in 0 until this.height) {
        for (x in 0 until this.width) {
            v = this.get(x, y)
            if (v != this.unknownValue) {
                action(x, y, v)
            }
        }
    }
}

inline fun IntIntArray.forEachCords(action: (x: Int, y: Int) -> Unit) {
    for (y in 0 until this.height) {
        for (x in 0 until this.width) {
            action(x, y)
        }
    }
}