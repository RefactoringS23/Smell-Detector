package cmu.csdetector.smells;

import cmu.csdetector.smells.detectors.BrainMethod;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import cmu.csdetector.smells.detectors.MessageChain;

public class MethodLevelSmellDetector extends CompositeSmellDetector {
	
	public MethodLevelSmellDetector() {

		addDetector(new MessageChain());
		addDetector(new FeatureEnvy());
		addDetector(new BrainMethod());
	}

	@Override
	protected SmellName getSmellName() {
		return null;
	}

}
