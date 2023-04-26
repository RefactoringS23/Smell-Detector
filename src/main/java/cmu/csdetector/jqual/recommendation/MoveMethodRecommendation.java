package cmu.csdetector.jqual.recommendation;

import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;

public class MoveMethodRecommendation extends Recommendation {
    private Type parent;
    private Method method;

    private Cluster cluster;
    private Type target;

    public MoveMethodRecommendation(Type parentClass, Method m, Type targetClass) {
        parent = parentClass;
        method = m;
        target = targetClass;
    };

    public MoveMethodRecommendation(Type parentClass, Cluster c, Type targetClass) {
        parent = parentClass;
        cluster = c;
        target = targetClass;
    }

    public String getReadableString(){
        StringBuilder s = new StringBuilder();
        s.append("MOVE METHOD:");
        s.append("\n");
        s.append("Source Class: ");
        s.append(parent.getBinding().getName()).append(" | ");
        s.append("Method Name: ");
        s.append(method!= null ? method.getFullyQualifiedName() : cluster.getMethodName());
        s.append(" | Target Class: ");
        s.append(target.getBinding().getName());
        s.append("\n********************************************\n");

        return s.toString();
    };
}
