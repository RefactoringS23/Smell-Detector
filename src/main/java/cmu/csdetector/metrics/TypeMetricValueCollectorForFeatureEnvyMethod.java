package cmu.csdetector.metrics;

import cmu.csdetector.metrics.calculators.type.SimilarityCoefficientCalculator;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TypeMetricValueCollectorForFeatureEnvyMethod extends MetricValueCollector {

    public TypeMetricValueCollectorForFeatureEnvyMethod(MethodDeclaration featureEnvyMethod) {
        addCalculator(new SimilarityCoefficientCalculator(featureEnvyMethod));
    }
}