package cmu.csdetector.jqual.refactoringOperations;

import cmu.csdetector.ast.visitors.BlockLineNumberVisitor;
import cmu.csdetector.ast.visitors.IfBlockVisitor;
import cmu.csdetector.ast.visitors.StatementObjectsVisitor;
import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.heuristics.ClusterManager;
import cmu.csdetector.jqual.recommendation.ExtractMethodRecommendation;
import cmu.csdetector.jqual.recommendation.Recommendation;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.Method;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ExtractMethodRefactoring extends RefactoringOperation {



    private Cluster bestCluster;

    public ExtractMethodRefactoring(Type parentClass, Method candidateMethod) {
        super(parentClass, candidateMethod);
    }

    public Cluster getBestCluster() {
        return this.bestCluster;
    }

    
    private SortedMap<Integer, HashSet<String>> getHashMapForClustering() throws ClassNotFoundException {
        MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getHeuristicMap();
    }

    private Map<String, ASTNode> getStringASTNodeMap() throws ClassNotFoundException {
        MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        targetMethod.accept(ifBlockVisitor);
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodeNameMap();
    }

    private  Map<ASTNode, Integer>  extractVariableDeclarations() throws ClassNotFoundException {
        MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor();
        targetMethod.accept(statementObjectsVisitor);
        return statementObjectsVisitor.getNodesDeclared();
    }

    private Set<Cluster> getGrabManifestsBlock() throws ClassNotFoundException {
        MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
        BlockLineNumberVisitor blockLineNumberVisitor = new BlockLineNumberVisitor();
        targetMethod.accept(blockLineNumberVisitor);
        return blockLineNumberVisitor.getLineMap();
    }

    @Override
    public void runOperation() {
        SortedMap<Integer, HashSet<String>> table = null;
        Map<String, ASTNode> stringASTNodeMap = null;
        Map<ASTNode, Integer> declaredVars = null;
        ClusterManager cm = null;
        Set<Cluster> blocks = null;
        try {
            table = getHashMapForClustering();
            stringASTNodeMap = getStringASTNodeMap();
            declaredVars = extractVariableDeclarations();
            cm = new ClusterManager(table, stringASTNodeMap, declaredVars);
            blocks = getGrabManifestsBlock();
            setBestCluster(cm.getBestCluster(blocks));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Recommendation getRecommendation() {
        return new ExtractMethodRecommendation(super.parentClass, super.candidateMethod, this.getBestCluster());
    }
    public void setBestCluster(Cluster bestCluster) {
        this.bestCluster = bestCluster;
    }
}
