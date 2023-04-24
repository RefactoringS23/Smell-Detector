package cmu.csdetector.heuristics;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.*;

public class ClusterManager {

    private Map<ASTNode, Integer> nodesDeclared;
    private SortedMap<Integer, Set<String>> statementStringsMap;
    private SortedMap<Integer, Set<ASTNode>> statementObjectsMap;

    private Cluster finalCluster; // ClusterManager returns this finalCluster

    private Set<Cluster> allClusters;
    private Set<Cluster> mergedClusters;
    private Set<Cluster> filteredClusters;

    public ClusterManager(SortedMap<Integer, Set<String>> statementStringsMap, SortedMap<Integer, Set<ASTNode>> statementObjectsMap, Map<ASTNode, Integer> variableDeclarations) {
        this.statementStringsMap = statementStringsMap;
        this.statementObjectsMap = statementObjectsMap;
        this.nodesDeclared = variableDeclarations;

    }

    public void createClusters(Set<Cluster> blocks) {
        allClusters = makeClusters();
        mergedClusters = createMergedClusters();
        filteredClusters = filterValidClusters(blocks);
        this.calculateLcomOfClusters();
        this.prepareClustersForRanking();
        ClusterRanking.rankClusters(filteredClusters);
    }

    private Set<ASTNode> getListOfAccessedVariables(Integer startLine, Integer endLine) {
        Set<ASTNode> access = new HashSet<>();
        for (int i = startLine; i <= endLine; i++) {
            if(this.statementObjectsMap.get(i) == null) continue;
            access.addAll(this.statementObjectsMap.get(i));
        }
        return access;
    }

    private void setMissingVarsForValidCluster(Cluster cluster) {
        Set<String> requiredAttributes = new HashSet<>();
        for (ASTNode n : cluster.getAccessedVariables()) {
            if (nodesDeclared.get(n) == null || nodesDeclared.get(n) < cluster.getStartLine().getLineNumber() || nodesDeclared.get(n) > cluster.getEndLine().getLineNumber()) {
                requiredAttributes.add(n.toString());
            }
        }
        cluster.setMissingVars(requiredAttributes);
    }

    private Set<Cluster> makeClusters() {
        Set<Cluster> clusters = new HashSet<>();
        int stepSize = 1;
        int methodSize = this.statementStringsMap.lastKey();

        while (stepSize < methodSize) {
            for (Integer currentLine : this.statementStringsMap.keySet()) {
                Set<String> row = this.statementStringsMap.get(currentLine);
                int currentEndLine = currentLine + stepSize;

                if (this.statementStringsMap.containsKey(currentEndLine)) {
                    for (String variableOrMethodCall : row) {
                        if (this.statementStringsMap.get(currentEndLine).contains(variableOrMethodCall)) {
                            clusters.add(new Cluster(currentLine, currentEndLine));
                            break;
                        }
                    }
                } else {
                    // In case of empty line, we add it for safety measures
                    clusters.add(new Cluster(currentLine, currentEndLine));
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

    private Set<Cluster> createMergedClusters() {
        Set<Cluster> mergeCandidates = new HashSet<>(this.allClusters);
        Set<Cluster> finalClusters = mergeCandidates;
        Set<Cluster> newClusters = new HashSet<>();

        while (mergeCandidates.size() > 0) {
            List<ClusterLine> sortedLines = convertListOfClusterObjectsToSortedList(mergeCandidates);
            List<ClusterLine> currentOpenClusters = new ArrayList<>();

            for (ClusterLine line : sortedLines) {
                if (line.getIsStart()) {
                    for (ClusterLine openClusterStartLine : currentOpenClusters) {
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

        }

        return finalClusters;
    }


    private Set<Cluster> filterValidClusters(Set<Cluster> blocks) {
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
            cluster.setAccessedVariables(accessedVariables);
            setMissingVarsForValidCluster(cluster);
        }
    }

    private void prepareClustersForRanking() {
        this.calculateBenefitOfClusters(this.filteredClusters);
        for (Cluster cluster : this.filteredClusters) {
            cluster.calculateClusterSize(this.statementStringsMap);
        }
    }

    // Call this method after filtering out the invalid clusters (and before ranking) to calculate the LCOM of
    // the valid clusters
    private void calculateBenefitOfClusters(Set<Cluster> clusters) {
        SortedMap<Integer, Set<Integer>> linePairs = buildLinePairs();
        int minKey = this.statementStringsMap.firstKey();
        int maxKey = this.statementStringsMap.lastKey();
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
        for (Integer thisLine : this.statementStringsMap.keySet()) {
            linePairs.put(thisLine, new HashSet<>());
            for (Integer otherLine : this.statementStringsMap.keySet()) {
                if (thisLine.equals(otherLine)) continue;
                for (String variable : this.statementStringsMap.get(thisLine)) {
                    if (this.statementStringsMap.get(otherLine).contains(variable)) {
                        linePairs.get(thisLine).add(otherLine);
                        break;
                    }
                }
            }
        }
        return linePairs;
    }


}
