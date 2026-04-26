package com.syndicati.services.residence;

import com.syndicati.models.residence.Appartement;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServicePredictionPrix {
    private static final int K = 7;

    private double[] weights;
    private double[] featureMeans = new double[K];
    private double[] featureStds  = new double[K];
    private boolean trained = false;

    private static final String CSV_PATH = "uploads/tunisia-real-estate.csv";

    private Integer parseCsvType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.strip().toLowerCase();
        for (String ex : new String[]{"house", "land", "surface", "vacant", "agricultural"})
            if (s.contains(ex)) return null;
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("1-room apartment", 0);
        map.put("2-room apartment", 1);
        map.put("3-room apartment", 2);
        map.put("4-room apartment", 3);
        map.put("5-room apartment", 4);
        map.put("6-room apartment", 5);
        return map.get(s);
    }

    private Integer parseAppType(String typeA) {
        if (typeA == null || typeA.isBlank()) return null;
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("S+0", 0);
        map.put("S+1", 1);
        map.put("S+2", 2);
        map.put("S+3", 3);
        map.put("S+4", 4);
        map.put("S+5", 5);
        return map.get(typeA.strip().toUpperCase());
    }

    private double[] buildFeatures(double surface, int typeIndex) {
        double[] f = new double[K];
        f[0] = surface;
        f[1 + Math.min(Math.max(typeIndex, 0), 5)] = 1.0;
        return f;
    }

    public void trainFromCsv() throws Exception {
        Path path = Paths.get(CSV_PATH);
        if (!Files.exists(path))
            throw new Exception("CSV not found: " + CSV_PATH);

        List<double[]> featureList = new ArrayList<>();
        List<Double>   priceList   = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new Exception("Empty CSV");

            String[] headers = headerLine.split(",");
            Map<String, Integer> col = new HashMap<>();
            for (int i = 0; i < headers.length; i++)
                col.put(headers[i].strip(), i);

            for (String req : List.of("Nature", "Type of Real Estate", "Surface", "Price"))
                if (!col.containsKey(req))
                    throw new Exception("Missing CSV column: " + req);

            String line;
            while ((line = br.readLine()) != null) {
                String[] cells = line.split(",", -1);
                if (!"Rental".equalsIgnoreCase(get(cells, col, "Nature"))) continue;

                Integer typeIdx = parseCsvType(get(cells, col, "Type of Real Estate"));
                if (typeIdx == null) continue;

                String surfStr  = get(cells, col, "Surface");
                String priceStr = get(cells, col, "Price");
                if (surfStr.isBlank() || priceStr.isBlank()) continue;

                try {
                    double surface = Double.parseDouble(surfStr);
                    double price   = Double.parseDouble(priceStr);
                    if (surface < 10 || surface > 1000) continue;
                    if (price   < 50 || price   > 20000) continue;
                    featureList.add(buildFeatures(surface, typeIdx));
                    priceList.add(price);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (featureList.isEmpty())
            throw new Exception("No valid rental rows found for training");

        int n = featureList.size();
        System.out.printf("[ML] Training on %d rental samples%n", n);

        for (int j = 0; j < K; j++) {
            double sum = 0;
            for (double[] f : featureList) sum += f[j];
            featureMeans[j] = sum / n;

            double var = 0;
            for (double[] f : featureList) var += Math.pow(f[j] - featureMeans[j], 2);
            featureStds[j] = Math.sqrt(var / n + 1e-8);
        }

        double[][] X = new double[n][K + 1];
        double[]   y = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < K; j++)
                X[i][j] = (featureList.get(i)[j] - featureMeans[j]) / featureStds[j];
            X[i][K] = 1.0;
            y[i]    = priceList.get(i);
        }

        weights = olsSolve(X, y, K + 1);
        trained = true;

        String[] labels = {"S+0","S+1","S+2","S+3","S+4","S+5"};
        for (int t = 0; t < 6; t++)
            System.out.printf("       %-4s → %.0f TND%n", labels[t], predictRaw(80, t));
    }

    public double predict(Appartement appartement) {
        if (!trained) return 0.0;
        try {
            double surface = Double.parseDouble(String.valueOf(appartement.getSuperficie()));
            Integer typeIdx = parseAppType(appartement.getType_a());
            if (typeIdx == null) return 0.0;
            return predictRaw(surface, typeIdx);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double predict(double surface, String typeA) {
        if (!trained) return 0.0;
        Integer typeIdx = parseAppType(typeA);
        if (typeIdx == null) return 0.0;
        return predictRaw(surface, typeIdx);
    }

    private double predictRaw(double surface, int typeIdx) {
        double[] raw = buildFeatures(surface, typeIdx);
        double dot = 0;
        for (int j = 0; j < K; j++)
            dot += weights[j] * ((raw[j] - featureMeans[j]) / featureStds[j]);
        dot += weights[K];
        return Math.max(0, dot);
    }

    public void saveModel(String path) throws Exception {
        new File(path).getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(featureMeans);
            oos.writeObject(featureStds);
            oos.writeObject(weights);
        }
        System.out.println("Modèle sauvegardé");
    }

    public void loadModel(String path) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            featureMeans = (double[]) ois.readObject();
            featureStds  = (double[]) ois.readObject();
            weights      = (double[]) ois.readObject();
            trained      = true;
        }
    }

    private double[] olsSolve(double[][] X, double[] y, int k) {
        double[][] A = new double[k][k];
        double[]   b = new double[k];

        for (int r = 0; r < X.length; r++) {
            for (int i = 0; i < k; i++) {
                b[i] += X[r][i] * y[r];
                for (int j = 0; j < k; j++)
                    A[i][j] += X[r][i] * X[r][j];
            }
        }

        double[][] aug = new double[k][k + 1];
        for (int i = 0; i < k; i++) {
            System.arraycopy(A[i], 0, aug[i], 0, k);
            aug[i][k] = b[i];
        }

        for (int col = 0; col < k; col++) {
            int pivot = col;
            for (int row = col + 1; row < k; row++)
                if (Math.abs(aug[row][col]) > Math.abs(aug[pivot][col])) pivot = row;
            double[] tmp = aug[col]; aug[col] = aug[pivot]; aug[pivot] = tmp;

            double div = aug[col][col];
            if (Math.abs(div) < 1e-12) continue;
            for (int j = col; j <= k; j++) aug[col][j] /= div;

            for (int row = 0; row < k; row++) {
                if (row == col) continue;
                double factor = aug[row][col];
                for (int j = col; j <= k; j++)
                    aug[row][j] -= factor * aug[col][j];
            }
        }

        double[] w = new double[k];
        for (int i = 0; i < k; i++) w[i] = aug[i][k];
        return w;
    }

    private String get(String[] cells, Map<String, Integer> col, String key) {
        Integer idx = col.get(key);
        if (idx == null || idx >= cells.length) return "";
        return cells[idx].strip();
    }
}