package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.jqual.recommendation.Recommendation;

public abstract class RefactoringOperation {
    public RefactoringOperation(){

    }
    public abstract Recommendation getRecommendation();
}
