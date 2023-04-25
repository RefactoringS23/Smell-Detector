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

public class FragmentGroupingTest {

    private static List<Type> types;
    private static List<Type> moviewtypes;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/group");
        File moview = new File("src/test/java/cmu/csdetector/dummy/group");

        types = TypeLoader.loadAllFromDir(dir);
        moviewtypes = TypeLoader.loadAllFromDir(moview);
        GenericCollector.collectAll(types);
    }

    @Test
    public void canIdentifyAllClusters() throws ClassNotFoundException {
        Type type = getType("Customer");
        String parentClassName = type.getBinding().getName();
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        ClusterManager cm = new ClusterManager(targetMethod, parentClassName);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster cluster = cm.getBestCluster(blocks);
        cm.getReturnType(cluster);
        cm.getMethodName(cluster, 1);

        Assertions.assertEquals(4, cluster.getMissingVars().size());
        Assertions.assertEquals("String",cluster.getReturnType());
        Assertions.assertEquals("getresult", cluster.getMethodName());
    }

    @Test
    public void canRankClusters() throws ClassNotFoundException {
        Type type = getType("Customer");
        String parentClassName = type.getBinding().getName();
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        ClusterManager cm = new ClusterManager(targetMethod, parentClassName);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster recommendedCluster = cm.getBestCluster(blocks);

        Assertions.assertEquals(new Integer(21), recommendedCluster.getStartLineNumber());
        Assertions.assertEquals(new Integer(57), recommendedCluster.getEndLineNumber());

    }
    @Test
    public void moveMethodForExtractMethod() throws ClassNotFoundException {
        Type type = getType("Customer");
        String parentClassName = type.getBinding().getName();
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        ClusterManager cm = new ClusterManager(targetMethod, parentClassName);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster recommendedCluster = cm.getBestCluster(blocks);

        GenericCollector.collectTypeMetricsForExtractedMethod(moviewtypes, recommendedCluster);
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
