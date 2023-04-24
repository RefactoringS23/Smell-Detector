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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveMethodHeuristicTest {
    private static List<Type> types;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/group");
        types = TypeLoader.loadAllFromDir(dir);
        GenericCollector.collectAll(types);
    }


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
    private String getTargetClassUsingLCOM3Metric()
    {
        Map<String, Double> metrics = new HashMap<>();
        Double maxDifference = -1d;
        String targetClass="";

        for(Type t: types){
            Double metricValue = t.getMetricValue(MetricName.LCOM3Diff);
            String className = t.getNodeAsTypeDeclaration().getName().toString();
            metrics.put(className, metricValue);
            if( metricValue > maxDifference){
                maxDifference = metricValue;
                targetClass = className;
            }
        };
        return targetClass;

    };

    private String getTargetClassUsingSimilarityMetric()
    {
        Map<String, Double> metrics = new HashMap<>();
        Double maxValue = -1d;
        String targetClass="";

        for(Type t: types){
            Double metricValue = t.getMetricValue(MetricName.JSC);
            String className = t.getNodeAsTypeDeclaration().getName().toString();
            metrics.put(className, metricValue);
            if( metricValue > maxValue){
                maxValue = metricValue;
                targetClass = className;
            }
        };
        return targetClass;

    }
    @Test
    public void targetClassForExtractedMethod1ShouldBeMovie() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method method = getMethod(type, "getAmount");
        MethodDeclaration featureEnvyMethod = (MethodDeclaration) method.getNode();
        GenericCollector.collectTypeMetricsForFeatureEnvyMethod(types, featureEnvyMethod);
        assertEquals("Movie", getTargetClassUsingLCOM3Metric());
//        assertEquals("Movie", getTargetClassUsingSimilarityMetric());
    }
}