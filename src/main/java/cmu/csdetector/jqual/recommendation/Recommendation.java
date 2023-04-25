package cmu.csdetector.jqual.recommendation;

public abstract class Recommendation {

    private String operationType;
    public Recommendation(){

    }
    protected abstract String  getReadableString();
}
