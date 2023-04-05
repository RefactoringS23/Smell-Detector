package cmu.csdetector.metrics.calculators.method;

import cmu.csdetector.ast.visitors.ClassMethodInvocationVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.Map;

/**
 * InternalMethodCallsCalculator calculates the number of internal calls that a method makes.
 *
 * It uses the ClassMethodInvocationVisitor which gives a map of the number of calls the method
 * makes to any class. The calculator iterates over the map and finds the number of call to the
 * class that declares the method which is under consideration.
 *
 * It returns the number of calls made to the class that the method is declared in and if there
 * are no internal calls, it returns 0.
 */
public class InternalMethodCallsCalculator extends MetricValueCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        MethodDeclaration method = (MethodDeclaration) target;
        IMethodBinding methodBinding = method.resolveBinding();
        ITypeBinding type = methodBinding.getDeclaringClass();

        ClassMethodInvocationVisitor visitor = new ClassMethodInvocationVisitor(type);
        target.accept(visitor);
        Map<ITypeBinding, Integer> methodCallMap = visitor.getMethodsCalls();

        for(ITypeBinding binding : methodCallMap.keySet()) {
            if (binding.getName().equals(type.getName())){
                return Double.valueOf(methodCallMap.get(binding));
            }
        }
        return 0.0;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.IMC;
    }
}
