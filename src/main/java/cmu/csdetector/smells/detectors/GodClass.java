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
 * God Class is a class level smell which indicates a large class implementing different
 * responsibilities and centralizing most of the system processing.
 *
 * The metrics used to detect this smell are:
 *      - Class Lines of Code (ClassLOCCalculator)
 *      - Tight Class Cohesion value (TCCMetricValueCalculator)
 *
 * The thresholds used for detecting the smell are:
 *      - MAX_CLOC = 500;
 *      - Average for TCC, which is found using -> aggregate.getAverageValue(MetricName.TCC);
 *
 * Condition to be satisfied for detection of smell:
 *          CLOC > MAX_CLOC && TCC < TCCAvg
 */
public class GodClass extends SmellDetector {

    private final static int MAX_CLOC = 500;
    @Override
    public List<Smell> detect(Resource resource) {
        List<Smell> smells = new ArrayList<>();

        Type type = (Type) resource;
        Double TCC = type.getMetricValue(MetricName.TCC);

        AggregateMetricValues aggregate = AggregateMetricValues.getInstance();
        Double TCCAvg = aggregate.getAverageValue(MetricName.TCC);

        Double CLOC = type.getMetricValue(MetricName.CLOC);

        if(CLOC != null && TCC !=null && TCCAvg!=null && CLOC > MAX_CLOC && TCC < TCCAvg){
            Smell smell = createSmell(resource);
            smell.setReason("TCC = " + TCC + " CLOC = " + CLOC);

            smells.add(smell);
        }

        return smells;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.GodClass;
    }
}
