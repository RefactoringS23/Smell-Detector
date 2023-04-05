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
 * This class tests the scenario where class contains only one Brain Method,
 * but it is extremely large and complex.
 *
 * New test classes have been created under src/test/java/cmu/csdetector/dummy/smellsForBrainClass/BrainClassWithOneBrainMethod
 * to test this scenario.
 *
 * This package contains 15 classes that are small and do not have code smells.
 * It also contains the class BrainClassWithOneBrainMethod which has one brain methods,
 * very high lines of code and very high wmc value.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BrainClassCase2Test {

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
        File dir = new File("src/test/java/cmu/csdetector/dummy/smellsForBrainClass/BrainClassWithOneBrainMethod");
        types = TypeLoader.loadAllFromDir(dir);
        GenericCollector genericCollector = new GenericCollector();
        for (Type type : types) {
            genericCollector.collectTypeAndMethodsMetricValues(type);
        }
    }

    /**
     * BrainClassWithOneBrainMethod is a brain class as its CLOC value is extremely high, and it has
     * one brain method and wmc value is also very high.
     */
    @Test
    public void testBrainClassWithOneBrainMethod(){

        BrainClass brainClass = new BrainClass();
        List<Smell> smells = brainClass.detect(findTypeByName("BrainClassWithOneBrainMethod"));
        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.BrainClass);
        Assertions.assertTrue(smells.get(0).getReason().contains("Case2"));
    }

    /**
     * SmallClass1 is a simple class with no brain methods, hence it does not have Brain Class smell.
     */
    @Test
    public void testSmallClass(){

        BrainClass brainClass = new BrainClass();
        List<Smell> smells = brainClass.detect(findTypeByName("SmallClass1"));

        Assertions.assertTrue(smells.isEmpty());
    }
}
