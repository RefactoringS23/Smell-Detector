package cmu.csdetector.dummy.HourlyPayCalculator;

class HourlyEmployee {

    private final String id;
    private final String name;

    private int tenthRate;
    private int tenthsWorked;

    HourlyEmployee(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTenthRate() {
        return tenthRate;
    }

    public void setTenthRate(int tenthRate) {
        this.tenthRate = tenthRate;
    }

    public int getTenthsWorked() {
        return tenthsWorked;
    }

    public void setTenthsWorked(int tenthsWorked) {
        this.tenthsWorked = tenthsWorked;
    }

}
