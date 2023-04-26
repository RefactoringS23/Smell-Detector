package cmu.csdetector.dummy.HourlyPayCalculator;

public class HourlyPayCalculator {

    private int tenthRate;
    private int tenthsWorked;

    private HourlyPayCalculator(CalculatorBuilder builder) {
        this.tenthRate = builder.tenthRate;
        this.tenthsWorked = builder.tenthsWorked;
    }

    public static class CalculatorBuilder {

        private final int tenthRate;

        public CalculatorBuilder(int tenthRate, int tenthsWorked) {
            this.tenthRate = tenthRate;
            this.tenthsWorked = tenthsWorked;
        }

        private final int tenthsWorked;

        /* may have other setters for optional variables */

        public HourlyPayCalculator build() {
            return new HourlyPayCalculator(this);
        }
    }

    private int calculateStraightTime() {
        return Math.min(400, this.tenthRate);
    }

    private int calculateOverTime(int straightTime) {
        return Math.max(0, tenthsWorked - straightTime);
    }

    private int calculateStraightPay(int straightTime) {
        return straightTime * tenthRate;
    }

    private int calculateOverTimePay(int overTime) {
        return (int) Math.round(overTime * tenthRate * 1.5);
    }

    public Money calculateWeeklyPay() {
        int straightTime = calculateStraightTime();
        int overTime = calculateOverTime(straightTime);
        return new Money(calculateStraightPay(straightTime) + calculateOverTimePay(overTime));
    }

    public static void main(String[] args) {

        HourlyEmployee e = getHourlyEmployee();

        HourlyPayCalculator calculator = new CalculatorBuilder(e.getTenthRate(), e.getTenthsWorked()).build();
        System.out.println(e.getName() + " gets " + calculator.calculateWeeklyPay() + " this week.");
    }

    private static HourlyEmployee getHourlyEmployee() {
        HourlyEmployee e = new HourlyEmployee("1001", "Jimmy Lu");
        e.setTenthRate(30);
        e.setTenthsWorked(20);
        return e;
    }

}
