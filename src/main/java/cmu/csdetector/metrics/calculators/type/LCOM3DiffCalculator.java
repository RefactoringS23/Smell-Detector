package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.metrics.MetricName;
import org.eclipse.jdt.core.dom.*;

/**
 * Class to calculate the LCOM3 D‘lack of cohesion of methods’ metrics.
 * 
 * The equation to calculate the LCOM2 is:
 * 		m = #declaredMethods(C)
 * 		a = #declaredAttributes(C)
 * 		m(A) = # of methods in C that reference attribute A
 * 		s = sum(m(A)) for A in declaredAttributes(C)
 * 		LCOM3(C) = (m - s/a)/(m - 1)
 * 
 * Observation = timesAccessedAttributes comprises the s
 * 
 * The metric definition as well as its implementation are available at:
 * http://www.aivosto.com/project/help/pm-oo-cohesion.html and
 * http://www.cs.sjsu.edu/~pearce/cs251b/DesignMetrics.htm
 * 
 * @author leonardo
 */
public class LCOM3DiffCalculator extends BaseLCOM {
	private MethodDeclaration featureEnvyMethod;
	private Cluster extractedMethod;

	private String featureEnvyParentClass;
	public LCOM3DiffCalculator(MethodDeclaration type) {
		super();
		featureEnvyMethod = type;
		featureEnvyParentClass =  ((TypeDeclaration) featureEnvyMethod.getParent()).getName().toString();
	}

	public LCOM3DiffCalculator(Cluster type) {
		super();
		extractedMethod = type;
		featureEnvyParentClass = type.getParentClassName();
	}

	@Override
	protected Double computeValue(ASTNode target) {
		TypeDeclaration type = (TypeDeclaration) target;
		if(type.getName().toString().equals(featureEnvyParentClass)){
			return 0d;
		}
		double before = calculateLCOM3BeforeMove(target);
		double after = calculateLCOM3AfterMove(target);
		return before-after;
	}

	private double calculateLCOM3BeforeMove(ASTNode target){
		//call the method first in order to initialize the attributes
		boolean isPossibleLCOM = instantiateAttributes(target);

		if (!isPossibleLCOM){
			return 0.0;
		}

		/*
		 * If there are no more than one method in a class, LCOM3 is undefined.
		 * An undefined LCOM3 is displayed as zero.
		 */
		if(nMethods == 1){
			return 0.0;
		}
		return (nMethods - timesAccessedAttributes/nAttributes) / (nMethods - 1);
	}

	private double calculateLCOM3AfterMove(ASTNode target){
		boolean isPossibleLCOM;
		if(this.extractedMethod != null){
			//call the method first in order to initialize the attributes
			isPossibleLCOM = simulateMoveMethod(target, this.extractedMethod.getAccessedVariables());
		} else {
			//call the method first in order to initialize the attributes
			isPossibleLCOM = simulateMoveMethod(target, this.featureEnvyMethod);

		}

		if (!isPossibleLCOM){
			return 0.0;
		}

		/*
		 * If there are no more than one method in a class, LCOM3 is undefined.
		 * An undefined LCOM3 is displayed as zero.
		 */
		if(nMethods == 1){
			return 0.0;
		}

		return (nMethods - timesAccessedAttributes/nAttributes) / (nMethods - 1);
	}
	@Override
	public MetricName getMetricName() {
		return MetricName.LCOM3Diff;
	}	

}
