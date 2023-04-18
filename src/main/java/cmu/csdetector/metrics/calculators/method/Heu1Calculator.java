package cmu.csdetector.metrics.calculators.method;

import cmu.csdetector.ast.visitors.BlockLineNumberVisitor;
import cmu.csdetector.ast.visitors.CyclomaticComplexityVisitor;
import cmu.csdetector.ast.visitors.StatementObjectsVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.ASTNode;

public class Heu1Calculator extends MetricValueCalculator {
    @Override
    protected Double computeValue(ASTNode target) {
        StatementObjectsVisitor visitor = new StatementObjectsVisitor();
        target.accept(visitor);
        BlockLineNumberVisitor visitor1 = new BlockLineNumberVisitor();
        target.accept(visitor1);
        System.out.println(visitor.getHeuristicMap());
        System.out.println(visitor1.getLineMap());
        return 0.2;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.CC;
    }

    @Override
    public boolean shouldComputeAggregate() {
        return true;
    }
}
