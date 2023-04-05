package cmu.csdetector.smells;

import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.GodClass;
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
 * This class tests BlobClassSample and SuperDummy classes for God Class smell.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GodClassTest {

    private List<Type> types;
    private Type blobType;
    private Type superDummyType;

    @BeforeAll
    public void setUp() throws IOException {

        File dir = new File("src/test/java/cmu/csdetector/dummy/smells");
        this.types = TypeLoader.loadAllFromDir(dir);

        GenericCollector genericCollector = new GenericCollector();

        for (Type type : this.types) {
            if (type.getBinding().getName().equals("BlobClassSample")) {
                this.blobType = type;
            } else if (type.getBinding().getName().equals("SuperDummy")) {
                this.superDummyType = type;
            }
            genericCollector.collectTypeMetricValues(type);
        }
    }

    /**
     * BlobClassSample has very high lines of code and low value for TCC. Hence, God class smell is
     * detected.
     */
    @Test
    public void testBlobClassSample() {

        GodClass godClass = new GodClass();
        List<Smell> smells = godClass.detect(this.blobType);

        Assertions.assertFalse(smells.isEmpty());
        Assertions.assertEquals(smells.get(0).getName(), SmellName.GodClass);
        Assertions.assertEquals(smells.get(0).getReason(), "TCC = 0.0 CLOC = 851.0");
    }

    /**
     * SuperDummy does not have heavy implementation, lines of code are less. Hence, it does
     * not have the god class smell.
     */
    @Test
    public void testSuperDummy() {

        GodClass godClass = new GodClass();
        List<Smell> smells = godClass.detect(this.superDummyType);

        Assertions.assertTrue(smells.isEmpty());
    }
}