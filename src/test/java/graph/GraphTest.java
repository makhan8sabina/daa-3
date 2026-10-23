package graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {
    @Test
    void readsDirectedGraph() throws IOException {
        Graph g = Graph.read(Path.of("data/cyclic.txt"));

        assertTrue(g.directed());
        assertEquals(4, g.size());
        assertEquals(4, g.edgeCount());
        assertEquals(1, g.outgoing(0).size());
        assertEquals(0, g.outgoing(3).size());
    }

    @Test
    void readsUndirectedGraphWithIsolatedVertex() throws IOException {
        Graph g = Graph.read(Path.of("data/disconnected.txt"));

        assertFalse(g.directed());
        assertEquals(5, g.size());
        assertEquals(2, g.edgeCount());
        assertEquals(0, g.outgoing(2).size());
        assertEquals(1, g.outgoing(0).size());
        assertEquals(1, g.outgoing(1).size());
    }

    @Test
    void readsNegativeWeights() throws IOException {
        Graph g = Graph.read(Path.of("data/dag.txt"));
        assertEquals(-3, g.outgoing(1).get(0).weight());
    }

    @Test
    void keepsSelfLoopsAndParallelEdges() throws IOException {
        Graph g = Graph.read(Path.of("data/edge_cases.txt"));

        assertEquals(5, g.size());
        assertEquals(5, g.edgeCount());
        assertEquals(3, g.outgoing(0).size());
        assertEquals(5, g.outgoing(1).size());
        assertEquals(2, g.outgoing(2).size());
        assertEquals(0, g.outgoing(3).size());
        assertEquals(0, g.outgoing(4).size());
    }

    @Test
    void reverseSwapsDirections() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 6);

        Graph r = g.reverse();

        assertEquals(1, r.outgoing(1).size());
        assertEquals(0, r.outgoing(1).get(0).to());
        assertEquals(6, r.outgoing(2).get(0).weight());
        assertEquals(0, r.outgoing(0).size());
    }

    @Test
    void rejectsMalformedInput(@org.junit.jupiter.api.io.TempDir Path dir) throws IOException {
        Path badHeader = dir.resolve("bad_header.txt");
        Files.writeString(badHeader, "3 1 sideways\n0 1 1\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.read(badHeader));

        Path wrongCount = dir.resolve("wrong_count.txt");
        Files.writeString(wrongCount, "3 2 directed\n0 1 1\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.read(wrongCount));

        Path badVertex = dir.resolve("bad_vertex.txt");
        Files.writeString(badVertex, "2 1 directed\n0 5 1\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.read(badVertex));

        Path empty = dir.resolve("empty.txt");
        Files.writeString(empty, "\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.read(empty));
    }
}
