package cmu.csdetector.refactoringTypes;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.method.CyclomaticComplexityCalculator;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.detectors.ComplexClass;

import java.util.List;

public class ExtractMethod {
    private List<Type> complexTypes;

    public void extractMethod(List<Type> types){

        for (Type type : types) {
            ComplexClass complexClass = new ComplexClass();
            List<Smell> complexClassSmells = complexClass.detect(type);
            if (complexClassSmells.size()>0) {
                complexTypes.add(type);
            }
        }

        for (Type type : complexTypes){
            for (Method method: type.getMethods()){
                Double cyclomaticComplexity = method.getMetricValue(MetricName.CC);
                if (cyclomaticComplexity > 10) {
                    System.out.println(cyclomaticComplexity);
                    // TODO: use heuristic 1
                }
            }
        }
        System.out.println("Extract Method instructions");
    }


}
