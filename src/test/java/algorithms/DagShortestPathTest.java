package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DagShortestPathTest {
    private static long[] bellmanFord(Graph g) {
        int n = g.size();
        long inf = Long.MAX_VALUE / 4;
        long[] dist = new long[n];
        int[] indegree = new int[n];

        Arrays.fill(dist, inf);

        for (Edge e : g.edges()) {
            indegree[e.to()]++;
        }

        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                dist[v] = 0;
            }
        }

        for (int round = 0; round < n; round++) {
            for (Edge e : g.edges()) {
                if (dist[e.from()] != inf && dist[e.from()] + e.weight() < dist[e.to()]) {
                    dist[e.to()] = dist[e.from()] + e.weight();
                }
            }
        }

        return dist;
    }

    @Test
    void matchesBellmanFordWithNegativeWeights() {
        Random rnd = new Random(19);

        for (int t = 0; t < 300; t++) {
            int n = 1 + rnd.nextInt(30);
            Graph g = TestGraphs.randomDag(n, rnd.nextInt(4 * n), -20, 20, rnd);

            long[] expected = bellmanFord(g);
            long[] actual = GraphAlgorithms.dagShortestPaths(g, new Metrics()).distance();

            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void restoredPathsAreConsistentWithDistances() {
        Random rnd = new Random(23);

        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(25);
            Graph g = TestGraphs.randomDag(n, 3 * n, -15, 15, rnd);
            GraphAlgorithms.PathResult r = GraphAlgorithms.dagShortestPaths(g, new Metrics());

            int[] indegree = new int[n];

            for (Edge e : g.edges()) {
                indegree[e.to()]++;
            }

            for (int v = 0; v < n; v++) {
                List<Integer> path = r.pathTo(v);

                assertEquals(0, indegree[path.get(0)]);
                assertEquals(v, path.get(path.size() - 1));

                long sum = 0;

                for (int i = 0; i + 1 < path.size(); i++) {
                    int from = path.get(i);
                    int to = path.get(i + 1);
                    long best = Long.MAX_VALUE;

                    for (Edge e : g.outgoing(from)) {
                        if (e.to() == to) {
                            best = Math.min(best, e.weight());
                        }
                    }

                    sum += best;
                }

                assertEquals(r.distance()[v], sum);
            }
        }
    }

    @Test
    void knownExampleFromFile() throws IOException {
        Graph g = Graph.read(Path.of("data/dag.txt"));
        GraphAlgorithms.PathResult r = GraphAlgorithms.dagShortestPaths(g, new Metrics());

        assertArrayEquals(new long[]{0, 2, -1, -2, 0, 0}, r.distance());
        assertEquals(TestGraphs.list(0, 1, 2, 4, 3, 5), r.pathTo(5));
        assertEquals(TestGraphs.list(0), r.pathTo(0));
    }

    @Test
    void everySourceStartsAtZero() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, -4);
        g.addEdge(2, 3, 1);

        long[] d = GraphAlgorithms.dagShortestPaths(g, new Metrics()).distance();

        assertArrayEquals(new long[]{0, 0, -4, -3}, d);
    }

    @Test
    void cyclicGraphIsRejected() throws IOException {
        Graph g = Graph.read(Path.of("data/cyclic.txt"));
        assertThrows(
                IllegalArgumentException.class,
                () -> GraphAlgorithms.dagShortestPaths(g, new Metrics())
        );
    }

    @Test
    void invalidVertexInPathTo() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, 1);
        GraphAlgorithms.PathResult r = GraphAlgorithms.dagShortestPaths(g, new Metrics());

        assertThrows(IllegalArgumentException.class, () -> r.pathTo(2));
    }
}
