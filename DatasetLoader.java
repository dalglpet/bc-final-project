package main;

import java.util.*;
import java.io.*;

// csv loading and sample generation
public class DatasetLoader {

    // read temperature csv
    // accepts headers like: year,temp | Year,Temperature | date,anomalyC
    // delimiters: comma, semicolon, tab
    // units: °C expected; if values look like Kelvin (> 200), convert to °C
    public static java.util.List<TempRecord> readTemperatures(String path) throws IOException {
        List<TempRecord> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String first = skipPreamble(br);
            if (first == null) return rows;

            String delim = detectDelimiter(first);
            String[] header = split(first, delim);
            Map<String,Integer> idx = headerIndex(header);

            Integer iYear = find(header, idx, new String[]{"year","date"});
            Integer iTemp = find(header, idx, new String[]{"temp","temperature","anomalyc","anomaly","temp_c","tc"});

            if (iYear == null || iTemp == null) {
                throw new IOException("could not locate year and temperature columns in " + path);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (isSkippable(line)) continue;
                String[] p = split(line, delim);
                if (p.length <= Math.max(iYear, iTemp)) continue;

                Integer year = parseYear(p[iYear]);
                Double tVal = parseDoubleSafe(p[iTemp]);
                if (year == null || tVal == null) continue;

                // heuristic Kelvin to Celsius
                if (tVal > 200.0) tVal = tVal - 273.15;

                rows.add(new TempRecord(year, tVal));
            }
        }
        rows.sort(Comparator.comparingInt(TempRecord::getYear));
        return rows;
    }

    // read gas csv
    // accepts headers like: year,gas,concentration | date,species,value
    // units:
    //  - ppm expected
    //  - if header mentions ppb, convert ppb -> ppm
    //  - if header mentions mole_fraction (fraction), convert fraction -> ppm
    // delimiters: comma, semicolon, tab
    public static java.util.List<GasRecord> readGases(String path) throws IOException {
        List<GasRecord> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String first = skipPreamble(br);
            if (first == null) return rows;

            String delim = detectDelimiter(first);
            String[] header = split(first, delim);
            Map<String,Integer> idx = headerIndex(header);

            Integer iYear = find(header, idx, new String[]{"year","date"});
            Integer iGas  = find(header, idx, new String[]{"gas","gastype","species","name"});
            Integer iConc = find(header, idx, new String[]{"concentration","value","ppm","ppb","mole_fraction","fraction"});

            if (iYear == null || iGas == null || iConc == null) {
                throw new IOException("could not locate year, gas, and concentration columns in " + path);
            }

            boolean headerIsPpb   = header[iConc].toLowerCase().contains("ppb");
            boolean headerIsPpm   = header[iConc].toLowerCase().contains("ppm");
            boolean headerIsFrac  = header[iConc].toLowerCase().contains("fraction");

            String line;
            while ((line = br.readLine()) != null) {
                if (isSkippable(line)) continue;
                String[] p = split(line, delim);
                if (p.length <= Math.max(Math.max(iYear, iGas), iConc)) continue;

                Integer year = parseYear(p[iYear]);
                String gas   = p[iGas].trim();
                Double conc  = parseDoubleSafe(p[iConc]);
                if (year == null || gas.isEmpty() || conc == null) continue;

                // normalize to ppm
                if (headerIsPpb) conc = conc / 1000.0;
                else if (headerIsFrac) conc = conc * 1_000_000.0;
                else if (!headerIsPpm) {
                    // heuristic: if numbers are large (~hundreds), likely ppm already
                    // if tiny (< 0.01), assume fraction -> ppm
                    if (conc < 0.01) conc = conc * 1_000_000.0;
                }

                rows.add(new GasRecord(year, gas, conc));
            }
        }
        rows.sort(Comparator.comparing(GasRecord::getGas).thenComparingInt(GasRecord::getYear));
        return rows;
    }

    // sample generators remain the same
    public static void generateSampleFiles() {
        try (PrintWriter tpw = new PrintWriter(new FileWriter("sample_temps.csv"));
             PrintWriter gpw = new PrintWriter(new FileWriter("sample_gases.csv"))) {

            tpw.println("year,temp");
            for (int y = 1995; y <= 2008; y++) {
                double trend = -0.10 + 0.019 * (y - 1995);
                double noise = ((y * 37) % 13 - 6) / 500.0;
                double temp = trend + noise;
                tpw.printf("%d,%.3f%n", y, temp);
            }

            gpw.println("year,gas,concentration");
            for (int y = 1993; y <= 2008; y++) {
                double co2Trend = 360 + 1.85 * (y - 1995);
                double co2Noise = ((y * 23) % 11 - 5) / 10.0;
                double co2 = co2Trend + co2Noise;
                gpw.printf("%d,CO2,%.2f%n", y, co2);
            }
            for (int y = 1997; y <= 2008; y++) {
                double ch4Trend = 1.70 + 0.007 * (y - 1997);
                double ch4Noise = ((y * 29) % 9 - 4) / 100.0;
                double ch4 = ch4Trend + ch4Noise;
                gpw.printf("%d,CH4,%.3f%n", y, ch4);
            }
        } catch (IOException e) {
            System.out.println("could not generate sample files " + e.getMessage());
        }
    }

    // helpers
    private static String skipPreamble(BufferedReader br) throws IOException {
        String ln;
        while ((ln = br.readLine()) != null) {
            if (!isSkippable(ln)) return ln;
        }
        return null;
    }

    private static boolean isSkippable(String line) {
        if (line == null) return true;
        String s = line.trim();
        if (s.isEmpty()) return true;
        if (s.startsWith("#") || s.startsWith("//") || s.startsWith(";")) return true;
        return false;
    }

    private static String detectDelimiter(String headerLine) {
        // simple detection priority: comma, semicolon, tab
        if (headerLine.indexOf(',') >= 0) return ",";
        if (headerLine.indexOf(';') >= 0) return ";";
        if (headerLine.indexOf('\t') >= 0) return "\t";
        return ","; // default
    }

    private static String[] split(String line, String delim) {
        // simple split without full CSV quoting support  fits most open data
        return line.split("\\Q" + delim + "\\E");
    }

    private static Map<String,Integer> headerIndex(String[] header) {
        Map<String,Integer> m = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            m.put(header[i].trim().toLowerCase(), i);
        }
        return m;
    }

    private static Integer find(String[] header, Map<String,Integer> idx, String[] keys) {
        for (String k : keys) {
            Integer i = idx.get(k);
            if (i != null) return i;
        }
        // fallback partial contains
        for (int i = 0; i < header.length; i++) {
            String h = header[i].trim().toLowerCase();
            for (String k : keys) {
                if (h.contains(k)) return i;
            }
        }
        return null;
    }

    private static Integer parseYear(String s) {
        if (s == null) return null;
        s = s.trim();
        // handle "1995-01-01" etc
        if (s.length() >= 4 && Character.isDigit(s.charAt(0))) {
            try {
                return Integer.parseInt(s.substring(0, 4));
            } catch (Exception ignore) { }
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }
}