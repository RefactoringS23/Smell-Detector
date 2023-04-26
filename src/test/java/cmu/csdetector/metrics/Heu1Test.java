package cmu.csdetector.metrics;

import cmu.csdetector.ast.visitors.TypeDeclarationCollector;
import cmu.csdetector.metrics.calculators.method.Heu1Calculator;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Heu1Test {
    private final double DELTA = 0.000001d;
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

    private Double getHeu1Value(String className) throws NullPointerException{
        Type typeClass = findTypeByName(className);

        TypeDeclarationCollector visitor = new TypeDeclarationCollector();
        assert typeClass != null;

        typeClass.getNode().accept(visitor);

        TypeDeclaration type = visitor.getNodesCollected().get(0);
        Heu1Calculator calculator = new Heu1Calculator();

        for(MethodDeclaration methodDeclaration: type.getMethods()){
            // System.out.println(methodDeclaration.getName());
            Double i = calculator.getValue(methodDeclaration);
        }
        return 0.2;
    }

    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/heu1");
        this.types = TypeLoader.loadAllFromDir(dir);

    }

    @Test
    public void testLCOM2EmptyClass() {
        Double obtained = getHeu1Value("testFile");

        Assertions.assertEquals(0, obtained, DELTA);

    }

}
