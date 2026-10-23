package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

final class TestGraphs {
    private TestGraphs() {}

    static Graph randomConnected(int n, int extra, Random rnd) {
        Graph g = new Graph(n, false);

        for (int i = 1; i < n; i++) {
            g.addEdge(rnd.nextInt(i), i, 1 + rnd.nextInt(50));
        }

        for (int i = 0; i < extra; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(50));
        }

        return g;
    }

    static Graph randomUndirected(int n, int m, Random rnd) {
        Graph g = new Graph(n, false);

        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(50));
        }

        return g;
    }

    static Graph randomDirected(int n, int m, Random rnd) {
        Graph g = new Graph(n, true);

        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(50));
        }

        return g;
    }

    static Graph randomDag(int n, int m, int minWeight, int maxWeight, Random rnd) {
        Graph g = new Graph(n, true);
        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }

        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i];
            rank[i] = rank[j];
            rank[j] = t;
        }

        for (int i = 0; i < m; i++) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);

            if (a == b) {
                continue;
            }

            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }

            g.addEdge(a, b, minWeight + rnd.nextInt(maxWeight - minWeight + 1));
        }

        return g;
    }

    static boolean[] bfs(Graph g, int source) {
        boolean[] seen = new boolean[g.size()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        seen[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int v = queue.remove();

            for (Edge e : g.outgoing(v)) {
                if (!seen[e.to()]) {
                    seen[e.to()] = true;
                    queue.add(e.to());
                }
            }
        }

        return seen;
    }

    static Set<Set<Integer>> bruteForceScc(Graph g) {
        int n = g.size();
        Graph rev = g.reverse();
        Set<Set<Integer>> result = new HashSet<>();

        for (int v = 0; v < n; v++) {
            boolean[] forward = bfs(g, v);
            boolean[] backward = bfs(rev, v);
            Set<Integer> comp = new HashSet<>();

            for (int u = 0; u < n; u++) {
                if (forward[u] && backward[u]) {
                    comp.add(u);
                }
            }

            result.add(comp);
        }

        return result;
    }

    static Set<Set<Integer>> asSets(List<List<Integer>> components) {
        Set<Set<Integer>> result = new HashSet<>();

        for (List<Integer> comp : components) {
            result.add(new HashSet<>(comp));
        }

        return result;
    }

    static Graph path(int n, boolean closeCycle) {
        Graph g = new Graph(n, true);

        for (int i = 0; i + 1 < n; i++) {
            g.addEdge(i, i + 1, 1);
        }

        if (closeCycle && n > 0) {
            g.addEdge(n - 1, 0, 1);
        }

        return g;
    }

    static List<Integer> list(int... values) {
        List<Integer> l = new ArrayList<>();

        for (int v : values) {
            l.add(v);
        }

        return l;
    }
}
