package cmu.csdetector.heuristics;

import cmu.csdetector.ast.visitors.AssignmentVisitor;
import cmu.csdetector.ast.visitors.IfBlockVisitor;
import cmu.csdetector.ast.visitors.StatementObjectsVisitor;
import cmu.csdetector.resources.Type;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.stream.Collectors;

public class ClusterManager {

    private Map<String, Integer> nodesDeclared;
    private SortedMap<Integer, HashSet<String>> statementObjectsMap;

    private Map<String, ASTNode> stringASTNodeMap;

    private Type declaringClass;
    private Cluster finalCluster; // ClusterManager returns this finalCluster

    private Set<Cluster> filteredClusters;
    private Map<String, String> nodeTypeMap;
    private Map<String, List<Integer>> assignmentVariables;
    private Set<Integer> breakSet;
    private  Set<List<Integer>> loopSet;

    public ClusterManager(MethodDeclaration md, Type declaringClass) {
        this.declaringClass = declaringClass;

        IfBlockVisitor ifBlockVisitor = new IfBlockVisitor();
        md.accept(ifBlockVisitor);

        this.loopSet = ifBlockVisitor.getLoopStartEnd();
        this.breakSet = ifBlockVisitor.getBreakSet();

        StatementObjectsVisitor statementObjectsVisitor = new StatementObjectsVisitor(ifBlockVisitor.getIfMap());
        md.accept(statementObjectsVisitor);

        this.statementObjectsMap = statementObjectsVisitor.getHeuristicMap();
        this.stringASTNodeMap = statementObjectsVisitor.getNodeNameMap();
        this.nodesDeclared = statementObjectsVisitor.getNodesDeclared();

        AssignmentVisitor assignmentVisitor = new AssignmentVisitor(ifBlockVisitor.getSpecialLine());
        md.accept(assignmentVisitor);

        this.nodeTypeMap = assignmentVisitor.getNodeTypeMap();
        this.assignmentVariables = assignmentVisitor.getLineMap();
    }

    public List<Cluster> getTopClusters(Set<Cluster> blocks) {
        Set<Cluster> baseClusters = makeClusters();
        Set<Cluster> allClusters = createMergedClusters(baseClusters);
        this.filterValidClusters(allClusters, blocks);
        this.calculateLcomOfClusters();
        this.prepareClustersForRanking();
        ClusterRanking.groupClusters(filteredClusters);
        List<Cluster> finalClusters = this.rankClusters();
        return finalClusters;
    }

    private List<Cluster> rankClusters() {
        Set<Cluster> primaryClusters = new HashSet<>();
        for (Cluster cluster : this.filteredClusters) {
            if (!cluster.isAlternative()) {
                primaryClusters.add(cluster);
            }
        }
        List<Cluster> sortedClusters = primaryClusters.stream()
                .sorted(Comparator.comparing(Cluster::getBenefit).reversed())
                .collect(Collectors.toList());
        return sortedClusters;

    }

    private Set<ASTNode> getListOfAccessedVariables(Integer startLine, Integer endLine) {
        Set<ASTNode> access = new HashSet<>();
        for (int i = startLine; i <= endLine; i++) {
            if(this.statementObjectsMap.get(i) == null) continue;
            HashSet<ASTNode> nodes = new HashSet<>();
            for (String name: this.statementObjectsMap.get(i)) {
                if (this.stringASTNodeMap.containsKey(name)) {
                    nodes.add(this.stringASTNodeMap.get(name));
                }
            }
            access.addAll(nodes);
        }
        return access;
    }

    private void setMissingVarsForValidCluster(Cluster cluster) {
        Set<ASTNode> requiredAttributes = new HashSet<>();
        for (ASTNode n : cluster.getAccessedVariables()) {
            if(n.getNodeType() == ASTNode.SIMPLE_NAME) {
                SimpleName variable = (SimpleName) n;
                ITypeBinding binding = ((SimpleName) n).resolveTypeBinding();
                String variableParentClassName = null;
                if(binding != null) {
                    variableParentClassName = binding.getTypeDeclaration().getName();
                }

                if(variableParentClassName != cluster.getParentClassName() && variable.getParent().getNodeType() != ASTNode.QUALIFIED_NAME) {
                    if (this.nodesDeclared.get(variable.getIdentifier()) == null || this.nodesDeclared.get(variable.getIdentifier()) < cluster.getStartLine().getLineNumber() ) {
                        requiredAttributes.add(n);
                    }
                }
            }
        }
        cluster.setMissingVars(requiredAttributes);
    }

