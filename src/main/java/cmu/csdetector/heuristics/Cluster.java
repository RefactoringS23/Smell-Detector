package cmu.csdetector.heuristics;

import cmu.csdetector.resources.Type;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.Objects;

public class Cluster {
    private final ClusterLine startLine;
    private final ClusterLine endLine;
    private Set<ASTNode> accessedVariables;

    private double lcom;
    private double benefit;

    private double clusterSize;
    private Set<ASTNode> missingVars;
    private String returnType;
    private String methodName;
    private boolean isAlternative = false;

    private List<Cluster> alternatives;

    private Type parentClass;

    private String parentClassName;

    public Cluster(Integer startLine, Integer endLine) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.alternatives = new ArrayList<>();
        this.accessedVariables = new HashSet<>();
        this.missingVars = new HashSet<>();
        this.returnType = "void";
        this.methodName = "LeoIsTheBestProf";
    }

    public Cluster(Integer startLine, Integer endLine, Type parentClass) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.alternatives = new ArrayList<>();
        this.accessedVariables = new HashSet<ASTNode>();
        this.missingVars = new HashSet<>();
        this.parentClass = parentClass;
        this.parentClassName = parentClass.getBinding().getName();
    }

    public Type getParentClass() {
        return parentClass;
    }

    public void setParentClass(Type parentClass) {
        this.parentClass = parentClass;
    }


    public void setMissingVars(Set<ASTNode> missingVars) {
        this.missingVars = missingVars;
    }

    public void setReturnType(String returnType) { this.returnType = returnType; }

    public void setMethodName(String methodName) { this.methodName = methodName; }

    public Set<ASTNode> getMissingVars() {
        return this.missingVars;
    }

    public void calculateClusterSize(SortedMap<Integer, HashSet<String>> table) {
        double clusterSize = 0;
        for (int i = this.getStartLineNumber(); i <= this.getEndLineNumber(); i++) {
            if (table.containsKey(i)) {
                clusterSize += 1;
            }
        }
        this.clusterSize = clusterSize;
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

    public String getParentClassName() {
        return parentClassName;
    }

    public double getLcom() {
        return lcom;
    }

    public double calculateLcom (SortedMap<Integer, Set<Integer>> linePairs) {
        // p = number of pairs of statements that do not share variables
        // q = number of pairs of lines that share variables
        double p = 0, q = 0;

        for (int i = this.getStartLineNumber(); i <= this.getEndLineNumber(); i++) {
            Set<Integer> pairs = linePairs.get(i);
            if (pairs == null) {
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
        this.lcom = Math.max(p-q, 0);
        return this.lcom;
    }

    // Need cluster for the entire method
    public double calculateLcomOfMethodAfterRefactoring(Cluster method, SortedMap<Integer, Set<Integer>> linePairs) {
        double p = 0, q = 0;
        for (int i = method.getStartLineNumber(); i <= method.getEndLineNumber(); i++) {
            if (this.getStartLineNumber() <= i && i <= this.getEndLineNumber()) {
                i = this.getEndLineNumber();
                continue;
            }
            Set<Integer> pairs = linePairs.get(i);
            if (pairs == null) {
                continue;
            }
            for (int j = i + 1; j <= method.getEndLineNumber(); j++) {
                if (this.getStartLineNumber() <= j &&  j <= this.getEndLineNumber()) {
                    j = this.getEndLineNumber();
                    continue;
                }
                if (pairs.contains(j)) {
                    q++;
                } else {
                    p++;
                }
            }
        }
        return Math.max(p-q, 0);
    }
        

    public double getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(double clusterSize) {
        this.clusterSize = clusterSize;
    }

    // Call this only after calculating benefit 
    public double getBenefit() {
        return benefit;
    }

    public void calculateBenefit(Cluster method, SortedMap<Integer, Set<Integer>> linePairs) {
        double originalLcom = method.getLcom();
        double opportunityLcom = this.calculateLcom(linePairs);
        double methodLcomAfterRefactoring = this.calculateLcomOfMethodAfterRefactoring(method, linePairs);

        double benefit = originalLcom - Math.max(opportunityLcom, methodLcomAfterRefactoring);
        this.benefit = benefit;
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

    public void setAccessedVariables(Set<ASTNode> accessedVariables) {
        this.accessedVariables = accessedVariables;
    }

    public String getReturnType() { return this.returnType; }

    public String getMethodName() { return this.methodName; }
}
