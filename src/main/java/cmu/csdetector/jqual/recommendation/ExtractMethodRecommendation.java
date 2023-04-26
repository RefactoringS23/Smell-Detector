package cmu.csdetector.jqual.recommendation;

import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;

import java.util.ArrayList;
import java.util.List;

public class ExtractMethodRecommendation extends Recommendation {

    private Type type;
    private Method method;
    private Cluster cluster;

    public ExtractMethodRecommendation(Type type, Method method, Cluster cluster) {
        this.type = type;
        this.method = method;
        this.cluster = cluster;
    }

    public String getReadableString() {
        return "EXTRACT METHOD: \n " +
                "\t\tClass: " + type.getBinding().getName() + "\n" +
                "\t\tMethod: " + method.getBinding().getName() + "\n" +
                "\t\tLine Numbers:  " + cluster.getStartLineNumber() + " to " + cluster.getEndLineNumber() + "\n" +
                "\t\tMethod Name Suggestion: " + cluster.getMethodName()  + "\n" +
                "\t\tParameters Suggestion: " + cluster.getMissingVars() + "\n" +
                "\t\tReturn Type Suggestion: " + cluster.getReturnType() + "\n" +
                "\n************************************************\n";
    }

    @Override
    public String toString() {
        return "ExtractMethodRecommendation{" +
                "type=" + type +
                ", method=" + method +
                ", cluster=" + cluster +
                '}';
    }

    public Cluster getFinalCluster() {
        return cluster;
    }
}
