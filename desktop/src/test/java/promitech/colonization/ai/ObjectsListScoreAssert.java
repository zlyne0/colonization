package promitech.colonization.ai;

import org.assertj.core.api.AbstractAssert;

public class ObjectsListScoreAssert extends AbstractAssert<ObjectsListScoreAssert, ObjectsListScore> {

	public ObjectsListScoreAssert(ObjectsListScore<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ObjectsListScoreAssert assertThat(ObjectsListScore<?> colony) {
		return new ObjectsListScoreAssert(colony, ObjectsListScoreAssert.class);
	}

	public ObjectsListScoreAssert hasSize(int size) {
		if (actual.size() != size) {
			failWithMessage("expected size <%s> but has size <%s>", size, actual.size());
		}
		return this;
	}
	
	public ObjectsListScoreAssert hasScore(int index, int score, Object obj) {
		if (actual.objScore(index).score != score) {
			failWithMessage("expected score at index %s <%s> but has score <%s>", index, score, actual.objScore(index).score);
		}
		if (!actual.objScore(index).obj.equals(obj)) {
			failWithMessage("at index %s is unexpected object", index);
		}
		return this;
	}
}
