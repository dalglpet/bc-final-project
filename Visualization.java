package main;


import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.*;

// chart helpers using XChart only
public class Visualization {

    // write time series csv
    public static void saveTimeSeriesCsv(String outputCsvPath, Map<Integer, Double> yearToValue) throws java.io.IOException {
        List<Integer> years = new ArrayList<>(yearToValue.keySet());
        Collections.sort(years);
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(outputCsvPath))) {
            pw.println("year,value");
            for (int y : years) {
                pw.printf("%d,%.6f%n", y, yearToValue.get(y));
            }
        }
    }

    // write scatter csv
    public static void saveScatterCsv(String outputCsvPath, List<Double> xValues, List<Double> yValues) throws java.io.IOException {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(outputCsvPath))) {
            pw.println("x,y");
            int n = Math.min(xValues.size(), yValues.size());
            for (int i = 0; i < n; i++) {
                pw.printf("%.6f,%.6f%n", xValues.get(i), yValues.get(i));
            }
        }
    }

    // save time series chart as png using XChart
    public static boolean saveTimeSeriesPng(String outputPngPath, Map<Integer, Double> yearToValue, String chartTitle, String yAxisLabel) {
        try {
            List<Integer> years = new ArrayList<>(yearToValue.keySet());
            Collections.sort(years);

            double[] xs = new double[years.size()];
            double[] ys = new double[years.size()];
            for (int i = 0; i < years.size(); i++) {
                xs[i] = years.get(i);
                ys[i] = yearToValue.get(years.get(i));
            }

            XYChart chart = new XYChartBuilder()
                    .title(chartTitle)
                    .xAxisTitle("Year")
                    .yAxisTitle(withUnitsY(yAxisLabel, chartTitle))
                    .width(900).height(540)
                    .build();

            XYSeries series = chart.addSeries("Series", xs, ys);
            series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            series.setMarker(SeriesMarkers.CIRCLE);

            String base = outputPngPath.endsWith(".png")
                    ? outputPngPath.substring(0, outputPngPath.length() - 4)
                    : outputPngPath;
            BitmapEncoder.saveBitmap(chart, base, BitmapFormat.PNG);
            return true;
        } catch (Exception ex) {
            System.out.println("chart save failed " + ex.getMessage());
            return false;
        }
    }

    // save scatter chart as png using XChart
    public static boolean saveScatterPng(String outputPngPath, List<Double> xValues, List<Double> yValues,
                                         String chartTitle, String xAxisLabel, String yAxisLabel) {
        try {
            int n = Math.min(xValues.size(), yValues.size());
            double[] xs = new double[n];
            double[] ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = xValues.get(i);
                ys[i] = yValues.get(i);
            }

            XYChart chart = new XYChartBuilder()
                    .title(chartTitle)
                    .xAxisTitle(withUnitsX(xAxisLabel))
                    .yAxisTitle(withUnitsY(yAxisLabel, chartTitle))
                    .width(900).height(540)
                    .build();

            XYSeries series = chart.addSeries("Points", xs, ys);
            series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
            series.setMarker(SeriesMarkers.CIRCLE);

            String base = outputPngPath.endsWith(".png")
                    ? outputPngPath.substring(0, outputPngPath.length() - 4)
                    : outputPngPath;
            BitmapEncoder.saveBitmap(chart, base, BitmapFormat.PNG);
            return true;
        } catch (Exception ex) {
            System.out.println("chart save failed " + ex.getMessage());
            return false;
        }
    }

    // helper for y axis units
    private static String withUnitsY(String yAxisLabel, String title) {
        String yl = yAxisLabel == null ? "" : yAxisLabel;
        if (yl.toLowerCase().contains("temp")) return "Temperature (°C)";
        if (title != null && title.toUpperCase().contains("CO2")) return yl + " (ppm)";
        if (title != null && title.toUpperCase().contains("CH4")) return yl + " (ppm)";
        return yl;
    }

    // helper for x axis units
    private static String withUnitsX(String xAxisLabel) {
        String xl = xAxisLabel == null ? "" : xAxisLabel;
        if (xl.toUpperCase().contains("CO2")) return xl + " (ppm)";
        if (xl.toUpperCase().contains("CH4")) return xl + " (ppm)";
        return xl;
    }

    // simple presence check for XChart
    public static boolean isXChartPresent() {
        try {
            Class.forName("org.knowm.xchart.XYChart");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}