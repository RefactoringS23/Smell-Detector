package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.ast.visitors.FieldAccessCollector;
import cmu.csdetector.ast.visitors.MethodCollector;
import cmu.csdetector.ast.visitors.MethodInvocationVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
* entities = method calls and used attributes
 */

public class SimilarityCoefficientCalculator extends MetricValueCalculator {

    private MethodDeclaration featureEnvyMethod;
    public SimilarityCoefficientCalculator(MethodDeclaration type) {
        featureEnvyMethod = type;
    }

    @Override
    protected Double computeValue(ASTNode target) {
        List<MethodDeclaration> methods = getMethods(target);
        boolean featureEnvyClass = false;
        double similarityScore = 0.0;
        double averageSimilarityScore;
        Set<String> fEnvyMethodEntities = getEntitiesAccessed(featureEnvyMethod);
        for (MethodDeclaration md : methods) {
            if (!md.getName().equals(featureEnvyMethod.getName())) {
                similarityScore = similarityScore + getSimilarity(fEnvyMethodEntities, md);
            } else {
                featureEnvyClass = true;
            }
        }
        if (featureEnvyClass) {
            averageSimilarityScore = similarityScore/(methods.size()-1);
        } else {
            averageSimilarityScore = similarityScore/methods.size();
        }
        return averageSimilarityScore;
    }

    private Double getSimilarity(Set<String> fEnvyMethodEntities, MethodDeclaration md) {
        Set<String> mdMethodEntities = getEntitiesAccessed(md);
        return getIntersection(fEnvyMethodEntities, mdMethodEntities)/
                getUnion(fEnvyMethodEntities, mdMethodEntities);
    }

    private Set<String> getEntitiesAccessed(ASTNode declaration) {
        Set<String> entitiesAccessed = new HashSet<>();
        FieldAccessCollector fieldAccessCollector = new FieldAccessCollector();
        declaration.accept(fieldAccessCollector);

        for (IBinding iBinding : fieldAccessCollector.getNodesCollected()) {
            IVariableBinding variableBinding = (IVariableBinding) iBinding;
            if (variableBinding.getDeclaringClass() != null) { // maybe make a visitor for this
                entitiesAccessed.add(variableBinding.getDeclaringClass().getName());
            }
        }

        MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
        declaration.accept(methodInvocationVisitor);
        Set<IMethodBinding> methodBindings = methodInvocationVisitor.getCalls();
        for (IMethodBinding mBinding : methodBindings) {
            entitiesAccessed.add(mBinding.getDeclaringClass().getName());
        }
        return entitiesAccessed;
    }

    private Double getUnion(Set<String> firstSet, Set<String> secondSet) {
        return firstSet.size() + secondSet.size() - getIntersection(firstSet, secondSet);
    }

    private Double getIntersection(Set<String> firstSet, Set<String> secondSet) {
        Double intersection = 0.0;
        for (String attribute : firstSet) {
            if (secondSet.contains(attribute)) {
                intersection += 1.0;
            }
        }
        return intersection;
    }

    private List<MethodDeclaration> getMethods(ASTNode target) {
        MethodCollector methodCollector = new MethodCollector();
        target.accept(methodCollector);
        TypeDeclaration td = (TypeDeclaration) target;
        List<MethodDeclaration> declaredMethods = new ArrayList<>();
        for (MethodDeclaration md : methodCollector.getNodesCollected()) {
            if (!md.getName().toString().equals(td.getName().toString())) {
                declaredMethods.add(md);
            }
        }
        return declaredMethods;
    }


    public MetricName getMetricName() {
        return MetricName.JSC;
    }


}
