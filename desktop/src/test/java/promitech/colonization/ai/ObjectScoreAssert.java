package promitech.colonization.ai;

import org.assertj.core.api.AbstractAssert;

import promitech.colonization.ai.ObjectsListScore.ObjectScore;

public class ObjectScoreAssert extends AbstractAssert<ObjectScoreAssert, ObjectScore<?>> {

	public ObjectScoreAssert(ObjectScore<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ObjectScoreAssert assertThat(ObjectScore<?> colony) {
		return new ObjectScoreAssert(colony, ObjectScoreAssert.class);
	}

	public ObjectScoreAssert hasScore(Object obj, int score) {
		if (!actual.getObj().equals(obj)) {
			failWithMessage("expected obj equals <%s> but it's <%s>", obj, actual.getObj());
		}
		if (actual.getScore() != score) {
			failWithMessage("expected score equals <%s> but is's <%s>", score, actual.getScore());
		}
		return this;
	}
	
}
