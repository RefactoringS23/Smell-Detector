package cmu.csdetector.heuristics;

import cmu.csdetector.ast.visitors.BlockLineNumberVisitor;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExtractAndMoveRefactorTest {

    private static List<Type> types;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/movie");

        types = TypeLoader.loadAllFromDir(dir);
        GenericCollector.collectAll(types);
    }

    @Test
    public void canIdentifyAllClusters() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        ClusterManager cm = new ClusterManager(targetMethod, type);
        Set<Cluster> blocks = getGrabManifestsBlock();
        List<Cluster> clusters = cm.getTopClusters(blocks);

        Assertions.assertEquals(clusters.get(0).getMissingVars().size(), 2);
        Assertions.assertEquals(clusters.get(0).getReturnType(), "double");
        Assertions.assertEquals(clusters.get(0).getMethodName(), "getthisAmount");
        Assertions.assertEquals(clusters.get(1).getMissingVars().size(), 2);
        Assertions.assertEquals(clusters.get(1).getReturnType(), "int");
        Assertions.assertEquals(clusters.get(1).getMethodName(), "getfrequentRenterPoints");
    }

   @Test
    public void canRankClusters() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        ClusterManager cm = new ClusterManager(targetMethod, type);
        Set<Cluster> blocks = getGrabManifestsBlock();
        List<Cluster> recommendedCluster = cm.getTopClusters(blocks);

        Assertions.assertEquals(new Integer(30), recommendedCluster.get(0).getStartLineNumber());
        Assertions.assertEquals(new Integer(45), recommendedCluster.get(0).getEndLineNumber());

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

    private Set<Cluster> getGrabManifestsBlock() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        BlockLineNumberVisitor blockLineNumberVisitor = new BlockLineNumberVisitor();
        targetMethod.accept(blockLineNumberVisitor);
        return blockLineNumberVisitor.getLineMap();
    }
}
