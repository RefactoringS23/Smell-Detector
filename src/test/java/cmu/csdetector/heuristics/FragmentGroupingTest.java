package cmu.csdetector.heuristics;

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

//    @Test
//    public void canIdentifyAtLeastOneClusterInLongSnippet() throws ClassNotFoundException {
//        Map<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
//
//        Set<Cluster> clusters = Cluster.makeClusters((SortedMap<Integer, HashSet<ASTNode>>) table);
//        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);
//        Assertions.assertTrue(allClusters.size() >= 1);
//    }

    private Set<Cluster> createSmallDummyBlocks() {
        Set<Cluster> blocks = new HashSet<>();
        blocks.add(new Cluster(1, 4));
        blocks.add(new Cluster(1, 5));
        blocks.add(new Cluster(3, 4));
        return blocks;
    }

//    @Test
//    public void invalidClustersAreFilteredOutFromSmallDummy() throws ClassNotFoundException {
//        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
//
//        Set<Cluster> clusters = Cluster.makeClusters(table);
//        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);
//        Set<Cluster> blocks = createSmallDummyBlocks();
//
//        Set<Cluster> filteredClusters = Cluster.filterValidClusters(allClusters, blocks);
//        System.out.println(filteredClusters);
//    }
//
//    @Test
//    public void invalidClustersAreFilteredOutFromPaperDummy() throws ClassNotFoundException {
//        SortedMap<Integer, HashSet<ASTNode>> table = createHashMapForClustering();
//        Set<Cluster> clusters = Cluster.makeClusters(table);
//        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);
//        Set<Cluster> blocks = createDummyBlocks();
//
//        Set<Cluster> filteredClusters = Cluster.filterValidClusters(allClusters, blocks);
//        System.out.println(filteredClusters);
//    }

    private Set<Cluster> createDummyBlocks() {
        Set<Cluster> blocks = new HashSet<>();
        blocks.add(new Cluster(1, 34));
        blocks.add(new Cluster(3, 32));
        blocks.add(new Cluster(29, 31));
        blocks.add(new Cluster(5, 9));
        blocks.add(new Cluster(10, 28));
        blocks.add(new Cluster(24, 27));
        blocks.add(new Cluster(12, 23));
        blocks.add(new Cluster(14, 16));
        blocks.add(new Cluster(16, 22));
        blocks.add(new Cluster(18, 20));
        blocks.add(new Cluster(14, 22));
        blocks.add(new Cluster(16, 22));
        blocks.add(new Cluster(5, 7));
        blocks.add(new Cluster(7, 9));
        blocks.add(new Cluster(14, 16));
        blocks.add(new Cluster(16, 22));
        return blocks;
    }

