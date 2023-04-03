package cmu.csdetector.util;

import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollectorForFeatureEnvyMethod;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.ClassLevelSmellDetector;
import cmu.csdetector.smells.MethodLevelSmellDetector;
import cmu.csdetector.smells.Smell;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

public class GenericCollector {

	public static void collectTypeMetricValues(Type type) {
		TypeMetricValueCollector collector = new TypeMetricValueCollector();
		collector.collect(type);
	}

	public static void collectTypeAndMethodsMetricValues(Type type) {
		TypeMetricValueCollector collector = new TypeMetricValueCollector();
		collector.collect(type);
		for (Method method : type.getMethods()) {
			MethodMetricValueCollector mColl = new MethodMetricValueCollector();
			mColl.collect(method);
		}
	}

	public static void collectTypeMetricsForFeatureEnvyMethod(List<Type> types, MethodDeclaration featureEnvyMethod) {
		for (Type type : types) {
			TypeMetricValueCollectorForFeatureEnvyMethod collector = new TypeMetricValueCollectorForFeatureEnvyMethod(featureEnvyMethod);
			collector.collect(type);
		}
	}

	public static void detectSmell(Type type) {
		// It is important to detect certain smells at method level first, such as Brain Method
		MethodLevelSmellDetector methodLevelSmellDetector = new MethodLevelSmellDetector();

		for(Method method : type.getMethods()) {
			List<Smell> smells = methodLevelSmellDetector.detect(method);
			method.addAllSmells(smells);
		}

		// Some class-level smell detectors rely on method-level smells as part of their detection
		ClassLevelSmellDetector classLevelSmellDetector = new ClassLevelSmellDetector();
		List<Smell> smells = classLevelSmellDetector.detect(type);
		type.addAllSmells(smells);
	}

	public static void collectAll(List<Type> types) {
		for (Type type: types) {
			collectTypeMetricValues(type);
			collectTypeAndMethodsMetricValues(type);
//			detectSmell(type);
		}
	}

}
