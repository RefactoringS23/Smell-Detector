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
        if (target == null) {
            return "".toString();
        }
        StringBuilder s = new StringBuilder();
        s.append("MOVE METHOD:");
        s.append("\n");
        s.append("\t\tSource Class: ");
        s.append(parent.getBinding().getName() + "\n");
        s.append("\t\tMethod Name: ");
        s.append(method!= null ? method.getFullyQualifiedName() : cluster.getMethodName()+ "\n");
        s.append("\t\tTarget Class: ");
        s.append(target.getBinding().getName() + "\n");
        s.append("\n************************************************\n");

        return s.toString();
    };
}
