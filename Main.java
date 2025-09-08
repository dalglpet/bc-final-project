package main;

import java.util.*;
import java.io.*;

// entry point and console menu
public class Main {

    private static AnalysisSession currentSession;
    private static final Scanner SC = new Scanner(System.in);

    // program entry
    public static void main(String[] args) {
        while (true) {
            printMenu();
            String choice = SC.nextLine().trim();
            if (choice.equals("1")) {
                createNewAnalysis();
            } else if (choice.equals("2")) {
                saveCurrent();
            } else if (choice.equals("3")) {
                loadExisting();
            } else if (choice.equals("4") || choice.equalsIgnoreCase("q")) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Unrecognized option. Choose 1, 2, 3, or 4.");
            }
        }
    }

    // menu printer
    private static void printMenu() {
        System.out.println();
        System.out.println("==== Climate Correlation Tool ====");
        System.out.println("1) Create new data analysis");
        System.out.println("2) Save current analysis");
        System.out.println("3) Load previous analyses");
        System.out.println("4) Quit");
        System.out.print("Select: ");
    }

    // create analysis
    private static void createNewAnalysis() {
        try {
            System.out.println();
            System.out.println("Enter path to the global temperature CSV (year,temp).");
            System.out.println("Press Enter to use sample_temps.csv.");
            String tempPath = SC.nextLine().trim();

            System.out.println("Enter path to the greenhouse gas CSV (year,gas,concentration).");
            System.out.println("Press Enter to use sample_gases.csv.");
            String gasPath = SC.nextLine().trim();

            if (tempPath.isEmpty() || gasPath.isEmpty()) {
                DatasetLoader.generateSampleFiles();
                tempPath = "sample_temps.csv";
                gasPath = "sample_gases.csv";
            }

            java.util.List<TempRecord> temps = DatasetLoader.readTemperatures(tempPath);
            java.util.List<GasRecord> gases = DatasetLoader.readGases(gasPath);
            currentSession = new AnalysisSession(tempPath, gasPath, temps, gases);
            currentSession.computeCorrelations();
            currentSession.printSummary();

            tryCreateOutputs(currentSession);
        } catch (IOException e) {
            System.out.println("Failed to load data. " + e.getMessage());
        }
    }

    // save summary
    private static void saveCurrent() {
        if (currentSession == null) {
            System.out.println("No current analysis. Create one first.");
            return;
        }
        System.out.println("Enter output path for summary (e.g., analysis.txt).");
        String out = SC.nextLine().trim();
        if (out.isEmpty()) {
            System.out.println("Path required.");
            return;
        }
        try {
            currentSession.save(out);
            System.out.println("Saved summary to: " + out);
        } catch (IOException e) {
            System.out.println("Save failed. " + e.getMessage());
        }
    }

    // load summary
    private static void loadExisting() {
        System.out.println("Enter path to a saved analysis file.");
        String in = SC.nextLine().trim();
        if (in.isEmpty()) {
            System.out.println("Path required.");
            return;
        }
        try {
            currentSession = AnalysisSession.load(in);
            currentSession.printSummary();
            tryCreateOutputs(currentSession);
        } catch (IOException e) {
            System.out.println("Load failed. " + e.getMessage());
        }
    }

    // outputs for charts  csv always  png via xchart or java2d fallback
    private static void tryCreateOutputs(AnalysisSession s) {
        try {
            java.util.Map<Integer, Double> tempSeries = new java.util.LinkedHashMap<>();
            for (TempRecord t : s.getTemperatures()) tempSeries.put(t.getYear(), t.getTemp());
            Visualization.saveTimeSeriesCsv("temps_timeseries.csv", tempSeries);
            boolean anyPng = Visualization.saveTimeSeriesPng("temps_timeseries.png", tempSeries, "Global Temperature", "Temperature");

            java.util.Map<String, java.util.List<GasRecord>> byGas = new java.util.LinkedHashMap<>();
            for (GasRecord g : s.getGases()) byGas.computeIfAbsent(g.getGas(), k -> new java.util.ArrayList<>()).add(g);

            java.util.Map<Integer, Double> tempByYear = new java.util.LinkedHashMap<>();
            for (TempRecord t : s.getTemperatures()) tempByYear.put(t.getYear(), t.getTemp());

            for (java.util.Map.Entry<String, java.util.List<GasRecord>> e : byGas.entrySet()) {
                String gas = e.getKey();
                java.util.List<GasRecord> rows = e.getValue();

                java.util.Map<Integer, Double> concSeries = new java.util.LinkedHashMap<>();
                java.util.List<Double> xs = new java.util.ArrayList<>();
                java.util.List<Double> ys = new java.util.ArrayList<>();
                for (GasRecord r : rows) {
                    concSeries.put(r.getYear(), r.getConcentration());
                    Double tv = tempByYear.get(r.getYear());
                    if (tv != null) { xs.add(r.getConcentration()); ys.add(tv); }
                }

                Visualization.saveTimeSeriesCsv(gas + "_timeseries.csv", concSeries);
                anyPng |= Visualization.saveTimeSeriesPng(gas + "_timeseries.png", concSeries, gas + " Concentration", "Concentration");
                Visualization.saveScatterCsv("temp_vs_" + gas + ".csv", xs, ys);
                anyPng |= Visualization.saveScatterPng("temp_vs_" + gas + ".png", xs, ys, "Temperature vs " + gas, gas + " Concentration", "Temperature");
            }

            if (!anyPng) {
                if (!Visualization.isXChartPresent()) {
                    System.out.println("Charts: XChart not detected on the runtime classpath. PNGs were not written.");
                    System.out.println("Tip: run with -cp out:xchart-3.8.8.jar so charts are saved.");
                } else {
                    System.out.println("Charts: XChart detected but PNG save calls did not succeed.");
                }
            } else {
                System.out.println("Charts: PNG files were written to the current folder.");
            }
        } catch (IOException ioe) {
            System.out.println("Chart file write failed. " + ioe.getMessage());
        }
    }
}
