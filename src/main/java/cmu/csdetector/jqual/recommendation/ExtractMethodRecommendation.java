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
        // TODO: cluster.getMissingVars() is a Set<ASTNode>, need to convert to String
        return "EXTRACT METHOD: Inside class " + type.getFullyQualifiedName() + " Inside Method " + method.getFullyQualifiedName() + " from "
            + cluster.getStartLineNumber() + " to " + cluster.getEndLineNumber() + " with the extracted method name " + cluster.getClusterName()
            + " with parameters " + "TODO_____________" + " and return type " + cluster.getReturnType();
    }
    public List<Cluster> getTopRecommendations(){
            List<Cluster> l = new ArrayList<>();
            return l;
    }
    @Override
    public String toString() {
        return "ExtractMethodRecommendation{" +
                "type=" + type +
                ", method=" + method +
                ", cluster=" + cluster +
                '}';
    }
}
