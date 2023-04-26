package cmu.csdetector.metrics;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimilarityCoefficientTest {
    private static List<Type> types;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/movie");
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
//
//    private boolean isSmellyMethod(SmellName smellName, String className, String methodName) throws ClassNotFoundException {
//        Type type = getType(className);
//        return getMethod(type, methodName).hasSmell(smellName);
//    }
//
//    @Test
//    public void statementMethodInCustomerClassHasFeatureEnvy() throws ClassNotFoundException {
//        assertTrue(isSmellyMethod(SmellName.FeatureEnvy, "Customer", "statement"));
//    }

    @Test
    public void calculateSimilarityCoefficient() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method method = getMethod(type, "getAmount");
        MethodDeclaration featureEnvyMethod = (MethodDeclaration) method.getNode();
        GenericCollector.collectTypeMetricsForFeatureEnvyMethod(types, featureEnvyMethod);
        // System.out.println(featureEnvyMethod);
    }
}
