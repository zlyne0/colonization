package promitech.colonization.ai.score;

import org.assertj.core.api.AbstractAssert;

import java.util.function.Predicate;

public class ScoreableObjectsListAssert extends AbstractAssert<ScoreableObjectsListAssert, ScoreableObjectsList<?>> {

	public ScoreableObjectsListAssert(ScoreableObjectsList<?> actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public static ScoreableObjectsListAssert assertThat(ScoreableObjectsList<?> list) {
		return new ScoreableObjectsListAssert(list, ScoreableObjectsListAssert.class);
	}

	public ScoreableObjectsListAssert hasSize(int size) {
		if (actual.size() != size) {
			failWithMessage("expected size <%s> but has size <%s>", size, actual.size());
		}
		return this;
	}
	
	public ScoreableObjectsListAssert hasSumScore(int sum) {
		if (actual.sumScore() != sum) {
			failWithMessage("expected score sum <%s> but has <%s>", sum, actual.sumScore());
		}
		return this;
	}

	/**
	 * equals by predicate
	 */
	public ScoreableObjectsListAssert hasScore(int index, int score, Predicate predicate) {
		ScoreableObjectsList.Scoreable os = actual.get(index);
		if (os.score() != score) {
			failWithMessage("expected score at index %s <%s> but has score <%s>", index, score, os.score());
		}
		Object objToPredicate = os;
		if (os instanceof ObjectScoreList.ObjectScore) {
			objToPredicate = ((ObjectScoreList.ObjectScore)os).getObj();
		}
		if (!predicate.test(objToPredicate)) {
			failWithMessage("at index %s is unexpected object", index);
		}
		return this;
	}

	/**
	 * To Find object use reference equals
	 */
	public ScoreableObjectsListAssert hasScore(int score, Predicate predicate) {
		ScoreableObjectsList.Scoreable objectScore = find(predicate);
		if (objectScore == null) {
			failWithMessage("can not find object for score %s", score);
			return this;
		}
		if (objectScore.score() != score) {
			failWithMessage("expected score %s for predicate but has score <%s>", score, objectScore.score());
		}
		return this;
	}

	private ScoreableObjectsList.Scoreable find(Predicate predicate) {
		for (ScoreableObjectsList.Scoreable scoreable : actual) {
			if (scoreable instanceof ObjectScoreList.ObjectScore) {
				if (predicate.test(((ObjectScoreList.ObjectScore) scoreable).getObj())) {
					return scoreable;
				}
			} else {
				if (predicate.test(scoreable)) {
					return scoreable;
				}
			}
		}
		return null;
	}
}
