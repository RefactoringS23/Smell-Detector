package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.metrics.MetricName;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Class to calculate the LCOM3 ‘lack of cohesion of methods’ metrics.
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
public class ChangingLCOM3Calculator extends BaseLCOM {
	private MethodDeclaration featureEnvyMethod;
	public ChangingLCOM3Calculator(MethodDeclaration type) {
		super();

		featureEnvyMethod = type;
	}

	@Override
	protected Double computeValue(ASTNode target) {
		// remove fmethod from src
		// fix simulation
		// ignore src

//		System.out.println(target.getParent());
//		System.out.println(featureEnvyMethod.getParent());

		if(target.getParent().equals(featureEnvyMethod.getParent())){
			System.out.println("llllllll");
			return 0d;
		}
		double before = calculateLCOM3BeforeMove(target);
		double after = calculateLCOM3AfterMove(target);
		System.out.println(before);
		System.out.println(after);
		if(after>before){
			return 0d;
		}
		return before-after;
	}

	private double calculateLCOM3BeforeMove(ASTNode target){
		boolean isPossibleLCOM = instantiateAttributes(target);	//call the method first in order to initialize the attributes

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
//		System.out.println(nMethods + " " + timesAccessedAttributes + " " + nAttributes );

		return (nMethods - timesAccessedAttributes/nAttributes) / (nMethods - 1);
	}
	private double calculateLCOM3AfterMove(ASTNode target){
		boolean isPossibleLCOM = simulateMoveMethod(target, this.featureEnvyMethod);	//call the method first in order to initialize the attributes

		if (!isPossibleLCOM){
			return 0.0;
		}
		System.out.println();

		/*
		 * If there are no more than one method in a class, LCOM3 is undefined.
		 * An undefined LCOM3 is displayed as zero.
		 */
		if(nMethods == 1){
			return 0.0;
		};

//		System.out.println("neww " + nMethods + " " + timesAccessedAttributes + " " + nAttributes );


		return (nMethods - timesAccessedAttributes/nAttributes) / (nMethods - 1);
	}
	@Override
	public MetricName getMetricName() {
		return MetricName.LCOM3Diff;
	}	

}
