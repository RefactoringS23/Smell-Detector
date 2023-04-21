package cmu.csdetector.ast.visitors;

import cmu.csdetector.heuristics.Cluster;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.*;

public class AssignmentVisitor extends ASTVisitor {
    private HashMap<String, List<Integer>> assignmentMap;
    private Map<String, ASTNode> nodeNameMap;

    public AssignmentVisitor () {
        assignmentMap = new HashMap<String, List<Integer>>();
        nodeNameMap = new HashMap<String, ASTNode>();
    }

    @Override
    public boolean visit(Assignment node) {
        if(node.getLeftHandSide() == null || node.getLeftHandSide().resolveTypeBinding() == null){
            return true;
        }
        List<Integer> lineList = this.assignmentMap.get(node.getLeftHandSide().toString());
        if(lineList == null) {
            lineList = new ArrayList<>();
        }
        lineList.add(getStartLineNumber(node));
        this.assignmentMap.put(node.getLeftHandSide().toString(), lineList);
        this.nodeNameMap.put(node.getLeftHandSide().toString(), node);
        return true;
    }

    private Integer getStartLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public HashMap<String, List<Integer>> getLineMap() {
        return this.assignmentMap;
    }

    public Map<String, ASTNode> getNameMap() {
        return this.nodeNameMap;
    }
}
