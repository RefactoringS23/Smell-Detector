package cmu.csdetector.metrics.calculators.method;

import cmu.csdetector.ast.visitors.ClassMethodInvocationVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.Map;

/**
 * ExternalMethodCallsCalculator calculates the highest number of external calls that a method makes
 * to a particular class.
 *
 * It uses the ClassMethodInvocationVisitor which gives a map of the number of calls the method
 * makes to any class. The calculator iterates over the map and finds the maximum value for the
 * number of call to an external class. This cannot be the class that declares the method under
 * consideration.
 *
 * It returns the maximum number of calls made to the external class (not sum of all external calls)
 * and if there are no external calls, it returns 0.
 *
 * The maximum value is returned as this metric is only used to compute feature envy and in order to
 * detect feature envy comparison with the highest value will be sufficient.
 */
public class ExternalMethodCallsCalculator extends MetricValueCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        MethodDeclaration method = (MethodDeclaration) target;
        IMethodBinding methodBinding = method.resolveBinding();
        ITypeBinding type = methodBinding.getDeclaringClass();

        ClassMethodInvocationVisitor visitor = new ClassMethodInvocationVisitor(type);
        target.accept(visitor);
        Map<ITypeBinding, Integer> methodCallMap = visitor.getMethodsCalls();

        Integer highestExternalMethodCallCount = 0;
        for(ITypeBinding binding : methodCallMap.keySet()) {
            if (!binding.getName().equals(type.getName()) && methodCallMap.get(binding)>highestExternalMethodCallCount){
                highestExternalMethodCallCount = methodCallMap.get(binding);
            }
        }

        return Double.valueOf(highestExternalMethodCallCount);
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.EMC;
    }
}
