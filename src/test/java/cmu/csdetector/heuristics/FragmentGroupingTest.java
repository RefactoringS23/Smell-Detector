package cmu.csdetector.heuristics;

import cmu.csdetector.ast.visitors.AssignmentVisitor;
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
    private static SortedMap<Integer, HashSet<String>> table1;

    private static Map<String, String> nodeTypeMap;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/heu1");
        types = TypeLoader.loadAllFromDir(dir);
        table1 = new TreeMap<Integer, HashSet<String>>();
        nodeTypeMap = new HashMap<>();
        GenericCollector.collectAll(types);
    }

    @Test
    public void canIdentifyAllClusters() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        Map<String, List<Integer>> assignedVars = extractReturnMap();
        Cluster.setAssignedNodes(assignedVars);
        ClusterManager cm = new ClusterManager(table, declaredVars);
        Set<Cluster> blocks = getGrabManifestsBlock();
        cm.createClusters(blocks);
        int expectedNumberOfClusters = 4;

        for (Cluster cluster: blocks) {
            cluster.setAssignedNodes(assignedVars);
            String returnValue = cluster.getReturnValue(assignedVars,table1);
            //System.out.println(returnValue);
            //cluster.getReturnType(nodeTypeMap, returnValue);
            System.out.println(cluster.getReturnType(nodeTypeMap, returnValue));
            Random rand = new Random();
            //cluster.getMethodName(returnValue, rand.nextInt());
            System.out.println(cluster.getMethodName(returnValue, rand.nextInt()));
        }

        Assertions.assertEquals(expectedNumberOfClusters, cm.getFilteredClusters().size());
    }

    @Test
    public void canRankClusters() throws ClassNotFoundException {
        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations();
        ClusterManager cm = new ClusterManager(table, declaredVars);
        Set<Cluster> blocks = getGrabManifestsBlock();
        cm.createClusters(blocks);
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
        table1 = tableWithName;
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

    private  Map<String, List<Integer>>  extractReturnMap() throws ClassNotFoundException {
        Type type = getType("testFile");
        Method target = getMethod(type, "grabManifests");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        IfBlockVisitor visitor =  new IfBlockVisitor();
        targetMethod.accept(visitor);
        AssignmentVisitor assignmentVisitor = new AssignmentVisitor(visitor.getSpecialLine());
        targetMethod.accept(assignmentVisitor);

        Map<String, List<Integer>> assignmentNameMap = assignmentVisitor.getLineMap();
        Map<String, String> nameMap = assignmentVisitor.getNodeTypeMap();
        nodeTypeMap = nameMap;

        /**
        System.out.println("1234567890");
        System.out.println(visitor.getSpecialLine());
        System.out.println(assignmentNameMap);
        System.out.println("12345678901234"); **/
        return assignmentNameMap;
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
