package cmu.csdetector.smells;

import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.LazyClass;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * This class tests LazyClass and NotLazyClass classes under 'dummy/smellsForLazyClas'
 * created to test for Lazy Class smell.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LazyClassTest {

    private List<Type> types;

    private Type findTypeByName(String name) {
        for (Type type : types) {
            TypeDeclaration td = (TypeDeclaration)type.getNode();
            String typeName = td.getName().toString();

            if (typeName.equals(name)) {
                return type;
            }
        }
        return null;
    }
    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/smellsForLazyClass");
        this.types = TypeLoader.loadAllFromDir(dir);
        GenericCollector genericCollector = new GenericCollector();
        for (Type type : this.types) {
            genericCollector.collectTypeMetricValues(type);
        }
    }

    /**
     * LazyClass has very low lines of code and does not have much implementation.
     * Hence, Lazy class smell is detected.
     */
    @Test
    public void testSuperDummy() {
        LazyClass lazyClass = new LazyClass();
        List<Smell> smells = lazyClass.detect(findTypeByName("LazyClass"));

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.LazyClass);
        Assertions.assertEquals(smells.get(0).getReason(), "CLOC = 1.0");
    }

    /**
     * NotLazyClass has sufficient lines of code, meaning CLOC is not less than first quartile of CLOC.
     * Hence, Lazy class smell is not detected.
     */
    @Test
    public void testBlobClassSample() {
        LazyClass lazyClass = new LazyClass();
        List<Smell> smells = lazyClass.detect(findTypeByName("NotLazyClass"));

        Assertions.assertTrue(smells.isEmpty());
    }

}
