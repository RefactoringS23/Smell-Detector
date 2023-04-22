package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

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
        String[] primitiveTypes = {"String", "int", "boolean", "long", "byte", "short", "float", "double", "char"};
        List<String> primitiveTypesList = Arrays.asList(primitiveTypes);
        if(node.getLeftHandSide() == null || node.getLeftHandSide().resolveTypeBinding() == null){
            return true;
        }
        // System.out.println(node.getLeftHandSide().resolveTypeBinding().getName());
        ASTNode leftNode = (ASTNode) node.getLeftHandSide();
        List<Integer> lineList;
        String name = "";

        if (leftNode.getNodeType() == ASTNode.ARRAY_ACCESS){
            ArrayAccess left = (ArrayAccess) leftNode;
            name = left.getArray().toString();
            lineList = this.assignmentMap.get(name);
        }
        else{
            SimpleName left = (SimpleName) leftNode;
            name = left.resolveBinding().getName();
            lineList = this.assignmentMap.get(name);
        }
        if (lineList == null) {
            lineList = new ArrayList<>();
        }


        if(primitiveTypesList.contains(node.getLeftHandSide().resolveTypeBinding().getName())) {
            lineList.add(getStartLineNumber(node));
            this.assignmentMap.put(name, lineList);
            this.nodeNameMap.put(name, node.getLeftHandSide());
        }
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
