package cmu.csdetector.heuristics;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Objects;

import cmu.csdetector.ast.visitors.AssignmentVisitor;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.*;

public class Cluster {
    private final ClusterLine startLine;
    private final ClusterLine endLine;

    private double benefit;
    private Set<ASTNode> accessedVariables;
    private Map<String, List<Integer>> accessedVariables1;
    private static Map<String, List<Integer>> assignedVariables;

    private double lcom;
    private static Map<ASTNode, Integer> nodesDeclared;

    private double clusterSize;
    private Set<String> missingVars;

    private boolean isAlternative = false;

    private List<Cluster> alternatives;


    public Cluster(Integer startLine, Integer endLine, Set<ASTNode> accessedVariables, Map<String, List<Integer>> accessedVariables1) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.accessedVariables = accessedVariables;
        this.clusterSize = Math.max(0, this.endLine.getLineNumber() - this.startLine.getLineNumber());
        this.alternatives = new ArrayList<>();
        this.missingVars = getAttributesList();
        this.assignedVariables = new HashMap<>();
        this.accessedVariables1 = accessedVariables1;
    };

    public Cluster(Integer startLine, Integer endLine) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.clusterSize = Math.max(0, this.endLine.getLineNumber() - this.startLine.getLineNumber());
        this.alternatives = new ArrayList<>();
        this.assignedVariables = new HashMap<>();
    }

    public ClusterLine getStartLine() {
        return startLine;
    }

    public Integer getStartLineNumber() {
        return startLine.getLineNumber();
    }

    public ClusterLine getEndLine() {
        return endLine;
    }

    public Integer getEndLineNumber() {
        return endLine.getLineNumber();
    }

    public Set<ASTNode> getAccessedVariables() {
        return accessedVariables;
    }

    public Map<String, List<Integer>> getAccessedVariables1() {
        return accessedVariables1;
    }

    public double getLcom() {
        return lcom;
    }

    public void calculateLcom (SortedMap<Integer, Set<Integer>> linePairs) {
        // p = number of pairs of statements that do not share variables
        // q = number of pairs of lines that share variables

        double p = 0, q = 0;

        for (int i = this.getStartLineNumber(); i <= this.getEndLineNumber(); i++) {
            Set<Integer> pairs = linePairs.get(i);
            if (pairs == null) {
                if (i < this.getEndLineNumber()) {
                    p++;
                }
                continue;
            }
            for (int j = i + 1; j <= this.getEndLineNumber(); j++) {
                if (pairs.contains(j)) {
                    q++;
                } else {
                    p++;
                }
            }
        }
        this.lcom = p - q;
        if (this.lcom < 0) this.lcom = 0;
    };
    public double getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(double clusterSize) {
        this.clusterSize = clusterSize;
    }

    public double getBenefit() {
        return benefit;
    }

    public boolean isAlternative() {
        return isAlternative;
    }

    public void setAlternative(boolean alternative) {
        isAlternative = alternative;
    }

    public List<Cluster> getAlternatives() {
        return alternatives;
    }
    public void addNewAlternativeCluster(Cluster alternative) {
        this.alternatives.add(alternative);
    };

    public static Set<Cluster> makeClusters(SortedMap<Integer, HashSet<ASTNode>> table, SortedMap<Integer, HashSet<String>> table1) {
        Set<Cluster> clusters = new HashSet<>();
        int stepSize = 1;
        int methodSize = table.lastKey();
        
        while (stepSize < methodSize) {
            for (Integer currentLine : table.keySet()) {
                Set<ASTNode> row = table.get(currentLine);
                int currentEndLine = currentLine + stepSize;

                if (table.containsKey(currentEndLine)) {
                    for (ASTNode variableOrMethodCall : row) {
                        if (table.get(currentEndLine).contains(variableOrMethodCall)) {
                            Set<ASTNode> accessedVariables = getListOfAccessedVariables(table, currentLine, currentEndLine);
                            Map<String, List<Integer>> accessedVariables1 = getListOfAccessedVariables1(table1, currentLine, currentEndLine);
                            clusters.add(new Cluster(currentLine, currentEndLine, accessedVariables, accessedVariables1));
                            break;
                        }
                    }
                } else {
                    // In case of empty line, we for safety measures
                    Set<ASTNode> accessedVariables = getListOfAccessedVariables(table, currentLine, currentEndLine);
                    Map<String, List<Integer>> accessedVariables1 = getListOfAccessedVariables1(table1, currentLine, currentEndLine);
                    clusters.add(new Cluster(currentLine, currentEndLine, accessedVariables, accessedVariables1));
                }
            }
            
            stepSize++;
        }
        return clusters;
    }
    
    public static List<ClusterLine> convertListOfClusterObjectsToSortedList(Set<Cluster> clusters) {
        List<ClusterLine> sortedLines = new ArrayList<>();
        for (Cluster cluster : clusters) {
            sortedLines.add(cluster.startLine);
            sortedLines.add(cluster.endLine);
        }
        
        Collections.sort(sortedLines);

        return sortedLines;
    }

    public static Set<Cluster> createMergedClusters(Set<Cluster> clusters) {
        Set<Cluster> mergeCandidates = new HashSet<>(clusters);
        Set<Cluster> finalClusters = mergeCandidates;
        Set<Cluster> newClusters = new HashSet<>();
        do {
            List<ClusterLine> sortedLines = convertListOfClusterObjectsToSortedList(mergeCandidates);
            List<ClusterLine> currentOpenClusters = new ArrayList<>();

            for (ClusterLine line : sortedLines) {
                if (line.getIsStart()) {
                    for (ClusterLine openClusterStartLine : currentOpenClusters) {
                        Set<ASTNode> mergedAccessedVars = new HashSet<ASTNode>();
                        mergedAccessedVars.addAll(openClusterStartLine.getCluster().getAccessedVariables());
                        mergedAccessedVars.addAll(line.getCluster().getAccessedVariables());

                        Map<String, List<Integer>> mergedAccessedVarsNames = new HashMap<String, List<Integer>>();
                        mergedAccessedVarsNames.putAll(openClusterStartLine.getCluster().getAccessedVariables1());
                        mergedAccessedVarsNames.putAll(line.getCluster().getAccessedVariables1());

                        newClusters.add(new Cluster(openClusterStartLine.getLineNumber(), line.getCluster().getEndLineNumber(), mergedAccessedVars, mergedAccessedVarsNames));
                    }
                    currentOpenClusters.add(line);
                } else {
                    currentOpenClusters.remove(line.getCluster().getStartLine());
                }
            }
            mergeCandidates = newClusters;
            finalClusters.addAll(newClusters);
            newClusters = new HashSet<>();

        } while (mergeCandidates.size() > 0);

        return finalClusters;
    }

    public static Set<Cluster> filterValidClusters(Set<Cluster> clusters, Set<Cluster> blocks) {
        Set<Cluster> filteredClusters = new HashSet<>();
        for (Cluster cluster : clusters) {
            // step 1 : find smallest block that contains this cluster
            Cluster smallestBlock = findSmallestBlockContainingThisCluster(cluster, blocks);
            if (smallestBlock == null) continue;
            // step 2: find every sub block of the smallest block
            Set<Cluster> subBlocks = findSubBlocksOfBlock(smallestBlock, blocks);
            // step 3: check that endLine is not in any of the sub blocks
            if (!startLineIsInSubBlocks(cluster.getStartLineNumber(), subBlocks) &&
                !endLineIsInSubBlocks(cluster.getEndLineNumber(), subBlocks)) {
                filteredClusters.add(cluster);
            }
        }
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

    @Override
    public String toString() {
        return "Cluster: " + this.startLine.getLineNumber().toString() + " to " + this.endLine.getLineNumber().toString();
    }

    // Call this method after filtering out the invalid clusters (and before ranking) to calculate the LCOM of
    // the valid clusters
    public static void calculateLcomOfClusters(Set<Cluster> clusters, SortedMap<Integer, HashSet<String>> table) {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs((table));
        for (Cluster cluster : clusters) {
            cluster.calculateLcom(linePairs);
        }
    }

    public static SortedMap<Integer, Set<Integer>> buildLinePairs (SortedMap<Integer, HashSet<String>> table) {
        SortedMap<Integer, Set<Integer>> linePairs = new TreeMap<>();
        for (Integer thisLine : table.keySet()) {
            linePairs.put(thisLine, new HashSet<Integer>());
            for (Integer otherLine : table.keySet()) {
                if (thisLine.equals(otherLine)) continue;
                for (String variable : table.get(thisLine)) {
                    if (table.get(otherLine).contains(variable)) {
                        linePairs.get(thisLine).add(otherLine);
                        break;
                    }
                }
            }
        }
        return linePairs;
    }
    @Override
    public int hashCode() {
        return this.getStartLineNumber() + this.getEndLineNumber();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;

        Cluster otherCluster = (Cluster) other;
        return Objects.equals(this.getStartLineNumber(), otherCluster.getStartLineNumber()) &&
                Objects.equals(this.getEndLineNumber(), otherCluster.getEndLineNumber());
    }

    private static Set<ASTNode> getListOfAccessedVariables(SortedMap<Integer, HashSet<ASTNode>> table, Integer startLine, Integer endLine) {
        Set<ASTNode> access = new HashSet<ASTNode>();
        for (int i = startLine; i <= endLine; i++) {
            if(table.get(i) == null) continue;
            access.addAll(table.get(i));
        }
        return access;
    }

    private static Map<String, List<Integer>>  getListOfAccessedVariables1(SortedMap<Integer, HashSet<String>> table, Integer startLine, Integer endLine) {
        Map<String, List<Integer>>  access = new HashMap<String, List<Integer>>();
        for (int ind: table.keySet()) {
            for(String name: table.get(ind)) {
                List<Integer> indList = access.get(name);
                if(indList == null) {
                    indList = new ArrayList<>();
                }
                indList.add(ind);
                access.put(name, indList);
            }
        }
        //System.out.println("the table");
        //System.out.println(access);
        return access;
    }

    private Set<String> getAttributesList() {
        Set<String> requiredAttributes = new HashSet<>();

        for (ASTNode n : accessedVariables) {
            if (nodesDeclared.get(n) == null || nodesDeclared.get(n) < startLine.getLineNumber() || nodesDeclared.get(n) > endLine.getLineNumber()) {
                requiredAttributes.add(n.toString());
            }
        }
        ;
        return requiredAttributes;
    }

    public String getReturnType() {
        String returnType = "void";
        Integer count = 0;

        for (String node: assignedVariables.keySet()) {
            //System.out.println("  aaaa  ");
            //System.out.println(index);
            //System.out.println(node);
            //System.out.println(assignedVariables);
            //System.out.println(startLine.getLineNumber());
            //System.out.println(endLine.getLineNumber());
                //System.out.println(index);
            List<Integer> indexList = accessedVariables1.get(node);
            int insideCluster = 0;
            int afterCluster = 0;
            if(indexList != null) {
                for( int ind : indexList) {
                    //System.out.println(ind);
                    if (ind >= startLine.getLineNumber() && ind <= endLine.getLineNumber()) {
                        insideCluster += 1;
                    } else if (ind > endLine.getLineNumber()) {
                        afterCluster += 1;
                    }
                }
            }
            if (insideCluster > 0 && afterCluster > 0) {
                //System.out.println(ind);
                returnType = node;
                count += 1;
                //System.out.println(returnType);
            }
        }
        if (count>1) {
            returnType = "invalid";
        }
        System.out.println(" ");
        System.out.println("returntype");
        System.out.println(startLine.getLineNumber());
        System.out.println(endLine.getLineNumber());
        System.out.println(returnType);
        return returnType;
    }
    public static void setDeclaredNodes(Map<ASTNode, Integer> vardDecs){
        nodesDeclared = vardDecs;
    }

    public static void setAssignedNodes(Map<String, List<Integer>> varsAssigned){;
        assignedVariables = varsAssigned;
    }
}
