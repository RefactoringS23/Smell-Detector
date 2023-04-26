package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;
import cmu.csdetector.smells.Thresholds;

import java.util.ArrayList;
import java.util.List;

/**
 * BrainMethod is a method level smell which indicates methods that tend to centralize
 * the functionality of a class.
 *
 * The metrics used to detect this smell are:
 *         - Method Lines of Code (MethodLOCCalculator)
 *         - McCabe’s Cyclomatic Number –(CyclomaticComplexityCalculator)
 *         - Maximum Nesting Level (MaxNestingCalculator)
 *         - Number of Accessed Variables (NOAVCalculator)
 *
 * The thresholds used for detecting the smell are:
 *          - highThreshold/2 for CLOC, where  highThreshold = Thresholds.getHighThreshold(MetricName.CLOC)
 *          - highThreshold for CC, where highThreshold = Thresholds.getHighThreshold(MetricName.CC)
 *
 * Condition to be satisfied for detection of smell:
 *          MLOC > halfHighCLOC && cc > highCC && maxNesting > Thresholds.SEVERAL && noav > Thresholds.MANY
 */
public class BrainMethod extends SmellDetector {
    @Override
    public List<Smell> detect(Resource resource) {
        /**
         * Collecting metrics.
         */
        Double MLOC = resource.getMetricValue(MetricName.MLOC);
        Double CC = resource.getMetricValue(MetricName.CC);
        Double maxNesting = resource.getMetricValue(MetricName.MaxNesting);
        Double noav = resource.getMetricValue(MetricName.NOAV);

        /**
         * To find threshold values, we use the method getHighThreshold in the Thresholds
         * class for the metrics CLOC and CC.
         */
        Double highCLOC = Thresholds.getHighThreshold(MetricName.CLOC);
        Double highCC = Thresholds.getHighThreshold(MetricName.CC);

        /**
         * To ensure there is no failure if some metric is not correctly calculated, a null check
         * for all metric values is used.
         */
        Boolean nullCheck = MLOC != null && CC != null && maxNesting != null && noav != null && highCLOC != null && highCC != null;

        /**
         * Use of the condition:
         * MLOC > halfHighCLOC && cc > highCC && maxNesting > Thresholds.SEVERAL && noav > Thresholds.MANY
         *
         * If the condition is satisfied, a smell is created and returned of type Brain Method.
         */
        if (nullCheck && MLOC > (highCLOC/2) && CC > highCC && maxNesting > Thresholds.SHALLOW && noav > Thresholds.MANY) {
            Smell smell = createSmell(resource);
            smell.setReason("MLOC = " + MLOC + " CC = " + CC + " maxNesting = " + maxNesting + " NOAV = " + noav);

            return List.of(smell);
        }

        return new ArrayList<>();
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.BrainMethod;
    }
}
