package cmu.csdetector.metrics;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MoveMethodHeuristicTest {
    private static List<Type> types;
    private List<MethodDeclaration> featureEnvies = new ArrayList<>();

    @BeforeAll
    public static void setUp() throws IOException, ClassNotFoundException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/group");
        types = TypeLoader.loadAllFromDir(dir);
        GenericCollector.collectAll(types);
    }

    private void collectFeaturEnvies() throws ClassNotFoundException{
        Type type = getType("Customer");
        Method method = getMethod(type, "statement");
        MethodDeclaration featureEnvyMethod = (MethodDeclaration) method.getNode();
        featureEnvies.add(featureEnvyMethod);
    };

    private Type getType(String typeName) throws ClassNotFoundException {
        for (Type type : types) {
            if (type.getNodeAsTypeDeclaration().getName().toString().equals(typeName)) {
                return type;
            }
        }
        throw new ClassNotFoundException();
    }

    private Method getMethod(Type type, String methodName) throws ClassNotFoundException {
        for (Method method : type.getMethods()) {
            if (method.getBinding().getName().equals(methodName)) {
                return method;
            }
        }
        throw new ClassNotFoundException();
    }
    private String getLCOM3DiffMetricValue()
    {
        Map<String, Double> LCOM3Diffs = new HashMap<>();
        Double targetClassDiff = -1d;
        String targetClass="";

        for(Type t: types){
            Double metricValue = t.getMetricValue(MetricName.LCOM3Diff);
            LCOM3Diffs.put(t.getFullyQualifiedName(),metricValue);
            if( metricValue > targetClassDiff){
                targetClassDiff = metricValue;
                targetClass = t.getFullyQualifiedName();
            }
        };
//        System.out.print(LCOM3Diffs);
//        Collections.max(LCOM3Diffs.entrySet(), (entry1, entry2) -> entry1.getValue() - entry2.getValue()).getKey();
        return targetClass;

    };

    private String getSimillarityMetricValue()
    {
        Map<String, Double> LCOM3Diffs = new HashMap<>();
        Double targetClassDiff = -1d;
        String targetClass="";

        for(Type t: types){
            Double metricValue = t.getMetricValue(MetricName.JSC);
            LCOM3Diffs.put(t.getFullyQualifiedName(),metricValue);
            if( metricValue > targetClassDiff){
                targetClassDiff = metricValue;
                targetClass = t.getFullyQualifiedName();
            }
        };
//        System.out.print(LCOM3Diffs);
//        Collections.max(LCOM3Diffs.entrySet(), (entry1, entry2) -> entry1.getValue() - entry2.getValue()).getKey();
        return targetClass;

    }
    @Test
    public void calculateSimilarityCoefficient() throws ClassNotFoundException {
        collectFeaturEnvies();
        for(MethodDeclaration featureEnvyMethod: featureEnvies){
            GenericCollector.collectTypeMetricsForFeatureEnvyMethod(types, featureEnvyMethod);
        }
        System.out.println(getSimillarityMetricValue());
        System.out.println(getLCOM3DiffMetricValue());


    }
}