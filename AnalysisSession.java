package main;

import java.util.*;
import java.io.*;

public class AnalysisSession {

    private final String tempCsvPath;
    private final String gasCsvPath;
    private final java.util.List<TempRecord> temperatures;
    private final java.util.List<GasRecord> gases;
    private final java.util.Map<String, Double> correlationByGas;

    // constructor
    public AnalysisSession(String tempCsvPath, String gasCsvPath,
                           java.util.List<TempRecord> temperatures, java.util.List<GasRecord> gases) {
        this.tempCsvPath = tempCsvPath;
        this.gasCsvPath = gasCsvPath;
        this.temperatures = temperatures;
        this.gases = gases;
        this.correlationByGas = new java.util.LinkedHashMap<>();
    }

    // compute pearson correlation per gas with year alignment
    public void computeCorrelations() {
        java.util.Map<Integer, Double> tempByYear = new java.util.TreeMap<>();
        for (TempRecord t : temperatures) tempByYear.put(t.getYear(), t.getTemp());

        java.util.Map<String, java.util.List<GasRecord>> byGas = new java.util.LinkedHashMap<>();
        for (GasRecord g : gases) byGas.computeIfAbsent(g.getGas(), k -> new java.util.ArrayList<>()).add(g);

        correlationByGas.clear();
        for (java.util.Map.Entry<String, java.util.List<GasRecord>> e : byGas.entrySet()) {
            String gas = e.getKey();
            java.util.List<GasRecord> rows = e.getValue();

            java.util.List<Double> xs = new java.util.ArrayList<>();
            java.util.List<Double> ys = new java.util.ArrayList<>();
            for (GasRecord r : rows) {
                Double tv = tempByYear.get(r.getYear());
                if (tv != null) { xs.add(tv); ys.add(r.getConcentration()); }
            }
            if (xs.size() >= 3) {
                double rr = Stats.pearson(xs, ys);
                correlationByGas.put(gas, rr);
            }
        }
    }

    // prints full sentences and punctuation
    public void printSummary() {
        if (correlationByGas.isEmpty()) {
            System.out.println("No correlations computed.");
            return;
        }
        System.out.println("Correlation results:");
        for (java.util.Map.Entry<String, Double> e : correlationByGas.entrySet()) {
            String gas = e.getKey();
            double r = e.getValue();
            System.out.printf("For %s, the Pearson correlation coefficient (r) is %+.4f.%n", gas, r);
            System.out.println(interpretCorrelation(r));
        }
    }

    // interpretation helper
    private String interpretCorrelation(double r) {
        double a = Math.abs(r);
        String dir = r > 0 ? "positive" : (r < 0 ? "negative" : "no direction");
        if (a < 0.05) return "Interpretation: no linear relationship detected based on aligned years.";
        String strength;
        if (a >= 0.90) strength = "very strong";
        else if (a >= 0.70) strength = "strong";
        else if (a >= 0.50) strength = "moderate";
        else if (a >= 0.30) strength = "weak";
        else strength = "very weak";
        return "Interpretation: " + strength + " " + dir + " linear relationship between gas concentration and temperature.";
    }

    // save summary
    public void save(String outPath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outPath))) {
            bw.write("TEMP_FILE=" + tempCsvPath);
            bw.newLine();
            bw.write("GAS_FILE=" + gasCsvPath);
            bw.newLine();
            for (java.util.Map.Entry<String, Double> e : correlationByGas.entrySet()) {
                bw.write("CORR," + e.getKey() + "," + e.getValue());
                bw.newLine();
            }
        }
    }

    // load summary
    public static AnalysisSession load(String inPath) throws IOException {
        String tempPath = null;
        String gasPath = null;
        java.util.Map<String, Double> corrs = new java.util.LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("TEMP_FILE=")) tempPath = line.substring(10).trim();
                else if (line.startsWith("GAS_FILE=")) gasPath = line.substring(9).trim();
                else if (line.startsWith("CORR,")) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String gas = parts[1].trim();
                        Double r = Double.parseDouble(parts[2].trim());
                        corrs.put(gas, r);
                    }
                }
            }
        }
        if (tempPath == null || gasPath == null) throw new IOException("summary file missing required lines");
        java.util.List<TempRecord> temps = DatasetLoader.readTemperatures(tempPath);
        java.util.List<GasRecord> gases = DatasetLoader.readGases(gasPath);
        AnalysisSession s = new AnalysisSession(tempPath, gasPath, temps, gases);
        for (java.util.Map.Entry<String, Double> e : corrs.entrySet()) s.correlationByGas.put(e.getKey(), e.getValue());
        return s;
    }

    // getters
    public java.util.Map<String, Double> getCorrelationByGas() { return correlationByGas; }
    public java.util.List<TempRecord> getTemperatures() { return temperatures; }
    public java.util.List<GasRecord> getGases() { return gases; }
    public String getTempCsvPath() { return tempCsvPath; }
    public String getGasCsvPath() { return gasCsvPath; }
}
