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
import java.util.ArrayList;
import java.util.List;

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


    @Test
    public void calculateSimilarityCoefficient() throws ClassNotFoundException {
        collectFeaturEnvies();
        for(MethodDeclaration featureEnvyMethod: featureEnvies){
            GenericCollector.collectTypeMetricsForFeatureEnvyMethod(types, featureEnvyMethod);
            System.out.println("featureEnvyMethod");
        }

    }
}