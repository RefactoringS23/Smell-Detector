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

import java.util.*;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ExtractMethodRefactoring extends RefactoringOperation {
    private Cluster bestCluster;
    private List<Cluster> topClusters;

    private final static int TOP_COUNT = 3;
    public ExtractMethodRefactoring(Type parentClass, Method candidateMethod) {
        super(parentClass, candidateMethod);
        runOperation();
    }

    public Cluster getBestCluster() {
        return this.bestCluster;
    }


    private Set<Cluster> getGrabManifestsBlock() throws ClassNotFoundException {
        MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
        BlockLineNumberVisitor blockLineNumberVisitor = new BlockLineNumberVisitor();
        targetMethod.accept(blockLineNumberVisitor);
        return blockLineNumberVisitor.getLineMap();
    }

    @Override
    public void runOperation() {
        ClusterManager cm = null;
        Set<Cluster> blocks = null;
        try {
            MethodDeclaration targetMethod = (MethodDeclaration) super.candidateMethod.getNode();
            String parentClassName = super.parentClass.getBinding().getName();
            cm = new ClusterManager(targetMethod, parentClass);
            blocks = getGrabManifestsBlock();
            List<Cluster> recommendedClusters = cm.getTopClusters(blocks); 
            setTopClusters(recommendedClusters.subList(0,Math.min(TOP_COUNT, recommendedClusters.size())));
            setBestCluster(getTopRecommendations().get(0));
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
    public void setTopClusters(List<Cluster> topClusters) {
        this.topClusters = topClusters;
    }


    public List<Cluster> getTopRecommendations() {
        return topClusters;

    }
}
