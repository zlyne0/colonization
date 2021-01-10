package promitech.colonization.ai;

import org.assertj.core.api.AbstractAssert;

import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class ObjectsListScoreAssert extends AbstractAssert<ObjectsListScoreAssert, ObjectsListScore<?>> {

	public ObjectsListScoreAssert(ObjectsListScore<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ObjectsListScoreAssert assertThat(ObjectsListScore<?> list) {
		return new ObjectsListScoreAssert(list, ObjectsListScoreAssert.class);
	}

	public ObjectsListScoreAssert hasSize(int size) {
		if (actual.size() != size) {
			failWithMessage("expected size <%s> but has size <%s>", size, actual.size());
		}
		return this;
	}
	
	public ObjectsListScoreAssert hasSumScore(int sum) {
		if (actual.scoreSum() != sum) {
			failWithMessage("expected score sum <%s> but has <%s>", sum, actual.scoreSum());
		}
		return this;
	}

	/**
	 * Equals by reference
	 */
	public <T> ObjectsListScoreAssert hasScore(int index, int score, T obj) {
		ObjectScore<?> os = actual.get(index);
		if (os.getScore() != score) {
			failWithMessage("expected score at index %s <%s> but has score <%s>", index, score, os.getScore());
		}
		if (os.getObj() != obj) {
			failWithMessage("at index %s is unexpected object", index);
		}
		return this;
	}

	/**
	 * To Find object use reference equals
	 */
	public ObjectsListScoreAssert hasScore(Object obj, int score) {
		ObjectScore<?> objectScore = actual.find(obj);
		if (objectScore == null) {
			failWithMessage("can not find score for object %s", obj);
			return this;
		}
		if (objectScore.getScore() != score) {
			failWithMessage("expected score %s at for object <%s> but has score <%s>", score, obj, objectScore.getScore());
		}
		return this;
	}

	/**
	 * To find object use equals by value
	 */
	public ObjectsListScoreAssert hasScore2(int score, Object obj) {
		boolean found = false;
		for (int i = 0; i < actual.size(); i++) {
			ObjectScore<?> objectScore = actual.get(i);
			if (objectScore.getObj().equals(obj) && objectScore.getScore() == score) {
				found = true;
			}
		}
		if (!found) {
			failWithMessage("can not find score %s at for object <%s>", score, obj);
		}
		return this;
	}	

	/**
	 * Equals by value 
	 */
	public <T> ObjectsListScoreAssert hasScore2(int index, int score, T obj) {
		ObjectScore<?> os = actual.get(index);
		if (os.getScore() != score) {
			failWithMessage("expected score at index %s <%s> but has score <%s>", index, score, os.getScore());
		}
		if (!os.getObj().equals(obj)) {
			failWithMessage("at index %s is unexpected object", index);
		}
		return this;
	}
	
	
}
