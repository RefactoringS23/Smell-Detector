package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementObjectsVisitor extends ASTVisitor {
    private SortedMap<Integer, HashSet<ASTNode>> heuristicMap;

    public StatementObjectsVisitor() {
        this.heuristicMap = new SortedMap<Integer, HashSet<ASTNode>>() {
            @Override
            public Comparator<? super Integer> comparator() {
                return null;
            }

            @Override
            public SortedMap<Integer, HashSet<ASTNode>> subMap(Integer fromKey, Integer toKey) {
                return null;
            }

            @Override
            public SortedMap<Integer, HashSet<ASTNode>> headMap(Integer toKey) {
                return null;
            }

            @Override
            public SortedMap<Integer, HashSet<ASTNode>> tailMap(Integer fromKey) {
                return null;
            }

            @Override
            public Integer firstKey() {
                return null;
            }

            @Override
            public Integer lastKey() {
                return null;
            }

            @Override
            public Set<Integer> keySet() {
                return null;
            }

            @Override
            public Collection<HashSet<ASTNode>> values() {
                return null;
            }

            @Override
            public Set<Entry<Integer, HashSet<ASTNode>>> entrySet() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public HashSet<ASTNode> get(Object key) {
                return null;
            }

            @Override
            public HashSet<ASTNode> put(Integer key, HashSet<ASTNode> value) {
                return null;
            }

            @Override
            public HashSet<ASTNode> remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends Integer, ? extends HashSet<ASTNode>> m) {

            }

            @Override
            public void clear() {

            }
        };
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
        hashSet.add(node);
        this.heuristicMap.put(lineNumber-5, hashSet);

        return true;
    }
    private Integer getLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public SortedMap<Integer, HashSet<ASTNode>> getHeuristicMap() {
        return this.heuristicMap;
    }
}
