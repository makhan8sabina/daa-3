package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CounterTest {
    @Test
    void traversalCountersFollowTheFormulas() {
        Random rnd = new Random(29);

        for (int t = 0; t < 50; t++) {
            int n = 2 + rnd.nextInt(100);
            Graph dag = TestGraphs.randomDag(n, 3 * n, 1, 20, rnd);
            int m = dag.edgeCount();

            Metrics scc = new Metrics();
            GraphAlgorithms.kosaraju(dag, scc);
            assertEquals(2L * m, scc.edgeScans);

            Metrics kahn = new Metrics();
            GraphAlgorithms.kahn(dag, kahn);
            assertEquals(m, kahn.edgeScans);

            Metrics paths = new Metrics();
            GraphAlgorithms.dagShortestPaths(dag, paths);
            assertEquals(m, paths.edgeScans);
        }
    }

    @Test
    void kruskalStopsOnceTheTreeIsComplete() {
        Random rnd = new Random(31);
        Graph g = TestGraphs.randomConnected(200, 4000, rnd);

        Metrics m = new Metrics();
        MST.kruskal(g, m);

        assertTrue(m.edgeScans < g.edgeCount());
        assertTrue(m.comparisons > 0);
        assertTrue(m.dsuOps > 0);
    }

    @Test
    void primScansEveryAdjacencyEntryOnce() {
        Random rnd = new Random(37);
        Graph g = TestGraphs.randomConnected(100, 500, rnd);

        Metrics m = new Metrics();
        MST.prim(g, m);

        assertEquals(2L * g.edgeCount(), m.edgeScans);
    }
}
