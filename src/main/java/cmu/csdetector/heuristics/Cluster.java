package cmu.csdetector.heuristics;

import java.util.*;

public class Cluster {
    private final ClusterLine startLine;
    private final ClusterLine endLine;

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

    public static Set<Cluster> makeClusters(SortedMap<Integer, HashSet<String>> table) {
        Set<Cluster> clusters = new HashSet<>();
        int stepSize = 1;
        int methodSize = table.lastKey();
        
        while (stepSize < methodSize) {
            for (Integer currentLine : table.keySet()) {
                Set<String> row = table.get(currentLine);
                int currentEndLine = currentLine + stepSize;

                if (table.containsKey(currentEndLine)) {
                    for (String variableOrMethodCall : row) {
                        if (table.get(currentEndLine).contains(variableOrMethodCall)) {
                            clusters.add(new Cluster(currentLine, currentEndLine));
                            break;
                        }
                    }
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
            
            //Integer iterCount = 0;
            for (ClusterLine line : sortedLines) {
                //System.out.println("Starting for loop " + iterCount);
                if (line.getIsStart()) {
                    //System.out.println("Iteration: " + iterCount + " => " + line + " is start for ");
                    for (ClusterLine openClusterStartLine : currentOpenClusters) {
                        newClusters.add(new Cluster(openClusterStartLine.getLineNumber(),
                                                    line.getCluster().getEndLineNumber()));
                    }
                    currentOpenClusters.add(line);
                } else {
                    //System.out.println("Iteration: " + iterCount + " => " + line + " is end");
                    currentOpenClusters.remove(line.getCluster().getStartLine());
                }
                //iterCount++;
            }
            mergeCandidates = newClusters;
            finalClusters.addAll(newClusters);
            newClusters = new HashSet<>();

        } while (mergeCandidates.size() > 0);

        return finalClusters;
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
}
