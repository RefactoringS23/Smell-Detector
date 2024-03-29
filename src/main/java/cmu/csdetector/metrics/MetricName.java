package cmu.csdetector.metrics;

public enum MetricName {
    CLOC("ClassLinesOfCode"),
    MLOC("MethodLinesOfCode"),
    CC("CyclomaticComplexity"),
    IsAbstract,
    MaxCallChain,
    ParameterCount,
    OverrideRatio,
    PublicFieldCount,
    TCC("TightClassCohesion"),
    MaxNesting,
    NOAV("NumberOfAccessedVariables"),
    NOAM("NumberOfAccessorMethods"),
    WMC("WeightedMethodCount"),
    WOC("WeighOfClass"),
    CINT("CouplingIntensity"),
    CDISP("CouplingDispersion"),
    ChangingClasses("ChangingClasses"),
    ChangingMethods("ChangingMethods"),
    IMC("Internal Method Calls"),
    EMC("External Method Calls"),
    LCOM("LackOfCohesionOfMethods"),
    LCOM2,
    LCOM3,
    LCOM3Diff("Difference in LCOM3"),

    JSC("JaccardSimilarityCoefficient");

    private String label;

    MetricName() {
        this.label = name();
    }

    MetricName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }


}
