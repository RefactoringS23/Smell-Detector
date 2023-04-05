package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature Envy is a method level smell which indicates a method that is more interested in the data of
 * other classes than the one it is actually in.
 *
 * The metrics used to detect this smell are:
 *         - Number of Internal Method Calls (InternalMethodCallsCalculator - Implemented for this smell)
 *         - Number of External Method Calls (ExternalMethodCallsCalculator - Implemented for this smell)
 *
 * Condition to be satisfied for detection of smell:
 *          externalMethodCall > internalMethodCall
 */
public class FeatureEnvy extends SmellDetector {
    @Override
    public List<Smell> detect(Resource resource) {
        Double internalMethodCall = resource.getMetricValue(MetricName.IMC);
        Double externalMethodCall = resource.getMetricValue(MetricName.EMC);

        if (externalMethodCall > internalMethodCall) {
            Smell smell = createSmell(resource);
            smell.setReason("EXTERNAL_METHOD_CALL = " + externalMethodCall + " INTERNAL_METHOD_CALL = " + internalMethodCall);

            return List.of(smell);
        }

        return new ArrayList<>();
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.FeatureEnvy;
    }
}
