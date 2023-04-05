package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;
import cmu.csdetector.smells.Thresholds;

import java.util.ArrayList;
import java.util.List;

/**
 * Brain Class is a class level smell that indicates classes that tend to accumulate an excessive amount
 * of intelligence.
 *
 * To detect a brain class the following metrics have been used:
 *      - Count of Brain Methods (Used BrainMethod smell detector)
 *      - Class Lines of Code (ClassLOCCalculator)
 *      - Weighted Method Count (WMCCalculator)
 *      - Tight Class Cohesion value (TCCMetricValueCalculator)
 *
 * The thresholds used for detecting the smell are:
 *      - veryHigh for CLOC, where  highThreshold = Thresholds.getVeryHighThreshold(MetricName.CLOC)
 *      - veryHigh for WMC, where highThreshold = Thresholds.getVeryHighThreshold(MetricName.WMC)
 */
public class BrainClass extends SmellDetector {
    @Override
    public List<Smell> detect(Resource resource) {
        List<Smell> smells = new ArrayList<>();

        Type type = (Type) resource;
        Double CLOC = type.getMetricValue(MetricName.CLOC);
        Double WMC = type.getMetricValue(MetricName.WMC);
        Double TCC = type.getMetricValue(MetricName.TCC);

        Double brainMethodCount = 0.0;

        /**
         * For each method in that class, check if method has Brain Method smell.
         * If yes, increase count of brain methods by 1.
         */

        for(Method method: type.getMethods()) {
            BrainMethod brainMethod = new BrainMethod();
            List<Smell> methodSmells = brainMethod.detect(method);
            if(!methodSmells.isEmpty()) {
                brainMethodCount = brainMethodCount + 1.0;
            }
        }

        Double veryHighCLOC = Thresholds.getVeryHighThreshold(MetricName.CLOC);
        Double veryHighWMC = Thresholds.getVeryHighThreshold(MetricName.WMC);

        /**
         * There are three cases for detecting Brain Class code smell.
         * Case 1: Class contains more than one Brain Method and is very large.
         * Case 2: Class contains only one Brain Method, but it is extremely large and complex
         * Case 3: Class is very complex and non-cohesive
         *
         * Based on the following formula:
         * (brainMethodCount > 1 && CLOC >= veryHighCloc || (brainMethodCount == 1 && CLOC >= 2*veryHighCloc && WMC >= 2*veryHighWMC)) &&  (WMC >= veryHighWMC && TCC < Thresholds.HALF))
         * Smells of the three categories are identified.
         *
         * These three scenarios are tested using three different classes in unit test. Each scenario is
         * simulated using new packages created under 'dummy/smellsForBrainClass'. These packages contain
         * test classes created to simulate these three scenarios for Brain Class code smell.
         */

        if(CLOC!= null && brainMethodCount > 1 && CLOC >= veryHighCLOC){
            Smell smell = createSmell(resource);
            smell.setReason("Case1: brainMethodCount = " + brainMethodCount + " CLOC = " + CLOC);

            smells.add(smell);
        } else if (CLOC!= null && WMC!=null && brainMethodCount == 1 && CLOC >= 2*veryHighCLOC && WMC >= 2*veryHighWMC) {
            Smell smell = createSmell(resource);
            smell.setReason("Case2: brainMethodCount = " + brainMethodCount + " CLOC = " + CLOC + " WMC = " + WMC);

            smells.add(smell);
        } else if (WMC!=null && TCC!=null && WMC >= veryHighWMC && TCC < Thresholds.HALF) {
            Smell smell = createSmell(resource);
            smell.setReason("Case3: WMC = " + WMC + " TCC = " + TCC);

            smells.add(smell);
        }

        return smells;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.BrainClass;
    }
}
