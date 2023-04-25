package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.jqual.recommendation.Recommendation;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;

public abstract class RefactoringOperation {
    protected Type parentClass;
    protected Method candidateMethod;
    public RefactoringOperation( Type parentClass, Method m){
        this.parentClass = parentClass;
        this.candidateMethod = m;

    }
    public abstract Recommendation getRecommendation();
    public abstract void runOperation();
}
