package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class StatementObjectsVisitor extends ASTVisitor {
    private Map<Integer, HashSet<ASTNode>> heuristicMap;

    public StatementObjectsVisitor() {
        this.heuristicMap = new HashMap<Integer, HashSet<ASTNode>>();
    }

    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            return false;
        }

        if (binding.getKind() == IBinding.VARIABLE) {
            Integer lineNumber = getLineNumber(node);
            HashSet hashSet = this.heuristicMap.get(lineNumber - 5);
            if (hashSet == null) {
                hashSet = new HashSet();
            }
            hashSet.add(node.resolveBinding().getName());
            this.heuristicMap.put(lineNumber - 5, hashSet);
        }
        return true;
    }

    public boolean visit(MethodInvocation node) {
        IMethodBinding methodBinding = node.resolveMethodBinding();
        if (methodBinding == null) {
            return true;
        }

        ITypeBinding typeBinding = methodBinding.getDeclaringClass();
        if (typeBinding == null) { // if we were not able to bind it, just discard.
            return true;
        }

        if (typeBinding.getQualifiedName().startsWith("java")){
            return true;
        }

        Integer lineNumber = getLineNumber(node);
        HashSet hashSet = this.heuristicMap.get(lineNumber-5);
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        hashSet.add(node.getName());
        this.heuristicMap.put(lineNumber-5, hashSet);

        return true;
    }
    private Integer getLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public Map<Integer, HashSet<ASTNode>> getHeuristicMap() {
        return this.heuristicMap;
    }
}
