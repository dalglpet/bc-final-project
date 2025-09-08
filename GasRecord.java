package main;

// greenhouse gas observation
public class GasRecord {

    private final int year;
    private final String gas;
    private final double concentration;

    // constructor
    public GasRecord(int year, String gas, double concentration) {
        this.year = year;
        this.gas = gas;
        this.concentration = concentration;
    }

    // getters
    public int getYear() { return year; }
    public String getGas() { return gas; }
    public double getConcentration() { return concentration; }
}
