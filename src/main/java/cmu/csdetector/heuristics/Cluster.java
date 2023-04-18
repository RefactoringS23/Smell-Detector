package cmu.csdetector.heuristics;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.Collections;
import java.util.Objects;

public class Cluster {
    private final ClusterLine startLine;
    private final ClusterLine endLine;

    private Set<ASTNode> accessedVariables;


    public Cluster(Integer startLine, Integer endLine, Set<ASTNode> accessedVariables) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.accessedVariables = accessedVariables;
    };

    public Cluster(Integer startLine, Integer endLine) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
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


    public static Set<Cluster> makeClusters(SortedMap<Integer, HashSet<ASTNode>> table) {
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
                            clusters.add(new Cluster(currentLine, currentEndLine, accessedVariables));
                            break;
                        }
                    }
                } else {
                    // In case of empty line, we cluster for safety measures
                    Set<ASTNode> accessedVariables = getListOfAccessedVariables(table, currentLine, currentEndLine);
                    clusters.add(new Cluster(currentLine, currentEndLine, accessedVariables));
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
                        newClusters.add(new Cluster(openClusterStartLine.getLineNumber(), line.getCluster().getEndLineNumber(), mergedAccessedVars));
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
            access.addAll(table.get(i));
        }
        return access;
    }

}
