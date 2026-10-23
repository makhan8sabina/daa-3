package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MSTTest {
    @Test
    void kruskalAndPrimAgreeOnRandomConnectedGraphs() {
        Random rnd = new Random(7);

        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(60);
            Graph g = TestGraphs.randomConnected(n, rnd.nextInt(4 * n), rnd);

            MSTResult k = MST.kruskal(g, new Metrics());
            MSTResult p = MST.prim(g, new Metrics());

            assertEquals(k.weight(), p.weight());
            assertEquals(n - 1, k.edges().size());
            assertEquals(n - 1, p.edges().size());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
        }
    }

    @Test
    void forestOnRandomPossiblyDisconnectedGraphs() {
        Random rnd = new Random(11);

        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(40);
            Graph g = TestGraphs.randomUndirected(n, rnd.nextInt(2 * n), rnd);

            MSTResult k = MST.kruskal(g, new Metrics());
            MSTResult p = MST.prim(g, new Metrics());

            assertEquals(k.weight(), p.weight());
            assertEquals(k.components(), p.components());
            assertEquals(n - k.components(), k.edges().size());
            assertEquals(n - p.components(), p.edges().size());
        }
    }

    @Test
    void disconnectedFileReturnsForest() throws IOException {
        Graph g = Graph.read(Path.of("data/disconnected.txt"));

        MSTResult k = MST.kruskal(g, new Metrics());
        MSTResult p = MST.prim(g, new Metrics());

        assertEquals(11, k.weight());
        assertEquals(11, p.weight());
        assertEquals(3, k.components());
        assertEquals(3, p.components());
    }

    @Test
    void selfLoopsParallelEdgesAndIsolatedVertices() throws IOException {
        Graph g = Graph.read(Path.of("data/edge_cases.txt"));

        MSTResult k = MST.kruskal(g, new Metrics());
        MSTResult p = MST.prim(g, new Metrics());

        assertEquals(3, k.weight());
        assertEquals(3, p.weight());
        assertEquals(3, k.components());
        assertEquals(3, p.components());
    }

    @Test
    void emptyAndSingleVertexGraphs() {
        Graph empty = new Graph(0, false);
        assertEquals(0, MST.kruskal(empty, new Metrics()).components());
        assertEquals(0, MST.prim(empty, new Metrics()).components());

        Graph single = new Graph(1, false);
        single.addEdge(0, 0, 5);
        assertEquals(1, MST.kruskal(single, new Metrics()).components());
        assertEquals(0, MST.prim(single, new Metrics()).weight());
    }

    @Test
    void rejectsDirectedGraph() {
        Graph g = new Graph(3, true);
        assertThrows(IllegalArgumentException.class, () -> MST.kruskal(g, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> MST.prim(g, new Metrics()));
    }
}
