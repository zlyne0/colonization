package promitech.colonization.ai.score

import promitech.colonization.ai.score.ScoreableObjectsList

class ObjectScoreList<OT>(capacity: Int) : ScoreableObjectsList<ObjectScoreList.ObjectScore<OT>>(capacity) {

    public class ObjectScore<O>(private var score : Int, val obj : O) : Scoreable {
        fun updateScore(s: Int) {
            score = s
        }

        override fun score(): Int {
            return score
        }
    }

    fun add(obj : OT, score : Int) : ObjectScoreList<OT> {
        super.add(ObjectScore(score, obj))
        return this
    }

}