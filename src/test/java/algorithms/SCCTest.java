package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SCCTest {
    @Test
    void matchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(3);

        for (int t = 0; t < 300; t++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(n, m, rnd);

            Set<Set<Integer>> expected = TestGraphs.bruteForceScc(g);
            Set<Set<Integer>> actual = TestGraphs.asSets(
                    GraphAlgorithms.kosaraju(g, new Metrics()).components()
            );

            assertEquals(expected, actual);
        }
    }

    @Test
    void cyclicFileComponents() throws IOException {
        Graph g = Graph.read(Path.of("data/cyclic.txt"));

        Set<Set<Integer>> actual = TestGraphs.asSets(
                GraphAlgorithms.kosaraju(g, new Metrics()).components()
        );

        assertEquals(
                Set.of(Set.of(0, 1, 2), Set.of(3)),
                actual
        );
    }

    @Test
    void everyVertexAppearsExactlyOnce() {
        Random rnd = new Random(5);
        Graph g = TestGraphs.randomDirected(150, 400, rnd);

        List<List<Integer>> comps = GraphAlgorithms.kosaraju(g, new Metrics()).components();
        int total = 0;

        for (List<Integer> c : comps) {
            total += c.size();
        }

        assertEquals(150, total);
    }

    @Test
    void emptyGraphHasNoComponents() {
        Graph g = new Graph(0, true);
        assertEquals(0, GraphAlgorithms.kosaraju(g, new Metrics()).components().size());
    }
}
