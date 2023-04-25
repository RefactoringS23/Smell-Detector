package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.jqual.recommendation.MoveMethodRecommendation;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MoveMethodRefactoring extends RefactoringOperation {

    public MoveMethodRefactoring(Type parentClass, Method featureEnvyMethod){
        super(parentClass, featureEnvyMethod);
    }
    public void runOperation(){
        MethodDeclaration md = (MethodDeclaration) candidateMethod.getNode();
//        GenericCollector.collectTypeMetricsForFeatureEnvyMethod(parentClass, candid);
    }

}
