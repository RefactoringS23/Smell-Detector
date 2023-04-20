package cmu.csdetector.heuristics;

import cmu.csdetector.ast.visitors.BlockLineNumberVisitor;
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
    public void canIdentifyAllClustersInSmallSnippetOfCode() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        Cluster.setDeclaredNodes(declaredVars);

        Set<Cluster> clusters = Cluster.makeClusters(table);
        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);

        int expectedNumberOfClusters = 6;

        Assertions.assertEquals(expectedNumberOfClusters, allClusters.size());
    }

    // TODO: USE THIS TEST TO INTERACT WITH ACTUAL VISITORS
    @Test
    public void canRankClusters() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        Cluster.setDeclaredNodes(declaredVars);

        Set<Cluster> clusters = Cluster.makeClusters(table);
        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);

        Set<Cluster> blocks = getGrabManifestsBlock();
        Set<Cluster> filteredClusters = Cluster.filterValidClusters(allClusters, blocks);
        Cluster.calculateLcomOfClusters(filteredClusters, table);
        ClusterRanking.rankClusters(filteredClusters);

        int expectedNumberOfClusters = 6;

        Assertions.assertEquals(expectedNumberOfClusters, allClusters.size());
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

    private SortedMap<Integer, HashSet<ASTNode>> createHashMapForClustering() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor();
        targetMethod.accept(statementObjectsVisitor);

        Map<String, ASTNode> nameMap = statementObjectsVisitor.getNodeNameMap();
        SortedMap<Integer, HashSet<String>> tableWithName = statementObjectsVisitor.getHeuristicMap();
        SortedMap<Integer, HashSet<ASTNode>> table = new TreeMap<>();
        for (int ind : tableWithName.keySet()) {
            HashSet<String> nodeNames = tableWithName.get(ind);
            HashSet<ASTNode> nodes = new HashSet<ASTNode>();
            for (String name: nodeNames) {
                nodes.add(nameMap.get(name));
            }
            table.put(ind, nodes);
        }
        return table;
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
