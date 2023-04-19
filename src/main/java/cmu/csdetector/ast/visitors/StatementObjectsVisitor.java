package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class StatementObjectsVisitor extends ASTVisitor {
    private Map<Integer, HashSet<ASTNode>> heuristicMap;
    private Map<Integer, ArrayList<Integer>> ifMap;

    public StatementObjectsVisitor(Map<Integer, ArrayList<Integer>> ifMap) {
        this.heuristicMap = new HashMap<Integer, HashSet<ASTNode>>();
        this.ifMap = ifMap;
    }

    @Override
    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            return false;
        }

        if (binding.getKind() == IBinding.VARIABLE) {
            Integer lineNumber = getStartLineNumber(node);
            addNodeToMap(node, lineNumber);

            ArrayList elseArray = this.ifMap.get(lineNumber);
            if (elseArray != null) {
                for (int i = 0; i < elseArray.size(); i++) {
                    addNodeToMap(node,(Integer) elseArray.get(i));
                }
            }
        }
        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding methodBinding = node.resolveMethodBinding();
        if (methodBinding == null) {
            return true;
        }

        ITypeBinding typeBinding = methodBinding.getDeclaringClass();
        if (typeBinding == null) { // if we were not able to bind it, just discard.
            return true;
        }

        Integer lineNumber = getStartLineNumber(node);
        addNodeToMap(node, lineNumber);

        ArrayList elseArray = this.ifMap.get(lineNumber);
        if (elseArray != null)
        {
            for (int i=0; i< elseArray.size(); i++)
            {
                addNodeToMap(node,(Integer) elseArray.get(i));
            }
        }
        return true;
    }

    private void addNodeToMap (ASTNode node, Integer lineNumber) {
        HashSet hashSet = this.heuristicMap.get(lineNumber);
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        hashSet.add(node);
        this.heuristicMap.put(lineNumber, hashSet);
    }

    private Integer getStartLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public Map<Integer, HashSet<ASTNode>> getHeuristicMap() {
        return this.heuristicMap;
    }
}
