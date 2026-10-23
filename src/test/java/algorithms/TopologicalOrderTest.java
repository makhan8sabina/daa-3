package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalOrderTest {
    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(13);

        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(100);
            Graph g = TestGraphs.randomDag(n, rnd.nextInt(4 * n), 1, 20, rnd);

            GraphAlgorithms.TopologicalResult r = GraphAlgorithms.kahn(g, new Metrics());
            List<Integer> order = r.order();

            assertFalse(r.hasCycle());
            assertEquals(n, order.size());

            int[] pos = new int[n];

            for (int i = 0; i < n; i++) {
                pos[order.get(i)] = i;
            }

            g.edges().forEach(e -> assertTrue(pos[e.from()] < pos[e.to()]));
        }
    }

    @Test
    void detectsCycleInFile() throws IOException {
        Graph g = Graph.read(Path.of("data/cyclic.txt"));
        GraphAlgorithms.TopologicalResult r = GraphAlgorithms.kahn(g, new Metrics());

        assertTrue(r.hasCycle());
        assertTrue(r.order().size() < g.size());
    }

    @Test
    void detectsCycleInRandomGraphWithInjectedCycle() {
        Random rnd = new Random(17);

        for (int t = 0; t < 100; t++) {
            int n = 3 + rnd.nextInt(50);
            Graph g = TestGraphs.randomDag(n, 2 * n, 1, 20, rnd);
            int a = rnd.nextInt(n);
            int b = (a + 1 + rnd.nextInt(n - 1)) % n;
            g.addEdge(a, b, 1);
            g.addEdge(b, a, 1);

            assertTrue(GraphAlgorithms.kahn(g, new Metrics()).hasCycle());
        }
    }

    @Test
    void selfLoopIsACycle() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 0, 1);
        assertTrue(GraphAlgorithms.kahn(g, new Metrics()).hasCycle());
    }

    @Test
    void isValidOrderRejectsBadOrders() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);

        assertTrue(GraphAlgorithms.isValidOrder(g, TestGraphs.list(0, 1, 2)));
        assertFalse(GraphAlgorithms.isValidOrder(g, TestGraphs.list(2, 1, 0)));
        assertFalse(GraphAlgorithms.isValidOrder(g, TestGraphs.list(0, 1)));
        assertFalse(GraphAlgorithms.isValidOrder(g, TestGraphs.list(0, 0, 2)));
    }

    @Test
    void emptyAndIsolatedVertices() {
        assertEquals(0, GraphAlgorithms.kahn(new Graph(0, true), new Metrics()).order().size());
        assertEquals(4, GraphAlgorithms.kahn(new Graph(4, true), new Metrics()).order().size());
    }
}
