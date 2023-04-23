package cmu.csdetector.heuristics;

import java.util.HashSet;
import java.util.Set;

import java.lang.Math;
import java.util.SortedMap;

import org.eclipse.jdt.core.dom.ASTNode;

public class ClusterRanking {

    private static final double THRESHOLD_SIZE_DIFFERENCE = 0.2;
    private static final double THRESHOLD_OVERLAPPING = 0.1;

    // Move to ClusterManager eventually


    public static void rankClusters(Set<Cluster> filteredClusters, SortedMap<Integer, HashSet<ASTNode>> table) {
        for (Cluster cluster : filteredClusters) {
            if (cluster.isAlternative() || (cluster.getClusterSize() == 0)) {
                continue;
            }
            for (Cluster otherCluster : filteredClusters) {
                if (!otherCluster.equals(cluster) && (otherCluster.getClusterSize() != 0)) {
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
        }
    }
    
    public static boolean notSimilarSize(Cluster primaryCluster, Cluster secondaryCluster) {
        Double difference = Math.abs(primaryCluster.getClusterSize() - secondaryCluster.getClusterSize())
                /Math.min(primaryCluster.getClusterSize(), secondaryCluster.getClusterSize());
        // TODO: according to the paper, should be 'return difference > THRESHOLD_SIZE_DIFFERENCE', but we don't get why
        return difference < THRESHOLD_SIZE_DIFFERENCE;
    }

    public static boolean significantOverlapping(Cluster primaryCluster, Cluster secondaryCluster) {
        double overlap = Math.min(primaryCluster.getEndLineNumber(), secondaryCluster.getEndLineNumber())
                - Math.max(primaryCluster.getStartLineNumber(), secondaryCluster.getStartLineNumber());
        Double percentageOverlapping = overlap
                /Math.max(primaryCluster.getClusterSize(), secondaryCluster.getClusterSize());
        return percentageOverlapping > THRESHOLD_OVERLAPPING;
    }

}

