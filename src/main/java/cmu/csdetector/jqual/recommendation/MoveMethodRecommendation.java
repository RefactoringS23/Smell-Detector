package cmu.csdetector.jqual.recommendation;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;

public class MoveMethodRecommendation extends Recommendation {
    private Type parent;
    private Method method;
    private Type target;

    public MoveMethodRecommendation(Type parentClass, Method m, Type targetClass) {
        parent = parentClass;
        method = m;
        target = targetClass;
    }

    public String getReadableString(){
        StringBuilder s = new StringBuilder();
        s.append("MOVE METHOD:");
        s.append("Source Class - ");
        s.append(parent.getFullyQualifiedName() + " | ");
        s.append("Method Name -");
        s.append(method.getFullyQualifiedName() + " | ");
        s.append("Target Class -");
        s.append(target);
        return s.toString();
    };
}
