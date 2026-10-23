package graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        this.n = n;
        this.directed = directed;
        adjacency = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            adjacency.add(new ArrayList<>());
        }

        edges = new ArrayList<>();
    }

    public int size() {
        return n;
    }

    public boolean directed() {
        return directed;
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public int edgeCount() {
        return edges.size();
    }

    public List<Edge> outgoing(int v) {
        checkVertex(v);
        return adjacency.get(v);
    }

    public void addEdge(int u, int v, int w) {
        checkVertex(u);
        checkVertex(v);

        Edge e = new Edge(u, v, w);
        edges.add(e);
        adjacency.get(u).add(e);

        if (!directed) {
            adjacency.get(v).add(new Edge(v, u, w));
        }
    }

    public Graph reverse() {
        Graph r = new Graph(n, true);

        for (Edge e : edges) {
            r.addEdge(e.to(), e.from(), e.weight());
        }

        return r;
    }

    public void checkVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
    }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path)
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Empty graph file");
        }

        String[] h = lines.get(0).split("\\s+");

        if (h.length != 3) {
            throw new IllegalArgumentException(
                    "Header must be: n m directed|undirected"
            );
        }

        int n = Integer.parseInt(h[0]);
        int m = Integer.parseInt(h[1]);

        boolean directed = switch (h[2].toLowerCase()) {
            case "directed" -> true;
            case "undirected" -> false;
            default -> throw new IllegalArgumentException(
                    "Expected directed or undirected"
            );
        };

        if (lines.size() - 1 != m) {
            throw new IllegalArgumentException("Expected " + m + " edges");
        }

        Graph g = new Graph(n, directed);

        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split("\\s+");

            if (p.length != 3) {
                throw new IllegalArgumentException(
                        "Invalid edge line: " + lines.get(i)
                );
            }

            g.addEdge(
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2])
            );
        }

        return g;
    }
}