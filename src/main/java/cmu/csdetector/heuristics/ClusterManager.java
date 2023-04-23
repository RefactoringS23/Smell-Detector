package cmu.csdetector.heuristics;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.*;

public class ClusterManager {
    private Map<Cluster, Set<ASTNode>> accessedVariablesMap;
    private Map<ASTNode, Integer> nodesDeclared;
    private Map<Cluster, HashSet<String>> missingVars;
    private SortedMap<Integer, HashSet<ASTNode>> statementObjectsMap;

    private Set<Cluster> allClusters;
    private Set<Cluster> mergedClusters;

    public ClusterManager(SortedMap<Integer, HashSet<ASTNode>> statementObjectsMap, Map<ASTNode, Integer> variableDeclarations) {
        this.statementObjectsMap = statementObjectsMap;
        this.nodesDeclared = variableDeclarations;
        this.missingVars = new HashMap<>();
        this.accessedVariablesMap = new HashMap<>();
    }

    public void createClusters() {
        allClusters = makeClusters();
        mergedClusters = createMergedClusters();
    }

    private Set<ASTNode> getListOfAccessedVariables(Integer startLine, Integer endLine) {
        Set<ASTNode> access = new HashSet<ASTNode>();
        for (int i = startLine; i <= endLine; i++) {
            if(this.statementObjectsMap.get(i) == null) continue;
            access.addAll(this.statementObjectsMap.get(i));
        }
        return access;
    }

    private void setMissingVarsForValidCluster(Cluster cluster) {
        Set<String> requiredAttributes = new HashSet<>();

        for (ASTNode n : accessedVariablesMap.get(cluster)) {
            if (nodesDeclared.get(n) == null || nodesDeclared.get(n) < cluster.getStartLine().getLineNumber() || nodesDeclared.get(n) > cluster.getEndLine().getLineNumber()) {
                requiredAttributes.add(n.toString());
            }
        }
        cluster.setMissingVars(requiredAttributes);
    }

    public Set<Cluster> makeClusters() {
        Set<Cluster> clusters = new HashSet<>();
        int stepSize = 1;
        int methodSize = this.statementObjectsMap.lastKey();

        while (stepSize < methodSize) {
            for (Integer currentLine : this.statementObjectsMap.keySet()) {
                Set<ASTNode> row = this.statementObjectsMap.get(currentLine);
                int currentEndLine = currentLine + stepSize;

                if (this.statementObjectsMap.containsKey(currentEndLine)) {
                    for (ASTNode variableOrMethodCall : row) {
                        if (this.statementObjectsMap.get(currentEndLine).contains(variableOrMethodCall)) {
//                            Set<ASTNode> accessedVariables = getListOfAccessedVariables(table, currentLine, currentEndLine);
                            clusters.add(new Cluster(currentLine, currentEndLine));
                            break;
                        }
                    }
                } else {
                    // In case of empty line, we for safety measures
//                    Set<ASTNode> accessedVariables = getListOfAccessedVariables(table, currentLine, currentEndLine);
//                    clusters.add(new Cluster(currentLine, currentEndLine));
                }
            }

            stepSize++;
        }
        return clusters;
    }


    public static List<ClusterLine> convertListOfClusterObjectsToSortedList(Set<Cluster> clusters) {
        List<ClusterLine> sortedLines = new ArrayList<>();
        for (Cluster cluster : clusters) {
            sortedLines.add(cluster.getStartLine());
            sortedLines.add(cluster.getEndLine());
        }

        Collections.sort(sortedLines);

        return sortedLines;
    }

    public Set<Cluster> createMergedClusters() {
        Set<Cluster> mergeCandidates = new HashSet<>(this.allClusters);
        Set<Cluster> finalClusters = mergeCandidates;
        Set<Cluster> newClusters = new HashSet<>();
        do {
            List<ClusterLine> sortedLines = convertListOfClusterObjectsToSortedList(mergeCandidates);
            List<ClusterLine> currentOpenClusters = new ArrayList<>();

            for (ClusterLine line : sortedLines) {
                if (line.getIsStart()) {
                    for (ClusterLine openClusterStartLine : currentOpenClusters) {
                        Set<ASTNode> mergedAccessedVars = new HashSet<ASTNode>();
//                        mergedAccessedVars.addAll(openClusterStartLine.getCluster().getAccessedVariables());
//                        mergedAccessedVars.addAll(line.getCluster().getAccessedVariables());
                        newClusters.add(new Cluster(openClusterStartLine.getLineNumber(), line.getCluster().getEndLineNumber()));
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


    public Set<Cluster> filterValidClusters(Set<Cluster> blocks) {
        Set<Cluster> filteredClusters = new HashSet<>();
        for (Cluster cluster : mergedClusters) {
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
        setAccessedVariablesForValidClusters(filteredClusters);
        return filteredClusters;
    }

    private void setAccessedVariablesForValidClusters(Set<Cluster> validClusters) {
        for (Cluster cluster : validClusters) {
            Set<ASTNode> accessedVariables = getListOfAccessedVariables(cluster.getStartLineNumber(), cluster.getEndLineNumber());
            setMissingVarsForValidCluster(cluster);
            accessedVariablesMap.put(cluster, accessedVariables);
            cluster.setAccessedVariables(accessedVariables);
        }
    }

    public void prepareClustersForRanking(Set<Cluster> filteredClusters) {
        this.calculateBenefitOfClusters(filteredClusters);
        for (Cluster cluster : filteredClusters) {
            cluster.calculateClusterSize(this.statementObjectsMap);
        }
    }

    // Call this method after filtering out the invalid clusters (and before ranking) to calculate the LCOM of
    // the valid clusters
    private void calculateBenefitOfClusters(Set<Cluster> clusters) {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs((this.statementObjectsMap));
        int minKey = this.statementObjectsMap.firstKey();
        int maxKey = this.statementObjectsMap.lastKey();
        Cluster methodCluster = new Cluster(minKey, maxKey);
        methodCluster.calculateLcom(linePairs);
        for (Cluster cluster : clusters) {
            cluster.calculateLcom(linePairs);
            cluster.calculateBenefit(methodCluster, linePairs);
        }
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
    public void calculateLcomOfClusters(Set<Cluster> clusters, SortedMap<Integer, HashSet<ASTNode>> table) {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs((table));
        for (Cluster cluster : clusters) {
            cluster.calculateLcom(linePairs);
        }
    }


    public static SortedMap<Integer, Set<Integer>> buildLinePairs (SortedMap<Integer, HashSet<ASTNode>> table) {
        SortedMap<Integer, Set<Integer>> linePairs = new TreeMap<>();
        for (Integer thisLine : table.keySet()) {
            linePairs.put(thisLine, new HashSet<>());
            for (Integer otherLine : table.keySet()) {
                if (thisLine.equals(otherLine)) continue;
                for (ASTNode variable : table.get(thisLine)) {
                    if (table.get(otherLine).toString().contains(variable.toString())) {
                        linePairs.get(thisLine).add(otherLine);
                        break;
                    }
                }
            }
        }
        return linePairs;
    }


}
