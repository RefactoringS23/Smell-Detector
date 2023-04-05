package cmu.csdetector.smells;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class tests all methods under FeatureEnvyMethod for Feature Envy smell.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FeatureEnvyTest {

    private List<Method> methods;

    private Method findMethodByName(String name) {
        for (Method method : this.methods) {
            if (method.getBinding().getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
    @BeforeAll
    public void setUp() throws IOException {

        File dir = new File("src/test/java/cmu/csdetector/dummy/smells");
        List<Type> types = TypeLoader.loadAllFromDir(dir);

        Type featureEnvyClass = types.get(0);
        GenericCollector genericCollector = new GenericCollector();

        for (Type type : types) {
            if (type.getBinding().getName().equals("FeatureEnvyMethod")) {
                genericCollector.collectTypeAndMethodsMetricValues(type);
                featureEnvyClass = type;
            }
        }

        this.methods = featureEnvyClass.getMethods();
    }

    /**
     * Method FeatureEnvyMethod is the constructor, hence no smell is generated.
     */
    @Test
    public void testFeatureEnvyMethod() {
        Method method = findMethodByName("FeatureEnvyMethod");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method getfPrivate is a getter method , hence no smell is generated.
     */
    @Test
    public void testgetfPrivate() {
        Method method = findMethodByName("getfPrivate");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method localA does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testlocalA() {
        Method method = findMethodByName("localA");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method localB does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testlocalB() {
        Method method = findMethodByName("localB");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method localC does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testlocalC() {
        Method method = findMethodByName("localC");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method localD does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testlocalD() {
        Method method = findMethodByName("localD");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method superLocal does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testsuperLocal() {
        Method method = findMethodByName("superLocal");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method mostForeign calls foreign1 2 times and has no internal calls.
     * Hence, Feature Envy smell is detected.
     */
    @Test
    public void testsuperForeign() {
        Method method = findMethodByName("superForeign");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.FeatureEnvy);
        Assertions.assertEquals(smells.get(0).getReason(), "EXTERNAL_METHOD_CALL = 2.0 INTERNAL_METHOD_CALL = 0.0");
    }

    /**
     * Method mostLocal does not have many external calls , hence no smell is generated.
     */
    @Test
    public void testmostLocal() {
        Method method = findMethodByName("mostLocal");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method mostForeign calls SuperDummy 4 times and internal calls are only 2.
     * Hence, Feature Envy smell is detected.
     */
    @Test
    public void testmostForeign() {
        Method method = findMethodByName("mostForeign");

        FeatureEnvy featureEnvy = new FeatureEnvy();
        List<Smell> smells = featureEnvy.detect(method);

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.FeatureEnvy);
        Assertions.assertEquals(smells.get(0).getReason(), "EXTERNAL_METHOD_CALL = 4.0 INTERNAL_METHOD_CALL = 2.0");
    }
}