    private Map<String, List<Integer>>  getVariableLineNumbers() {
        Map<String, List<Integer>>  access = new HashMap<String, List<Integer>>();
        for (int ind: statementObjectsMap.keySet()) {
            for(String name: statementObjectsMap.get(ind)) {

                List<Integer> indList = access.get(name);
                if(indList == null) {
                    indList = new ArrayList<>();
                }
                indList.add(ind);
                access.put(name, indList);
            }
        }
        return access;
    }

    public String getReturnValue(Cluster cluster) {
        Set<String> returnType = new HashSet<>();

        Map<String, List<Integer>> variablesAccessedInClass = getVariableLineNumbers();
        for (String node: this.assignmentVariables.keySet()) {
            List<Integer> indexList = variablesAccessedInClass.get(node);
            int insideCluster = 0;
            int afterCluster = 0;
            if(indexList != null) {
                for( int ind : indexList) {
                    if (ind >= cluster.getStartLineNumber() && ind <= cluster.getEndLineNumber()) {
                        insideCluster += 1;
                    } else if (ind > cluster.getEndLineNumber()) {
                        afterCluster += 1;
                    }
                }
            }
            if (insideCluster > 0 && afterCluster > 0) {
                returnType.add(node);
            }
        }

        for(String nodeName : this.nodesDeclared.keySet()){
            int declaredAt = this.nodesDeclared.get(nodeName);
            if(declaredAt >= cluster.getStartLineNumber() && declaredAt <= cluster.getEndLineNumber()) {
                List<Integer> indexList = variablesAccessedInClass.get(nodeName);
                if(indexList != null) {
                    for( int ind : indexList) {
                        if (ind > cluster.getEndLineNumber()) {
                            returnType.add(nodeName);
                        }
                    }
                }
            }

        }

        if (returnType.size()>1) {
            return("invalid");
        }

        if(returnType.size() == 1) {
            for (String str : returnType){
                return str;
            }
        }
        return "void";
    }

    public void getReturnType(Cluster cluster) {
        String returnValue = getReturnValue(cluster);
        if (returnValue != "void" && returnValue != "invalid") {
            String returnType = this.nodeTypeMap.get(returnValue);
            if (returnType != null) {
                cluster.setReturnType(returnType);
            }
            else {
                Expression node = (Expression) this.stringASTNodeMap.get(returnValue);
                if(node != null && node.resolveTypeBinding() != null) {
                    cluster.setReturnType(node.resolveTypeBinding().getName());
                }
                else {
                    cluster.setReturnType("void");
                }
            }
        }
        else {
            cluster.setReturnType(returnValue);
        }
    }

    public void getMethodName(Cluster cluster, int i) {
        String returnValue = getReturnValue(cluster);
        String name = String.join("","LeoIsTheBestProf", String.valueOf(i));
        if (returnValue != "void" && returnValue != "invalid") {
            name = String.join("","get",returnValue);
        }
        cluster.setMethodName(name);
    }

    private Set<Cluster> makeClusters() {
        Set<Cluster> clusters = new HashSet<>();
        int stepSize = 1;
        int methodSize = this.statementObjectsMap.lastKey();

        while (stepSize < methodSize) {
            for (Integer currentLine : this.statementObjectsMap.keySet()) {
                Set<String> row = this.statementObjectsMap.get(currentLine);
                int currentEndLine = currentLine + stepSize;

                if (this.statementObjectsMap.containsKey(currentEndLine)) {
                    for (String variableOrMethodCall : row) {
                        if (this.statementObjectsMap.get(currentEndLine).contains(variableOrMethodCall)) {
                            clusters.add(new Cluster(currentLine, currentEndLine, this.declaringClass));
                            break;
                        }
                    }
                } else {
                    // In case of empty line, we add it for safety measures
                    clusters.add(new Cluster(currentLine, currentEndLine, this.declaringClass));
                }
            }

            stepSize++;
        }
        return clusters;
    }


