package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementObjectsVisitor extends ASTVisitor {
    private SortedMap<Integer, HashSet<ASTNode>> heuristicMap;
    private Map<Integer, ArrayList<Integer>> ifMap;

    private Map<String, ASTNode> nodeNameMap;

    private List<ASTNode> nodesDeclared = new ArrayList<>();

    public StatementObjectsVisitor(Map<Integer, ArrayList<Integer>> ifMap) {
        this.heuristicMap = new TreeMap<Integer, HashSet<ASTNode>>();
        this.nodeNameMap = new HashMap<String, ASTNode>();
        this.ifMap = ifMap;
    };
    public StatementObjectsVisitor() {
        this.heuristicMap = new TreeMap<Integer, HashSet<ASTNode>>();
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
            addNodeToMap((ASTNode) node, lineNumber);
            this.nodeNameMap.put(binding.getName(), node);

            ArrayList elseArray = this.ifMap.get(lineNumber);
            if (elseArray != null) {
                for (int i = 0; i < elseArray.size(); i++) {
                    addNodeToMap((ASTNode) node,(Integer) elseArray.get(i));
                    this.nodeNameMap.put(binding.getName(), node);
                }
            }
        }
        return true;
    };



    public boolean visit(VariableDeclarationFragment node) {
        this.nodesDeclared.add(node);
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
        addNodeToMap(astNode, lineNumber);
        this.nodeNameMap.put(nodeName, node);

        ArrayList elseArray = this.ifMap.get(lineNumber);
        if (elseArray != null)
        {
            for (int i=0; i< elseArray.size(); i++)
            {
                addNodeToMap(astNode,(Integer) elseArray.get(i));
                this.nodeNameMap.put(nodeName, node);
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

    public SortedMap<Integer, HashSet<ASTNode>> getHeuristicMap() {
        return this.heuristicMap;
    }

    public Map<String, ASTNode> getNodeNameMap() {
        return this.nodeNameMap;
    }
    public List<ASTNode> getNodesDeclared(){
        return this.nodesDeclared;
    }
}
