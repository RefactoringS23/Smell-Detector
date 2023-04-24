package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementObjectsVisitor extends ASTVisitor {
    private SortedMap<Integer, Set<String>> heuristicMap;
    private Map<Integer, ArrayList<Integer>> ifMap;

    private Map<String, ASTNode> nodeNameMap;

    private Map<ASTNode, Integer> nodesDeclared = new HashMap<>();

    public StatementObjectsVisitor(Map<Integer, ArrayList<Integer>> ifMap) {
        this.heuristicMap = new TreeMap<Integer, Set<String>>();
        this.nodeNameMap = new HashMap<String, ASTNode>();
        this.ifMap = ifMap;
    };
    public StatementObjectsVisitor() {
        this.heuristicMap = new TreeMap<Integer, Set<String>>();
        this.nodeNameMap = new HashMap<String, ASTNode>();
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
            addNodeToMap(node.resolveBinding().getName(), lineNumber);
            this.nodeNameMap.put(binding.getName(), node);

            ArrayList elseArray = this.ifMap.get(lineNumber);
            if (elseArray != null) {
                for (int i = 0; i < elseArray.size(); i++) {
                    addNodeToMap(node.resolveBinding().getName(),(Integer) elseArray.get(i));
                    this.nodeNameMap.put(binding.getName(), node);
                }
            }
        }
        return true;
    };



    public boolean visit(VariableDeclarationFragment node) {
        this.nodesDeclared.put(node.getName(), getStartLineNumber(node));
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

        String nodeName = node.resolveMethodBinding().getName();
        ASTNode astNode = (ASTNode) node;

        Integer lineNumber = getStartLineNumber(node);
        addNodeToMap(nodeName, lineNumber);
        this.nodeNameMap.put(nodeName, node);

        ArrayList elseArray = this.ifMap.get(lineNumber);
        if (elseArray != null)
        {
            for (int i=0; i< elseArray.size(); i++)
            {
                addNodeToMap(nodeName,(Integer) elseArray.get(i));
                this.nodeNameMap.put(nodeName, node);
            }
        }
        return true;
    }

    private void addNodeToMap (String node, Integer lineNumber) {
        Set<String> hashSet = this.heuristicMap.get(lineNumber);
        if (hashSet == null) {
            hashSet = new HashSet<String>();
        }
        hashSet.add(node);
        this.heuristicMap.put(lineNumber, hashSet);
    }

    private Integer getStartLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public SortedMap<Integer, Set<String>> getHeuristicMap() {
        return this.heuristicMap;
    }

    public Map<String, ASTNode> getNodeNameMap() {
        return this.nodeNameMap;
    }
    public Map<ASTNode, Integer> getNodesDeclared(){
        return this.nodesDeclared;
    }
}
