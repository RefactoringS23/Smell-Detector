package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.jqual.recommendation.MoveMethodRecommendation;
import cmu.csdetector.jqual.recommendation.Recommendation;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.TypeMetricValueCollectorForFeatureEnvyMethod;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveMethodRefactoring extends RefactoringOperation {

    private List<Type> allTypes;
    public MoveMethodRefactoring(Type parentClass, Method featureEnvyMethod, List<Type> types){
        super(parentClass, featureEnvyMethod);
        allTypes = types;
    }
    public void runOperation(){
        MethodDeclaration md = (MethodDeclaration) candidateMethod.getNode();
        TypeMetricValueCollectorForFeatureEnvyMethod metricCollector = new  TypeMetricValueCollectorForFeatureEnvyMethod(md);
    }

    public Recommendation getRecommendation() {
        Map<String, Double> metrics = new HashMap<>();
        Double maxDifference = -10d;
        Type targetClass = new Type();

        for(Type t: allTypes){
            Double metricValue = t.getMetricValue(MetricName.LCOM3Diff);
            String className = t.getNodeAsTypeDeclaration().getName().toString();
            metrics.put(className, metricValue);
            if( metricValue > maxDifference){
                maxDifference = metricValue;
                targetClass = t;
            }
        };

        String targetName = targetClass.getNodeAsTypeDeclaration().getName().toString();
        System.out.println(targetName);
        Recommendation r = new MoveMethodRecommendation(parentClass, candidateMethod, targetClass);
        return r;

    };

}
