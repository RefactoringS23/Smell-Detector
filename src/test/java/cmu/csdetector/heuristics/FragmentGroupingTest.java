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
    private static List<Type> moviewtypes;

    private static SortedMap<Integer, HashSet<String>> table1;

    private static Map<String, String> nodeTypeMap;
    private static Set<Integer> breakSet;
    private static Set<List<Integer>> loopSet;

    @BeforeAll
    public static void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/csdetector/dummy/group");
        File moview = new File("src/test/java/cmu/csdetector/dummy/group");

        types = TypeLoader.loadAllFromDir(dir);
        moviewtypes = TypeLoader.loadAllFromDir(moview);
        table1 = new TreeMap<Integer, HashSet<String>>();
        nodeTypeMap = new HashMap<>();
        breakSet = new HashSet<>();
        loopSet = new HashSet<>();
        GenericCollector.collectAll(types);
    }

    @Test
    public void canIdentifyAllClusters() throws ClassNotFoundException {
        Type type = getType("Customer");
        String parentClassName = type.getBinding().getName();
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<String, Integer> declaredVars = extractVariableDeclarations();
        Map<String, List<Integer>> assignedVars = extractReturnMap();
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars, parentClassName);
        cm.setAssignmentVariables(assignedVars);
        cm.setNodeTypeMap(nodeTypeMap);
        cm.setBreakSet(breakSet);
        cm.setLoopSet(loopSet);
        
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
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<String, Integer> declaredVars = extractVariableDeclarations();
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars, parentClassName);
        Map<String, List<Integer>> assignedVars = extractReturnMap();
        cm.setAssignmentVariables(assignedVars);
        cm.setNodeTypeMap(nodeTypeMap);
        cm.setBreakSet(breakSet);
        cm.setLoopSet(loopSet);
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster recommendedCluster = cm.getBestCluster(blocks);

        Assertions.assertEquals(new Integer(21), recommendedCluster.getStartLineNumber());
        Assertions.assertEquals(new Integer(57), recommendedCluster.getEndLineNumber());

    }
    @Test
    public void moveMethodForExtractMethod() throws ClassNotFoundException {
        Type type = getType("Customer");
        String parentClassName = type.getBinding().getName();
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<String, Integer> declaredVars = extractVariableDeclarations();
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars, parentClassName);
        Map<String, List<Integer>> assignedVars = extractReturnMap();
        cm.setAssignmentVariables(assignedVars);
        cm.setNodeTypeMap(nodeTypeMap);
        cm.setBreakSet(breakSet);
        cm.setLoopSet(loopSet);
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

    private Map<String, ASTNode> getStringASTNodeMap() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodeNameMap();
    }

    private SortedMap<Integer, HashSet<String>> getHashMapForClustering() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        loopSet = ifBlockVisitor.getLoopStartEnd();
        breakSet = ifBlockVisitor.getBreakSet();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getHeuristicMap();
    };

    private  Map<String, Integer>  extractVariableDeclarations() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor();
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodesDeclared();
    }

    private  Map<String, List<Integer>>  extractReturnMap() throws ClassNotFoundException {
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
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
        Type type = getType("Customer");
        Method target = getMethod(type, "statement");
        MethodDeclaration targetMethod = (MethodDeclaration) target.getNode();
        BlockLineNumberVisitor blockLineNumberVisitor = new BlockLineNumberVisitor();
        targetMethod.accept(blockLineNumberVisitor);
        return blockLineNumberVisitor.getLineMap();
    }
}
