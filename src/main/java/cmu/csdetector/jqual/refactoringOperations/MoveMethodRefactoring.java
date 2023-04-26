package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.heuristics.Cluster;
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
    private Cluster candidateCluster;


    private Type finalTargetClass;
    public MoveMethodRefactoring(Type parentClass, Method featureEnvyMethod, List<Type> types){
        super(parentClass, featureEnvyMethod);
        allTypes = types;
        runOperation();
    };

    public MoveMethodRefactoring(Type parentClass, Cluster extractedCluster, List<Type> types){
        super(parentClass, null);
        allTypes = types;
        candidateCluster = extractedCluster;
        runOperation();
    };

    public void runOperation(){
        if(candidateMethod != null) {
            MethodDeclaration md = (MethodDeclaration) candidateMethod.getNode();
            for(Type t: allTypes){
                TypeMetricValueCollectorForFeatureEnvyMethod metricCollector = new TypeMetricValueCollectorForFeatureEnvyMethod(md);
                metricCollector.collect(t);
            }
        } else {
            for(Type t: allTypes){
                TypeMetricValueCollectorForFeatureEnvyMethod metricCollector = new TypeMetricValueCollectorForFeatureEnvyMethod(candidateCluster);
                metricCollector.collect(t);
            }
        }
    }

    public Recommendation getRecommendation() {
        Map<String, Double> metrics = new HashMap<>();
        Double maxDifference = -10d;
        Type targetClass = null;

        for(Type t: allTypes){
            Double metricValue = t.getMetricValue(MetricName.LCOM3Diff);
            String className = t.getNodeAsTypeDeclaration().getName().toString();
            metrics.put(className, metricValue);
            if( metricValue > maxDifference){
                maxDifference = metricValue;
                targetClass = t;
            }
        };
        setTargetClass(targetClass);
        String targetName = targetClass.getNodeAsTypeDeclaration().getName().toString();
        // System.out.println(targetName);
        if(candidateCluster != null){
            Recommendation r = new MoveMethodRecommendation(parentClass, candidateCluster, targetClass);
            return r;
        }
            Recommendation r = new MoveMethodRecommendation(parentClass, candidateMethod, targetClass);
        return r;

    };

    public Type getTargetClass() {
        return finalTargetClass;
    }
    public void setTargetClass(Type finalTargetClass) {
        this.finalTargetClass = finalTargetClass;
    }

}
