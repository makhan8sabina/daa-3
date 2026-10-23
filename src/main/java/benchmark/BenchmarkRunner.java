package benchmark;

import algorithms.GraphAlgorithms;
import algorithms.MST;
import algorithms.MSTResult;
import graph.Graph;
import metrics.Metrics;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class BenchmarkRunner {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int[] DENSITIES = {4, 20};
    private static final int SEED = 42;
    private static final int RUNS = 5;

    private static final long[][] REFERENCE = {
            {1000, 4000, 147970, 27, 396274},
            {1000, 20000, 30363, 1, 252411},
            {3162, 12648, 468911, 137, 1271911},
            {3162, 63240, 97589, 1, 723157},
            {10000, 40000, 1486139, 373, 3941828},
            {10000, 200000, 309550, 1, 2381342},
            {31623, 126492, 4715515, 1188, 12551891},
            {31623, 632460, 968517, 1, 7376612},
            {100000, 400000, 15012094, 3923, 40096539},
            {100000, 2000000, 3056681, 1, 23512087}
    };

    private static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private record Row(
            String algorithm,
            String graph,
            int n,
            int m,
            double timeMs,
            Metrics metrics,
            long result
    ) {}

    private interface Task {
        void run();
    }

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");

        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }

        List<Row> rows = new ArrayList<>();

        for (int n : SIZES) {
            for (int d : DENSITIES) {
                runCase(n, d * n, d == 4 ? "sparse" : "dense", rows);
            }
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out))) {
            w.println(HEADER);

            for (Row r : rows) {
                Metrics x = r.metrics();

                w.printf(
                        Locale.US,
                        "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d%n",
                        r.algorithm(), r.graph(), r.n(), r.m(), r.timeMs(),
                        x.dsuOps, x.edgeScans, x.comparisons, x.maxDepth,
                        r.result()
                );
            }
        }

        System.out.println("Wrote " + rows.size() + " rows to " + out);
    }

    private static void runCase(int n, int m, String name, List<Row> rows) {
        Graph undirected = generateUndirected(n, m, new Random(SEED));
        Graph directed = generateDirected(n, m, new Random(SEED));
        Graph dag = generateDag(n, m, new Random(SEED));

        Metrics km = new Metrics();
        MSTResult kr = MST.kruskal(undirected, km);
        double kt = medianMs(() -> MST.kruskal(undirected, new Metrics()));

        Metrics pm = new Metrics();
        MSTResult pr = MST.prim(undirected, pm);
        double pt = medianMs(() -> MST.prim(undirected, new Metrics()));

        if (kr.weight() != pr.weight() || kr.components() != pr.components()) {
            throw new IllegalStateException("Kruskal and Prim disagree: n=" + n + " m=" + m);
        }

        if (kr.edges().size() != n - kr.components()) {
            throw new IllegalStateException("Wrong number of MST edges: n=" + n + " m=" + m);
        }

        Metrics sm = new Metrics();
        var sr = GraphAlgorithms.kosaraju(directed, sm);
        double st = medianMs(() -> GraphAlgorithms.kosaraju(directed, new Metrics()));

        Metrics qm = new Metrics();
        var qr = GraphAlgorithms.kahn(dag, qm);
        double qt = medianMs(() -> GraphAlgorithms.kahn(dag, new Metrics()));

        if (qr.hasCycle() || !GraphAlgorithms.isValidOrder(dag, qr.order())) {
            throw new IllegalStateException("Kahn failed on generated DAG: n=" + n + " m=" + m);
        }

        Metrics dm = new Metrics();
        var dr = GraphAlgorithms.dagShortestPaths(dag, dm);
        double dt = medianMs(() -> GraphAlgorithms.dagShortestPaths(dag, new Metrics()));

        long sum = 0;

        for (long x : dr.distance()) {
            sum += x;
        }

        if (sm.edgeScans != 2L * m || qm.edgeScans != m || dm.edgeScans != m) {
            throw new IllegalStateException("Unexpected edge scan counters: n=" + n + " m=" + m);
        }

        checkReference(n, m, kr.weight(), sr.components().size(), sum);

        rows.add(new Row("Kruskal", name, n, m, kt, km, kr.weight()));
        rows.add(new Row("Prim", name, n, m, pt, pm, pr.weight()));
        rows.add(new Row("Kosaraju", name, n, m, st, sm, sr.components().size()));
        rows.add(new Row("Kahn", name, n, m, qt, qm, qr.order().size()));
        rows.add(new Row("DagShortestPath", name, n, m, dt, dm, sum));

        System.out.printf(
                "n=%d m=%d MST=%d SCC=%d DAG-distance-sum=%d  [reference OK]%n",
                n, m, kr.weight(), sr.components().size(), sum
        );
    }

    private static void checkReference(int n, int m, long mst, long scc, long dagSum) {
        for (long[] ref : REFERENCE) {
            if (ref[0] == n && ref[1] == m) {
                if (ref[2] != mst || ref[3] != scc || ref[4] != dagSum) {
                    throw new IllegalStateException(
                            "Reference mismatch n=" + n + " m=" + m
                                    + " expected=" + Arrays.toString(Arrays.copyOfRange(ref, 2, 5))
                                    + " got=[" + mst + ", " + scc + ", " + dagSum + "]"
                    );
                }

                return;
            }
        }

        throw new IllegalStateException("No reference row for n=" + n + " m=" + m);
    }

    private static double medianMs(Task task) {
        long[] times = new long[RUNS];

        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            task.run();
            times[i] = System.nanoTime() - start;
        }

        Arrays.sort(times);
        return times[RUNS / 2] / 1_000_000.0;
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    private static int[] identity(int n) {
        int[] a = new int[n];

        for (int i = 0; i < n; i++) {
            a[i] = i;
        }

        return a;
    }

    private static int[] randomPair(Random rnd, int n) {
        int a = rnd.nextInt(n);
        int b;

        do {
            b = rnd.nextInt(n);
        } while (b == a);

        return new int[]{a, b};
    }

    private static Graph generateUndirected(int n, int m, Random rnd) {
        Graph g = new Graph(n, false);
        int[] perm = identity(n);
        shuffle(perm, rnd);

        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            g.addEdge(u, perm[i], 1 + rnd.nextInt(1000));
        }

        while (g.edgeCount() < m) {
            int[] p = randomPair(rnd, n);
            g.addEdge(p[0], p[1], 1 + rnd.nextInt(1000));
        }

        return g;
    }

    private static Graph generateDirected(int n, int m, Random rnd) {
        Graph g = new Graph(n, true);

        while (g.edgeCount() < m) {
            int[] p = randomPair(rnd, n);
            g.addEdge(p[0], p[1], 1 + rnd.nextInt(1000));
        }

        return g;
    }

    private static Graph generateDag(int n, int m, Random rnd) {
        Graph g = new Graph(n, true);
        int[] rank = identity(n);
        shuffle(rank, rnd);

        while (g.edgeCount() < m) {
            int[] p = randomPair(rnd, n);
            int a = p[0];
            int b = p[1];

            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }

            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }

        return g;
    }
}
