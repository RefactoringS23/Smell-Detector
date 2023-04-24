package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementObjectsVisitor extends ASTVisitor {
    private SortedMap<Integer, HashSet<String>> heuristicMap;
    private Map<Integer, ArrayList<Integer>> ifMap;

    private Map<String, ASTNode> nodeNameMap;

    private  Set<Integer> methodDeclarations;

    private Map<ASTNode, Integer> nodesDeclared = new HashMap<>();

    public StatementObjectsVisitor(Map<Integer, ArrayList<Integer>> ifMap) {
        this.heuristicMap = new TreeMap<Integer, HashSet<String>>();
        this.nodeNameMap = new HashMap<String, ASTNode>();
        this.ifMap = ifMap;
        this.methodDeclarations = new HashSet<>();
    };
    public StatementObjectsVisitor() {
        this.heuristicMap = new TreeMap<Integer, HashSet<String>>();
        this.nodeNameMap = new HashMap<String, ASTNode>();
        this.ifMap = new HashMap<Integer, ArrayList<Integer>>();
        this.methodDeclarations = new HashSet<>();
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


    @Override
    public boolean visit(VariableDeclarationFragment node) {
        this.nodesDeclared.put(node.getName(), getStartLineNumber(node));
        return true;
    }

    @Override
    public  boolean visit(MethodDeclaration node) {
        this.methodDeclarations.add(getStartLineNumber(node));
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

    public SortedMap<Integer, HashSet<String>> getHeuristicMap() {
        for (int i: this.methodDeclarations) {
            this.heuristicMap.remove(i);
        }
        return this.heuristicMap;
    }

    public Map<String, ASTNode> getNodeNameMap() {
        return this.nodeNameMap;
    }
    public Map<ASTNode, Integer> getNodesDeclared(){
        return this.nodesDeclared;
    }
}