//    private SortedMap<Integer, HashSet<ASTNode>> createSmallDummyHashMapForClustering() {
//        SortedMap<Integer, HashSet<String>> table = new TreeMap<>();
//
//
//        HashSet<String> set1 = new HashSet<>();
//        set1.add("manifests");
//        table.put(1, set1);
//
//        HashSet<String> set2 = new HashSet<>();
//        set2.add("rcs.length");
//        table.put(2, set2);
//
//        HashSet<String> set3 = new HashSet<>();
//        set3.add("manifests");
//        set3.add("i");
//        table.put(3, set3);
//
//        HashSet<String> set4 = new HashSet<>();
//        set4.add("rcs.length");
//        table.put(4, set4);
//
//        HashSet<String> set5 = new HashSet<>();
//        set5.add("i");
//        table.put(5, set5);
//
//
//        return table;
//    }

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
        SortedMap<Integer, HashSet<ASTNode>> table = new TreeMap<Integer, HashSet<ASTNode>>();
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

    /*
    // This is the Table 2 from the SEMI paper
    private SortedMap<Integer, HashSet<ASTNode>> createDummyHashMapForClustering() {
        SortedMap<Integer, HashSet<ASTNode>> table = new TreeMap<>();


        HashSet<String> set2 = new HashSet<>();
        set2.add("manifests");
        set2.add("rcs.length");
        set2.add("rcs");
        set2.add("length");
        table.put(2, set2);

        HashSet<String> set3 = new HashSet<>();
        set3.add("i");
        set3.add("rcs.length");
        set3.add("rcs");
        set3.add("length");
        table.put(3, set3);

        HashSet<String> set4 = new HashSet<>();
        set4.add("rec");
        table.put(4, set4);

        HashSet<String> set5 = new HashSet<>();
        set5.add("rcs");
        set5.add("i");
        table.put(5, set5);

        HashSet<String> set6 = new HashSet<>();
        set6.add("rec");
        set6.add("grabRes");
        set6.add("rcs");
        set6.add("i");
        table.put(6, set6);

        HashSet<String> set7 = new HashSet<>();
        set7.add("rcs");
        set7.add("i");
        table.put(7, set7);

        HashSet<String> set8 = new HashSet<>();
        set8.add("rec");
        set8.add("grabNonFileSetRes");
        set8.add("rcs");
        set8.add("i");
        table.put(8, set8);

        HashSet<String> set10 = new HashSet<>();
        set10.add("j");
        set10.add("rec.length");
        set10.add("rec");
        set10.add("length");
        table.put(10, set10);

        HashSet<String> set11 = new HashSet<>();
        set11.add("name");
        set11.add("rec.getName.replace");
        set11.add("j");
        set11.add("rec");
        set11.add("getName.replace");
        set11.add("getName");
        set11.add("replace");
        table.put(11, set11);

        HashSet<String> set12 = new HashSet<>();
        set12.add("rcs");
        set12.add("i");
        table.put(12, set12);

        HashSet<String> set13 = new HashSet<>();
        set13.add("afs");
        set13.add("rcs");
        set13.add("i");
        table.put(13, set13);

        HashSet<String> set14 = new HashSet<>();
        set14.add("rcs");
        set14.add("i");
        set14.add("equals");
        set14.add("afs.getFullpath");
        set14.add("getProj");
        set14.add("afs");
        set14.add("getFullpath");
        table.put(14, set14);

        HashSet<String> set15 = new HashSet<>();
        set15.add("name.afs.getFullpath");
        set15.add("getProj");
        set15.add("name");
        set15.add("afs.getFullpath");
        set15.add("afs");
        set15.add("getFullpath");
        table.put(15, set15);

        HashSet<String> set16 = new HashSet<>();
        set16.add("rcs");
        set16.add("i");
        set16.add("equals");
        set16.add("afs.getFullpath");
        set16.add("getProj");
        set16.add("afs.getPref");
        set16.add("afs");
        set16.add("getFullpath");
        set16.add("getPref");
        table.put(16, set16);

        HashSet<String> set17 = new HashSet<>();
        set17.add("pr");
        set17.add("afs.getPref");
        set17.add("getProj");
        set17.add("afs");
        set17.add("getPref");
        table.put(17, set17);

        HashSet<String> set18 = new HashSet<>();
        set18.add("rcs");
        set18.add("i");
        set18.add("equals");
        set18.add("afs.getFullpath");
        set18.add("getProj");
        set18.add("afs.getPref");
         set18.add("afs");
         set18.add("getFullpath");
         set18.add("getPref");
         set18.add("pr.endsWith");
         set18.add("pr");
         set18.add("endsWith");
        table.put(18, set18);

        HashSet<String> set19 = new HashSet<>();
        set19.add("pr");
        table.put(19, set19);

        HashSet<String> set21 = new HashSet<>();
        set21.add("pr");
        set21.add("name");
        table.put(21, set21);

        HashSet<String> set24 = new HashSet<>();
        set24.add("name.equalsIgnoreCase");
        set24.add("name");
        set24.add("equalsIgnoreCase");
        table.put(34, set24);

        HashSet<String> set25 = new HashSet<>();
        set25.add("manifests");
        set25.add("i");
        set25.add("rec");
        set25.add("j");
        table.put(25, set25);

        HashSet<String> set29 = new HashSet<>();
        set29.add("manifests");
        set29.add("i");
        table.put(29, set29);

        HashSet<String> set30 = new HashSet<>();
        set30.add("manifests");
        set30.add("i");
        table.put(30, set30);

        HashSet<String> set33 = new HashSet<>();
        set33.add("manifests");
        table.put(33, set33);

        return table;
    }
     */
}
