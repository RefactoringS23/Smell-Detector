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

    @Override
    public boolean visit(SimpleName node) {
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            return false;
        }

        if (binding.getKind() == IBinding.VARIABLE) {
            Integer lineNumber = getStartLineNumber(node);
            HashSet hashSet = this.heuristicMap.get(lineNumber - 5);
            if (hashSet == null) {
                hashSet = new HashSet();
            }
            hashSet.add(node);
            this.heuristicMap.put(lineNumber - 5, hashSet);
            ArrayList elseArray = this.ifMap.get(lineNumber - 5);
            if (elseArray != null) {
                for (int i = 0; i < elseArray.size(); i++) {
                    HashSet hashSet1 = this.heuristicMap.get(elseArray.get(i));
                    if (hashSet1 == null) {
                        hashSet1 = new HashSet();
                    }
                    hashSet1.add(node);
                    this.heuristicMap.put((Integer) elseArray.get(i), hashSet1);
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
        HashSet hashSet = this.heuristicMap.get(lineNumber-5);
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        hashSet.add(node);
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

                hashSet1.add(node);
                this.heuristicMap.put((Integer) elseArray.get(i), hashSet1);
            }
        }
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        if (node.getElseStatement() != null) {
            Integer lineNumber = getStartLineNumber(node);
            ArrayList arrayList = this.ifMap.get(lineNumber-5);
            if (arrayList == null) {
                arrayList = new ArrayList();
            }
            arrayList.add(getStartLineNumber(node.getElseStatement())-5);
            this.ifMap.put(lineNumber-5, arrayList);
        }
        System.out.println("xyz");
        System.out.println(node);
        System.out.println(getStartLineNumber(node));
        System.out.println(getEndLineNumber(node));
        //System.out.println(node.getThenStatement());
        /** System.out.println("aba");
        Block block =  (Block) node.getParent();
        System.out.println(block);
        System.out.println("cc");
        System.out.println(block.statements());
        System.out.println(node.getParent().getNodeType());
        System.out.println(node.getLocationInParent().getNodeClass());
        System.out.println(" ");
         **/
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

    public Map<Integer, HashSet<ASTNode>> getHeuristicMap() {
        return this.heuristicMap;
    }
}