    private static List<ClusterLine> convertListOfClusterObjectsToSortedList(Set<Cluster> clusters) {
        List<ClusterLine> sortedLines = new ArrayList<>();
        for (Cluster cluster : clusters) {
            sortedLines.add(cluster.getStartLine());
            sortedLines.add(cluster.getEndLine());
        }

        Collections.sort(sortedLines);

        return sortedLines;
    }

    private Set<Cluster> createMergedClusters(Set<Cluster> baseClusters) {
        Set<Cluster> mergeCandidates = baseClusters;
        Set<Cluster> finalClusters = mergeCandidates;
        Set<Cluster> newClusters = new HashSet<>();

        while (mergeCandidates.size() > 0) {
            List<ClusterLine> sortedLines = convertListOfClusterObjectsToSortedList(mergeCandidates);
            List<ClusterLine> currentOpenClusters = new ArrayList<>();

            for (ClusterLine line : sortedLines) {
                if (line.getIsStart()) {
                    for (ClusterLine openClusterStartLine : currentOpenClusters) {
                        if (!finalClusters.contains(new Cluster(openClusterStartLine.getLineNumber(), line.getCluster().getEndLineNumber(), this.declaringClass))) {
                            newClusters.add(new Cluster(openClusterStartLine.getLineNumber(), line.getCluster().getEndLineNumber(), this.declaringClass));
                        }
                    }
                    currentOpenClusters.add(line);
                } else {
                    currentOpenClusters.remove(line.getCluster().getStartLine());
                }
            }
            mergeCandidates = newClusters;
            finalClusters.addAll(newClusters);
            newClusters = new HashSet<>();

        }

        return finalClusters;
    }


    private Set<Cluster> filterValidClusters(Set<Cluster> mergedClusters, Set<Cluster> blocks) {
        filteredClusters = new HashSet<>();
        for (Cluster cluster : mergedClusters) {
            // step 1 : find smallest block that contains this cluster
            Cluster smallestBlock = findSmallestBlockContainingThisCluster(cluster, blocks);
            if (smallestBlock == null) continue;
            // step 2: find every sub block of the smallest block
            Set<Cluster> subBlocks = findSubBlocksOfBlock(smallestBlock, blocks);
            // step 3: check that startLine and endLine are not in any of the sub blocks
            if (!startLineIsInSubBlocks(cluster.getStartLineNumber(), subBlocks) &&
                    !endLineIsInSubBlocks(cluster.getEndLineNumber(), subBlocks)) {
                if(!invalidateClustersWithBreak(cluster) && getReturnValue(cluster) != "invalid"){
                    filteredClusters.add(cluster);
                }
            }
        }
        setAccessedVariablesForValidClusters(filteredClusters);
        int counter = 0;
        for(Cluster filteredCluster : filteredClusters) {
            getReturnType(filteredCluster);
            getMethodName(filteredCluster,counter);
            counter ++;
        }

        return filteredClusters;
    }

    public boolean invalidateClustersWithBreak(Cluster cluster) {
        Set<Integer> breakSetInside = new HashSet<Integer>();
        Set<List<Integer>> loopSetInside = new HashSet<List<Integer>>();

        for(int i: this.breakSet) {
            if(i>=cluster.getStartLineNumber() && i<= cluster.getEndLineNumber()){
                breakSetInside.add(i);
            }
        }

        for(List<Integer> ind : this.loopSet) {
            if(ind.get(0) >= cluster.getStartLineNumber() && ind.get(1)<= cluster.getEndLineNumber()) {
                loopSetInside.add(ind);
            }
        }

        Set<Integer> countOfParentLoop = new HashSet<Integer>();

        for (int i: breakSetInside){
            if(loopSetInside.size()<1){
                return true;
            }
            int count = 0;
            for(List<Integer> ind : this.loopSet) {
                if(ind.get(0) <= i && ind.get(1)>=i) {
                    count = count + 1;
                }
            }
            countOfParentLoop.add(count);
        }

        if(countOfParentLoop.size()>0) {
            return (countOfParentLoop.contains(0));
        }

        return false;
    }

