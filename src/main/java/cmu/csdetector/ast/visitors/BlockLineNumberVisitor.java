package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlockLineNumberVisitor extends ASTVisitor {
    private ArrayList<ArrayList<Integer>> lineMap;

    public BlockLineNumberVisitor() {
        this.lineMap = new ArrayList<ArrayList<Integer>>();
    }

    public boolean visit(Block node) {
        Integer startLineNumber = getStartLineNumber(node);
        Integer endLineNumber = getEndLineNumber(node);
        ArrayList arrayList = new ArrayList();
        arrayList.add(startLineNumber);
        arrayList.add(endLineNumber);
        this.lineMap.add(arrayList);
        return true;
    }

    public boolean visit(IfStatement node) {
        Integer startLineNumber = getStartLineNumber(node);
        Integer endLineNumber = getEndLineNumber(node);
        ArrayList arrayList = new ArrayList();
        arrayList.add(startLineNumber);
        arrayList.add(endLineNumber);
        this.lineMap.add(arrayList);
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

    public ArrayList<ArrayList<Integer>> getLineMap() {
        return this.lineMap;
    }
}
