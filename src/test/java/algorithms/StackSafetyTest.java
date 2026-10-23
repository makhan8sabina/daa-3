package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackSafetyTest {
    private static final int N = 100_000;

    @Test
    void sccOnPathOf100000Vertices() {
        Graph g = TestGraphs.path(N, false);
        Metrics m = new Metrics();

        GraphAlgorithms.SCCResult r = GraphAlgorithms.kosaraju(g, m);

        assertEquals(N, r.components().size());
        assertEquals(N, r.maxDepth());
        assertEquals(N, m.maxDepth);
    }

    @Test
    void sccOnCycleOf100000Vertices() {
        Graph g = TestGraphs.path(N, true);

        GraphAlgorithms.SCCResult r = GraphAlgorithms.kosaraju(g, new Metrics());

        assertEquals(1, r.components().size());
        assertEquals(N, r.components().get(0).size());
    }

    @Test
    void kahnAndShortestPathsOnPathOf100000Vertices() {
        Graph g = TestGraphs.path(N, false);

        GraphAlgorithms.TopologicalResult t = GraphAlgorithms.kahn(g, new Metrics());
        assertFalse(t.hasCycle());
        assertTrue(GraphAlgorithms.isValidOrder(g, t.order()));

        GraphAlgorithms.PathResult p = GraphAlgorithms.dagShortestPaths(g, new Metrics());
        assertEquals(N - 1, p.distance()[N - 1]);
        assertEquals(N, p.pathTo(N - 1).size());
    }
}
