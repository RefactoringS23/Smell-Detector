package cmu.csdetector.heuristics;

import java.util.HashSet;
import java.util.Set;

import java.lang.Math;
import java.util.SortedMap;


public class ClusterRanking {

    private static final double THRESHOLD_SIZE_DIFFERENCE = 0.2;
    private static final double THRESHOLD_OVERLAPPING = 0.5;

    public static Set<Cluster> rankClusters(Set<Cluster> filteredClusters) {

        for (Cluster cluster : filteredClusters) {
            if (cluster.isAlternative() || (cluster.getClusterSize() == 0)) {
                continue;
            }
            for (Cluster otherCluster : filteredClusters) {
                if (!otherCluster.equals(cluster) && (otherCluster.getClusterSize() != 0))
                    if (!otherCluster.isAlternative() && notSimilarSize(cluster, otherCluster)
                            && significantOverlapping(cluster, otherCluster)) {
                        if (cluster.getBenefit() > otherCluster.getBenefit()) {
                            cluster.addNewAlternativeCluster(otherCluster);
                            otherCluster.setAlternative(true);
                        } else {
                            otherCluster.addNewAlternativeCluster(cluster);
                            cluster.setAlternative(true);
                        }
                    }
                }
            }
        return new HashSet<>();
    }

    public static boolean notSimilarSize(Cluster primaryCluster, Cluster secondaryCluster) {
        Double difference = Math.abs(primaryCluster.getClusterSize() - secondaryCluster.getClusterSize())
                /Math.min(primaryCluster.getClusterSize(), secondaryCluster.getClusterSize());
        return difference > THRESHOLD_SIZE_DIFFERENCE;
    }

    public static boolean significantOverlapping(Cluster primaryCluster, Cluster secondaryCluster) {
        double overlap = Math.max(primaryCluster.getStartLineNumber(), secondaryCluster.getStartLineNumber())
                - Math.min(primaryCluster.getEndLineNumber(), secondaryCluster.getEndLineNumber());
        Double percentageOverlapping = overlap
                /Math.max(primaryCluster.getClusterSize(), secondaryCluster.getClusterSize());
        return percentageOverlapping > THRESHOLD_OVERLAPPING;
    }

    private static double calculateLCOM2(ClusterLine startLine, ClusterLine endLine, SortedMap<Integer, HashSet<String>> table) {
        for (Integer currentLine : table.keySet()) {
            Set<String> row = table.get(currentLine);
//            int currentEndLine = currentLine + stepSize;
        }

        return 0.0;
    }

}

