package cmu.csdetector.ast.visitors;

import cmu.csdetector.heuristics.Cluster;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockLineNumberVisitor extends ASTVisitor {
    private HashSet<Cluster> lineMap;

    public BlockLineNumberVisitor() {
        this.lineMap = new HashSet<Cluster>();
    }

    public boolean visit(Block node) {
        Integer startLineNumber = getStartLineNumber(node);
        Integer endLineNumber = getEndLineNumber(node);
        Cluster cluster = new Cluster(startLineNumber, endLineNumber);
        this.lineMap.add(cluster);
        return true;
    }

    public boolean visit(IfStatement node) {
        Integer startLineNumber = getStartLineNumber(node);
        Integer endLineNumber = getEndLineNumber(node);
        Cluster cluster = new Cluster(startLineNumber, endLineNumber);
        this.lineMap.add(cluster);
        return true;
    }

    private Integer getStartLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    private Integer getEndLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        int endPosition = startPosition + astNode.getLength();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(endPosition);
    }

    public HashSet<Cluster> getLineMap() {
        return this.lineMap;
    }
}
