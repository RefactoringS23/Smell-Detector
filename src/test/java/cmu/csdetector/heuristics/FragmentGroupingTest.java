package cmu.csdetector.heuristics;

import cmu.csdetector.ast.visitors.BlockLineNumberVisitor;
import cmu.csdetector.ast.visitors.IfBlockVisitor;
import cmu.csdetector.ast.visitors.StatementObjectsVisitor;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FragmentGroupingTest {

    private static List<Type> types;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/heu1");
        types = TypeLoader.loadAllFromDir(dir);
        GenericCollector.collectAll(types);
    }

    @Test
    public void canIdentifyAllClusters() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster cluster = cm.getBestCluster(blocks);
        int expectedNumberOfClusters = 4;
        Assertions.assertEquals(expectedNumberOfClusters, cm.getFilteredClusters().size());
    }

    @Test
    public void canRankClusters() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster cluster = cm.getBestCluster(blocks);
        int expectedNumberOfClusters = 4;
        Assertions.assertEquals(expectedNumberOfClusters, cm.getFilteredClusters().size());
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

    private Map<String, ASTNode> getStringASTNodeMap() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodeNameMap();
    }

    private SortedMap<Integer, HashSet<String>> getHashMapForClustering() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getHeuristicMap();
    };

    private  Map<ASTNode, Integer>  extractVariableDeclarations() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor();
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodesDeclared();
    }

    private Set<Cluster> getGrabManifestsBlock() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        BlockLineNumberVisitor blockLineNumberVisitor = new BlockLineNumberVisitor();
        targetMethod.accept(blockLineNumberVisitor);
        return blockLineNumberVisitor.getLineMap();
    }
}
