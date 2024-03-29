package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class IfBlockVisitor extends ASTVisitor {
    private Map<Integer, ArrayList<Integer>> ifMap;
    private Set<Integer> specialLine;

    private Set<List<Integer>> loopStartEnd;

    private Set<Integer> breakSet;
    public IfBlockVisitor() {
        this.ifMap = new HashMap<Integer, ArrayList<Integer>>();
        this.specialLine = new HashSet<>();
        this.loopStartEnd = new HashSet<List<Integer>>();
        this.breakSet = new HashSet<Integer>();
    }

    @Override
    public boolean visit(IfStatement node) {
        Integer nodeStart = getStartLineNumber(node);
        if (node.getElseStatement() != null) {
            addToMap(nodeStart, getStartLineNumber(node.getElseStatement()));
        }

        if(node.getThenStatement().getNodeType() == ASTNode.BLOCK){
            Block ifBlock = (Block) node.getThenStatement();
            List ifBlockList = ifBlock.statements();
            for (int i=0; i<ifBlockList.size(); i++) {
                if(ifBlockList.get(i).getClass() != null && ifBlockList.get(i).getClass().toString().contains("IfStatement")){
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
            specialLine.add(nodeStart);
        }

        return true;
    }

    @Override
    public boolean visit(BreakStatement node) {
        breakSet.add(getStartLineNumber(node));
        return true;
    }
    @Override
    public boolean visit(ContinueStatement node) {
        breakSet.add(getStartLineNumber(node));
        return true;
    }
    
    @Override
    public boolean visit(CatchClause node) {
        specialLine.add(getStartLineNumber(node));
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        specialLine.add(getStartLineNumber(node));
        List<Integer> lines = new ArrayList<>();
        lines.add(getStartLineNumber(node));
        lines.add(getEndLineNumber(node));
        loopStartEnd.add(lines);
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        specialLine.add(getStartLineNumber(node));
        List<Integer> lines = new ArrayList<>();
        lines.add(getStartLineNumber(node));
        lines.add(getEndLineNumber(node));
        loopStartEnd.add(lines);
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        specialLine.add(getStartLineNumber(node));
        List<Integer> lines = new ArrayList<>();
        lines.add(getStartLineNumber(node));
        lines.add(getEndLineNumber(node));
        loopStartEnd.add(lines);
        return true;
    }

    @Override
    public boolean visit(LambdaExpression node) {
        specialLine.add(getStartLineNumber(node));
        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {
        specialLine.add(getStartLineNumber(node));
        return true;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        specialLine.add(getStartLineNumber(node));
        List<Integer> lines = new ArrayList<>();
        lines.add(getStartLineNumber(node));
        lines.add(getEndLineNumber(node));
        loopStartEnd.add(lines);
        return true;
    }


    @Override
    public boolean visit(WhileStatement node) {
        specialLine.add(getStartLineNumber(node));
        List<Integer> lines = new ArrayList<>();
        lines.add(getStartLineNumber(node));
        lines.add(getEndLineNumber(node));
        loopStartEnd.add(lines);
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

    public Set<Integer> getSpecialLine() {
        return this.specialLine;
    }

    public Set<List<Integer>> getLoopStartEnd() {
        return this.loopStartEnd;
    }

    public Set<Integer> getBreakSet() {
        return this.breakSet;
    }
}
