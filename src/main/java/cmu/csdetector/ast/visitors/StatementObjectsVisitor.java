package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class StatementObjectsVisitor extends ASTVisitor {
    private Map<Integer, HashSet<ASTNode>> heuristicMap;
    private Map<Integer, ArrayList<Integer>> ifMap;

    public StatementObjectsVisitor() {
        this.heuristicMap = new HashMap<Integer, HashSet<ASTNode>>();
        this.ifMap = new HashMap<Integer, ArrayList<Integer>>();
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
            ArrayList elseArray = this.ifMap.get(lineNumber-5);
            if (elseArray != null)
            {
                for (int i=0; i< elseArray.size(); i++)
                {
                    HashSet hashSet1 = this.heuristicMap.get(elseArray.get(i));
                    if (hashSet1 == null) {
                        hashSet1 = new HashSet();
                    }
                    hashSet1.add(node.resolveBinding().getName());
                    this.heuristicMap.put((Integer) elseArray.get(i), hashSet1);
                }
            }
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

        Integer lineNumber = getLineNumber(node);
        HashSet hashSet = this.heuristicMap.get(lineNumber-5);
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        hashSet.add(node.getName());
        this.heuristicMap.put(lineNumber-5, hashSet);
        ArrayList elseArray = this.ifMap.get(lineNumber-5);
        if (elseArray != null)
        {
            for (int i=0; i< elseArray.size(); i++)
            {
                HashSet hashSet1 = this.heuristicMap.get(elseArray.get(i));
                if (hashSet1 == null) {
                    hashSet1 = new HashSet();
                }
                hashSet1.add(node.getName());
                this.heuristicMap.put((Integer) elseArray.get(i), hashSet1);
            }
        }
        return true;
    }

    public boolean visit(IfStatement node) {
        if (node.getElseStatement() != null) {
            Integer lineNumber = getLineNumber(node);
            ArrayList arrayList = this.ifMap.get(lineNumber-5);
            if (arrayList == null) {
                arrayList = new ArrayList();
            }
            arrayList.add(getLineNumber(node.getElseStatement())-5);
            this.ifMap.put(lineNumber-5, arrayList);
        }
        System.out.println(node.getThenStatement());
        System.out.println(node.getThenStatement().toString().contains(" if "));
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
