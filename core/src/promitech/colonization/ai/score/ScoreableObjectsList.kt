package promitech.colonization.ai.score

import net.sf.freecol.common.util.MyInteger

open class ScoreableObjectsList<T : ScoreableObjectsList.Scoreable>(capacity: Int) : Iterable<T> {

    interface Scoreable {
        fun score() : Int
    }

    val objects = ArrayList<T>(capacity)

    private val scorableDescendingComparator = object : Comparator<Scoreable> {
        override fun compare(p0: Scoreable, p1: Scoreable): Int {
            return MyInteger.compare(p1.score(), p0.score());
        }
    }

    private val scorableAscendingComparator = object : Comparator<Scoreable> {
        override fun compare(p0: Scoreable, p1: Scoreable): Int {
            return MyInteger.compare(p0.score(), p1.score());
        }
    }

    fun add(obj: T) {
        objects.add(obj)
    }

    fun sortDescending() {
        objects.sortWith(scorableDescendingComparator)
    }

    fun sortAscending() {
        objects.sortWith(scorableAscendingComparator)
    }

    override fun iterator(): Iterator<T> {
        return objects.iterator()
    }

    fun size() : Int = objects.size
    fun isEmpty() : Boolean = objects.size == 0
    fun firstObj() : T = objects.get(0)
    fun get(index: Int): T = objects.get(index)
    fun lastObj() : T = objects.get(objects.lastIndex)
    fun theBestScore(): T = objects.get(0)

    fun clear() {
        this.objects.clear()
    }

    fun removeLast() {
        objects.removeAt(objects.lastIndex)
    }

    fun sumScore() : Int {
        var sum = 0
        for (obj in objects) {
            sum += obj.score()
        }
        return sum
    }

    fun prettyPrint() {
        println("size = " + this.objects.size + ", sumScore = " + sumScore())
        for (obj in this.objects) {
            println(obj)
        }
    }
}
