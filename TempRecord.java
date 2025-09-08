package main;

// global temperature observation
public class TempRecord {

    private final int year;
    private final double temp;

    // constructor
    public TempRecord(int year, double temp) {
        this.year = year;
        this.temp = temp;
    }

    // getters
    public int getYear() { return year; }
    public double getTemp() { return temp; }
}
