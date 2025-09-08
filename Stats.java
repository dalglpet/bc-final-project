package main;

// statistical helpers
public class Stats {

    // pearson correlation coefficient
    public static double pearson(java.util.List<Double> xs, java.util.List<Double> ys) {
        int n = Math.min(xs.size(), ys.size());
        if (n < 2) return 0.0;
        double meanX = mean(xs);
        double meanY = mean(ys);
        double num = 0.0, denX = 0.0, denY = 0.0;
        for (int i = 0; i < n; i++) {
            double dx = xs.get(i) - meanX;
            double dy = ys.get(i) - meanY;
            num += dx * dy;
            denX += dx * dx;
            denY += dy * dy;
        }
        double denom = Math.sqrt(denX) * Math.sqrt(denY);
        if (denom == 0) return 0.0;
        return num / denom;
    }

    // mean helper
    public static double mean(java.util.List<Double> v) {
        if (v.isEmpty()) return 0.0;
        double s = 0.0;
        for (double x : v) s += x;
        return s / v.size();
    }
}
