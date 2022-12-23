package promitech.colonization.ai.score

class ObjectScoreList<OT>(capacity: Int) : ScoreableObjectsList<ObjectScoreList.ObjectScore<OT>>(capacity) {

    class ObjectScore<O>(private var score : Int, val obj : O) : Scoreable {
        fun updateScore(s: Int) {
            score = s
        }

        override fun score(): Int {
            return score
        }

        override fun toString(): String {
            return obj.toString() + ", score: " + score
        }
    }

    fun add(obj : OT, score : Int) : ObjectScoreList<OT> {
        super.add(ObjectScore(score, obj))
        return this
    }

}