    private void setAccessedVariablesForValidClusters(Set<Cluster> validClusters) {
        for (Cluster cluster : validClusters) {
            Set<ASTNode> accessedVariables = getListOfAccessedVariables(cluster.getStartLineNumber(), cluster.getEndLineNumber());
            cluster.setAccessedVariables(accessedVariables);
            setMissingVarsForValidCluster(cluster);
        }
    }

    private void prepareClustersForRanking() {
        this.calculateBenefitOfClusters(this.filteredClusters);
        for (Cluster cluster : this.filteredClusters) {
            cluster.calculateClusterSize(this.statementObjectsMap);
        }
    }

    // Call this method after filtering out the invalid clusters (and before ranking) to calculate the LCOM of
    // the valid clusters
    private void calculateBenefitOfClusters(Set<Cluster> clusters) {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs();
        int minKey = this.statementObjectsMap.firstKey();
        int maxKey = this.statementObjectsMap.lastKey();
        Cluster methodCluster = new Cluster(minKey, maxKey);
        methodCluster.calculateLcom(linePairs);
        for (Cluster cluster : clusters) {
            cluster.calculateLcom(linePairs);
            cluster.calculateBenefit(methodCluster, linePairs);
        }
    }

    public Set<Cluster> getFilteredClusters() {
        return filteredClusters;
    }


    private static Cluster findSmallestBlockContainingThisCluster(Cluster cluster, Set<Cluster> blocks) {
        Cluster smallestBlock = null;
        for (Cluster block : blocks) {
            if (block.getStartLineNumber() < cluster.getStartLineNumber() &&
                    block.getEndLineNumber() > cluster.getEndLineNumber()) {
                if (smallestBlock == null) {
                    smallestBlock = block;
                } else if (block.getEndLineNumber() - block.getStartLineNumber() <
                        smallestBlock.getEndLineNumber() - smallestBlock.getStartLineNumber()) {
                    smallestBlock = block;
                }
            }
        }
        return smallestBlock;
    }

    private static Set<Cluster> findSubBlocksOfBlock(Cluster block, Set<Cluster> blocks) {
        Set<Cluster> subBlocks = new HashSet<>();
        for (Cluster subBlock : blocks) {
            if ((subBlock.getStartLineNumber() >= block.getStartLineNumber() &&
                    subBlock.getEndLineNumber() < block.getEndLineNumber()) ||
                    (subBlock.getStartLineNumber() > block.getStartLineNumber() &&
                            subBlock.getEndLineNumber() <= block.getEndLineNumber())) {
                subBlocks.add(subBlock);
            }
        }
        return subBlocks;
    }

    private static boolean startLineIsInSubBlocks(Integer lineNumber, Set<Cluster> subBlocks) {
        for (Cluster subBlock : subBlocks) {
            if (subBlock.getStartLineNumber() < lineNumber &&
                    subBlock.getEndLineNumber() >= lineNumber) {
                return true;
            }
        }
        return false;
    }

    private static boolean endLineIsInSubBlocks(Integer lineNumber, Set<Cluster> subBlocks) {
        for (Cluster subBlock : subBlocks) {
            if (subBlock.getStartLineNumber() <= lineNumber &&
                    subBlock.getEndLineNumber() > lineNumber) {
                return true;
            }
        }
        return false;
    }

    // Call this method after filtering out the invalid clusters (and before ranking) to calculate the LCOM of
    // the valid clusters
    private void calculateLcomOfClusters() {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs();
        for (Cluster cluster : this.filteredClusters) {
            cluster.calculateLcom(linePairs);
        }
    }


    private SortedMap<Integer, Set<Integer>> buildLinePairs() {
        SortedMap<Integer, Set<Integer>> linePairs = new TreeMap<>();
        for (Integer thisLine : this.statementObjectsMap.keySet()) {
            linePairs.put(thisLine, new HashSet<>());
            for (Integer otherLine : this.statementObjectsMap.keySet()) {
                if (thisLine.equals(otherLine)) continue;
                for (String variable : this.statementObjectsMap.get(thisLine)) {
                    if (this.statementObjectsMap.get(otherLine).contains(variable)) {
                        linePairs.get(thisLine).add(otherLine);
                        break;
                    }
                }
            }
        }
        return linePairs;
    }


}
