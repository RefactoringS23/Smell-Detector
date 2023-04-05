package cmu.csdetector.smells;

import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.BrainClass;
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
 * This class tests the scenario where class contains more than one Brain Method and is very large.
 *
 * New test classes have been created under src/test/java/cmu/csdetector/dummy/smellsForBrainClass/BrainClassWithManyBrainMethods
 * to test this scenario.
 *
 * This package contains 15 classes that are small and do not have code smells.
 * It also contains the class BrainClassWithManyBrainMethods which has two brain methods and very high lines of code.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BrainClassCase1Test {
    private List<Type> types;
    private Type findTypeByName(String name) {
        for (Type type : this.types) {
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
        File dir = new File("src/test/java/cmu/csdetector/dummy/smellsForBrainClass/BrainClassWithManyBrainMethods");
        this.types = TypeLoader.loadAllFromDir(dir);
        GenericCollector genericCollector = new GenericCollector();
        for (Type type : this.types) {
            genericCollector.collectTypeAndMethodsMetricValues(type);
        }
    }

    /**
     * BrainClassWithManyBrainMethods is a brain class as CLOC value is extremely high, and it also has
     * two brain methods.
     */
    @Test
    public void testBrainClassWithManyBrainMethod() {

        BrainClass brainClass = new BrainClass();
        List<Smell> smells = brainClass.detect(findTypeByName("BrainClassWithManyBrainMethods"));

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.BrainClass);
        Assertions.assertTrue(smells.get(0).getReason().contains("Case1"));
    }

    /**
     * SmallClass1 is a simple class with no brain methods, hence it does not have Brain Class smell.
     */
    @Test
    public void testSmallClass() {
        BrainClass brainClass = new BrainClass();
        List<Smell> smells = brainClass.detect(findTypeByName("SmallClass1"));

        Assertions.assertTrue(smells.isEmpty());
    }
}
