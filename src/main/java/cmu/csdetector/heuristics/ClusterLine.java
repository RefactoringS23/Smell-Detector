package cmu.csdetector.heuristics;


public class ClusterLine implements Comparable<ClusterLine> {
    private final Integer lineNumber;
    private final Cluster cluster;
    private final Boolean isStart;

    public ClusterLine(Integer lineNumber, Cluster cluster, Boolean isStart) {
        this.lineNumber = lineNumber;
        this.cluster = cluster;
        this.isStart = isStart;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public Integer getLineNumber() {
        return this.lineNumber;
    }
    
    public Boolean getIsStart() {
        return this.isStart;
    }

    @Override
    public int compareTo(ClusterLine otherLine) {
        if (this.lineNumber < otherLine.lineNumber) {
            return -1;
        } else if (this.lineNumber > otherLine.lineNumber) {
            return 1;
        } else if (this.isStart && !otherLine.isStart) {
            return -1;
        } else if (!this.isStart && otherLine.isStart) {
            return 1;
        } else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return "("+this.lineNumber.toString()+", " + this.isStart.toString()+")";
    }
}
