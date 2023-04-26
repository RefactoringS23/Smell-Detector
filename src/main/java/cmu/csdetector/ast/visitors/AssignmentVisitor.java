package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class AssignmentVisitor extends ASTVisitor {
    private HashMap<String, List<Integer>> assignmentMap;
    private Map<String, String> nodeTypeMap;

    private Set<Integer> specialLine;

    private String[] primitiveTypes;

    public AssignmentVisitor (Set<Integer> lineSet) {
        assignmentMap = new HashMap<String, List<Integer>>();
        nodeTypeMap = new HashMap<String, String>();
        specialLine = lineSet;
        primitiveTypes = new String[]{"String", "int", "boolean", "long", "byte", "short", "float", "double", "char"};
    }

    @Override
    public boolean visit(Assignment node) {
        if(node.getLeftHandSide() == null || node.getLeftHandSide().resolveTypeBinding() == null){
            return true;
        }

        ASTNode leftNode = (ASTNode) node.getLeftHandSide();
        if(leftNode != null && node.getLeftHandSide().resolveTypeBinding() != null) {
            addToMap(leftNode, node.getLeftHandSide().resolveTypeBinding().getName());
        }

        return true;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        if (node.getOperator() == PrefixExpression.Operator.INCREMENT || node.getOperator() == PrefixExpression.Operator.DECREMENT) {
            ASTNode leftNode = (ASTNode) node.getOperand();
            if(leftNode != null && node.getOperand().resolveTypeBinding() != null) {
                addToMap(leftNode, node.getOperand().resolveTypeBinding().getName());
            }
        }
        return true;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        if (node.getOperator() == PostfixExpression.Operator.INCREMENT || node.getOperator() == PostfixExpression.Operator.DECREMENT) {
            ASTNode leftNode = (ASTNode) node.getOperand();
            if(leftNode != null && node.getOperand().resolveTypeBinding() != null) {
                addToMap(leftNode, node.getOperand().resolveTypeBinding().getName());
            }
        }
        return true;
    }

    private void addToMap (ASTNode leftNode, String nodeName) {
        List<String> primitiveTypesList = Arrays.asList(this.primitiveTypes);
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


        if(primitiveTypesList.contains(nodeName)) {
            if(!this.specialLine.contains(getStartLineNumber(leftNode))){
                lineList.add(getStartLineNumber(leftNode));
                this.assignmentMap.put(name, lineList);
                this.nodeTypeMap.put(name, nodeName);
            }
        }
    }

    private Integer getStartLineNumber(ASTNode astNode) {
        int startPosition = astNode.getStartPosition();
        CompilationUnit cu = (CompilationUnit) astNode.getRoot();
        return cu.getLineNumber(startPosition);
    }

    public HashMap<String, List<Integer>> getLineMap() {
        return this.assignmentMap;
    }

    public Map<String, String> getNodeTypeMap() {
        return this.nodeTypeMap;
    }
}
