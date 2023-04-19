package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IfBlockVisitor extends ASTVisitor {
    private Map<Integer, ArrayList<Integer>> ifMap;

    public IfBlockVisitor() {
        this.ifMap = new HashMap<Integer, ArrayList<Integer>>();
    }

    @Override
    public boolean visit(IfStatement node) {
        Integer nodeStart = getStartLineNumber(node);
        if (node.getElseStatement() != null) {
            addToMap(nodeStart, getStartLineNumber(node.getElseStatement()));
        }

        Block ifBlock = (Block) node.getThenStatement();
        List ifBlockList = ifBlock.statements();
        for (int i=0; i<ifBlockList.size(); i++) {
            if(ifBlockList.get(i).getClass().toString().contains("IfStatement")){
                Integer nestedIfStart = getStartLineNumber((ASTNode) ifBlockList.get(i));
                addToMap(nodeStart, nestedIfStart);
            }
        }

        for (Map.Entry<Integer, ArrayList<Integer>> set :
                this.ifMap.entrySet()) {
            ArrayList<Integer> listOfParent = set.getValue();
            ArrayList<Integer> listOfNode = this.ifMap.get(nodeStart);
            if (listOfParent != null && listOfNode != null && listOfNode != listOfParent && listOfParent.contains(nodeStart)) {
                listOfParent.addAll(listOfNode);
            }

        }
        return true;
    }


    private void addToMap (Integer parentIf, Integer elseAndNestedIf) {
        ArrayList arrayList = this.ifMap.get(parentIf);
        if (arrayList == null) {
            arrayList = new ArrayList();
        }
        arrayList.add(elseAndNestedIf);
        this.ifMap.put(parentIf, arrayList);
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

    public Map<Integer, ArrayList<Integer>> getIfMap() {
        return this.ifMap;
    }
}