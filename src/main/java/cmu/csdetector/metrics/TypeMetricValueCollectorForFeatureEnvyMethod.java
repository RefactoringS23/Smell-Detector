package cmu.csdetector.metrics;

import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.metrics.calculators.type.LCOM3DiffCalculator;
import cmu.csdetector.metrics.calculators.type.SimilarityCoefficientCalculator;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TypeMetricValueCollectorForFeatureEnvyMethod extends MetricValueCollector {

    public TypeMetricValueCollectorForFeatureEnvyMethod(MethodDeclaration featureEnvyMethod) {
        addCalculator(new SimilarityCoefficientCalculator(featureEnvyMethod));
        addCalculator(new LCOM3DiffCalculator(featureEnvyMethod));

    }

    public TypeMetricValueCollectorForFeatureEnvyMethod(Cluster extractedMethod) {
//        addCalculator(new SimilarityCoefficientCalculator(extractedMethod));
        addCalculator(new LCOM3DiffCalculator(extractedMethod));

    }
}
