package cmu.csdetector.smells;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.BrainMethod;
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
 * This class tests all methods under BrainClassWithOneBrainMethod for Brain Method smell.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class BrainMethodTest {
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

        Type BrainClass = types.get(0);
        GenericCollector genericCollector = new GenericCollector();

        for (Type type : types) {
            if (type.getBinding().getName().equals("BrainClassWithOneBrainMethod")) {
                genericCollector.collectTypeAndMethodsMetricValues(type);
                BrainClass = type;
            }
        }

        this.methods = BrainClass.getMethods();
    }

    /**
     * Method cc2 is a simple method which is not a brain method, hence no smell is generated.
     */
    @Test
    public void testcc2() {
        Method method = findMethodByName("cc2");

        BrainMethod brainMethod = new BrainMethod();
        List<Smell> smells = brainMethod.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method cc3 is a simple method which is not a brain method, hence no smell is generated.
     */
    @Test
    public void testcc3() {
        Method method = findMethodByName("cc3");

        BrainMethod brainMethod = new BrainMethod();
        List<Smell> smells = brainMethod.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method cc4 is a simple method which is not a brain method, hence no smell is generated.
     */
    @Test
    public void testcc4() {
        Method method = findMethodByName("cc4");

        BrainMethod brainMethod = new BrainMethod();
        List<Smell> smells = brainMethod.detect(method);

        Assertions.assertTrue(smells.isEmpty());
    }

    /**
     * Method brain is a brain method as it has high no of lines of code, high cyclomatic complexity,
     * high nesting and also high number of accessed variables. Hence, a brain method smell is detected.
     */
    @Test
    public void testbrain() {
        Method method = findMethodByName("brain");

        BrainMethod brainMethod = new BrainMethod();
        List<Smell> smells = brainMethod.detect(method);

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.BrainMethod);
        Assertions.assertEquals(smells.get(0).getReason(), "MLOC = 520.0 CC = 20.0 maxNesting = 9.0 NOAV = 11.0");
    }
}
