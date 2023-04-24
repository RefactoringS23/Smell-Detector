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
    private Set<ASTNode> accessedVariables;
    private Map<String, List<Integer>> accessedVariables1;
    private static Map<String, List<Integer>> assignedVariables;

    private double lcom;
    private double benefit;

    private double clusterSize;
    private Set<String> missingVars;

    private boolean isAlternative = false;

    private List<Cluster> alternatives;


    public Cluster(Integer startLine, Integer endLine) {
        this.startLine = new ClusterLine(startLine, this, true);
        this.endLine = new ClusterLine(endLine, this, false);
        this.alternatives = new ArrayList<>();
        this.accessedVariables = new HashSet<>();
        this.missingVars = new HashSet<>();
    }

    public void setMissingVars(Set<String> missingVars) {
        this.missingVars = missingVars;
    }

    public Set<String> getMissingVars() {
        return this.missingVars;
    }

    public void calculateClusterSize(SortedMap<Integer, HashSet<ASTNode>> table) {
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

    public Map<String, List<Integer>> getAccessedVariables1() {
        return accessedVariables1;
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
        this.lcom = p - q;
        if (this.lcom < 0) this.lcom = 0;
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
        double lcom = p - q;
        if (lcom < 0) lcom = 0;
        return lcom;
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

    private Map<String, List<Integer>>  getListOfAccessedVariables1(SortedMap<Integer, HashSet<String>> table) {
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
        return access;
    }

    public String getReturnValue(Map<String, List<Integer>> assignmentVariables, SortedMap<Integer, HashSet<String>> visitorTable) {
        String returnType = "void";
        Integer count = 0;

        Map<String, List<Integer>> variablesAccessedInClass = getListOfAccessedVariables1(visitorTable);
        for (String node: assignmentVariables.keySet()) {
            List<Integer> indexList = variablesAccessedInClass.get(node);
            int insideCluster = 0;
            int afterCluster = 0;
            if(indexList != null) {
                for( int ind : indexList) {
                    if (ind >= startLine.getLineNumber() && ind <= endLine.getLineNumber()) {
                        insideCluster += 1;
                    } else if (ind > endLine.getLineNumber()) {
                        afterCluster += 1;
                    }
                }
            }
            if (insideCluster > 0 && afterCluster > 0) {
                returnType = node;
                count += 1;
            }
        }
        if (count>1) {
            returnType = "invalid";
        }
        System.out.println("return value");
        System.out.println(startLine.getLineNumber());
        System.out.println(endLine.getLineNumber());
        System.out.println(returnType);
        System.out.println(" ");
        return returnType;
    }

    public String getReturnType(Map<String, String> nodeTypeMap, String returnValue) {
        if (returnValue != "void" && returnValue != "invalid") {
            return nodeTypeMap.get(returnValue);
        }
        else {
            return returnValue;
        }
    }

    public String getMethodName(String returnValue, int i) {
        String name = String.join("","LeoIsTheBestProf", String.valueOf(i));
        if (returnValue != "void" && returnValue != "invalid") {
            name = String.join("","get",returnValue);
        }
        return name;
    }

    public static void setAssignedNodes(Map<String, List<Integer>> varsAssigned){;
        assignedVariables = varsAssigned;
    }
}
