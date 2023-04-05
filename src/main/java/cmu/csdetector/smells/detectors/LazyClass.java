package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.AggregateMetricValues;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;

import java.util.ArrayList;
import java.util.List;

/**
 * Lazy Class is a class level smell which indicates a class having a very small
 * dimension, few methods and with low complexity.
 *
 * The metrics used to detect this smell are:
 *      - Class Lines of Code (ClassLOCCalculator)
 *
 * The thresholds used for detecting the smell are:
 *      - First Quartile for CLOC, which is found using -> aggregate.getFirstQuartileValue(MetricName.CLOC)
 *
 * Condition to be satisfied for detection of smell:
 *          CLOC < CLOCFirstQuartile
 */
public class LazyClass extends SmellDetector {

    @Override
    public List<Smell> detect(Resource resource) {
        List<Smell> smells = new ArrayList<>();

        Type type = (Type) resource;
        Double CLOC = type.getMetricValue(MetricName.CLOC);

        AggregateMetricValues aggregate = AggregateMetricValues.getInstance();
        Double CLOCFirstQuartile = aggregate.getFirstQuartileValue(MetricName.CLOC);

        if(CLOC != null && CLOC < CLOCFirstQuartile){
            Smell smell = createSmell(resource);
            smell.setReason("CLOC = " + CLOC);
            smells.add(smell);
        }

        return smells;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.LazyClass;
    }
}
