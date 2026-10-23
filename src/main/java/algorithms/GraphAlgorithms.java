package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public final class GraphAlgorithms {
    private GraphAlgorithms() {}

    public record SCCResult(List<List<Integer>> components, long maxDepth) {}

    public record TopologicalResult(List<Integer> order, boolean hasCycle) {}

    public record PathResult(
            long[] distance,
            int[] parent,
            List<Integer> topologicalOrder
    ) {
        public List<Integer> pathTo(int v) {
            if (v < 0 || v >= parent.length) {
                throw new IllegalArgumentException("Invalid vertex: " + v);
            }

            List<Integer> path = new ArrayList<>();

            for (int x = v; x != -1; x = parent[x]) {
                path.add(x);
            }

            Collections.reverse(path);
            return path;
        }
    }

    public static SCCResult kosaraju(Graph g, Metrics m) {
        int n = g.size();

        boolean[] seen = new boolean[n];
        int[] nextIndex = new int[n];
        List<Integer> finish = new ArrayList<>(n);
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        long maxDepth = 0;

        for (int s = 0; s < n; s++) {
            if (seen[s]) {
                continue;
            }

            seen[s] = true;
            stack.push(s);
            maxDepth = Math.max(maxDepth, stack.size());

            while (!stack.isEmpty()) {
                int v = stack.peek();
                List<Edge> adj = g.outgoing(v);

                if (nextIndex[v] < adj.size()) {
                    Edge e = adj.get(nextIndex[v]++);
                    m.edgeScans++;

                    if (!seen[e.to()]) {
                        seen[e.to()] = true;
                        stack.push(e.to());
                        maxDepth = Math.max(maxDepth, stack.size());
                    }
                } else {
                    finish.add(stack.pop());
                }
            }
        }

        Graph rev = g.reverse();
        Arrays.fill(seen, false);

        List<List<Integer>> comps = new ArrayList<>();

        for (int i = finish.size() - 1; i >= 0; i--) {
            int s = finish.get(i);

            if (seen[s]) {
                continue;
            }

            List<Integer> comp = new ArrayList<>();

            stack.push(s);
            seen[s] = true;
            maxDepth = Math.max(maxDepth, stack.size());

            while (!stack.isEmpty()) {
                int v = stack.pop();
                comp.add(v);

                for (Edge e : rev.outgoing(v)) {
                    m.edgeScans++;

                    if (!seen[e.to()]) {
                        seen[e.to()] = true;
                        stack.push(e.to());
                        maxDepth = Math.max(maxDepth, stack.size());
                    }
                }
            }

            comps.add(comp);
        }

        m.maxDepth = Math.max(m.maxDepth, maxDepth);

        return new SCCResult(comps, maxDepth);
    }

    public static TopologicalResult kahn(Graph g, Metrics m) {
        int n = g.size();
        int[] indegree = new int[n];

        for (int v = 0; v < n; v++) {
            for (Edge e : g.outgoing(v)) {
                indegree[e.to()]++;
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();

        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>(n);

        while (!queue.isEmpty()) {
            int v = queue.remove();
            order.add(v);

            for (Edge e : g.outgoing(v)) {
                m.edgeScans++;

                if (--indegree[e.to()] == 0) {
                    queue.add(e.to());
                }
            }
        }

        boolean cycle = order.size() != n;

        if (!cycle && !isValidOrder(g, order)) {
            throw new IllegalStateException("Invalid topological order");
        }

        return new TopologicalResult(List.copyOf(order), cycle);
    }

    public static boolean isValidOrder(Graph g, List<Integer> order) {
        int n = g.size();

        if (order.size() != n) {
            return false;
        }

        int[] pos = new int[n];
        Arrays.fill(pos, -1);

        for (int i = 0; i < n; i++) {
            int v = order.get(i);

            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }

            pos[v] = i;
        }

        for (Edge e : g.edges()) {
            if (pos[e.from()] >= pos[e.to()]) {
                return false;
            }
        }

        return true;
    }

    public static PathResult dagShortestPaths(Graph g, Metrics m) {
        TopologicalResult topo = kahn(g, new Metrics());

        if (topo.hasCycle()) {
            throw new IllegalArgumentException(
                    "DAG shortest paths require an acyclic graph"
            );
        }

        int n = g.size();
        long inf = Long.MAX_VALUE / 4;

        long[] dist = new long[n];
        int[] parent = new int[n];
        int[] indegree = new int[n];

        Arrays.fill(dist, inf);
        Arrays.fill(parent, -1);

        for (Edge e : g.edges()) {
            indegree[e.to()]++;
        }

        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                dist[v] = 0;
            }
        }

        for (int v : topo.order()) {
            for (Edge e : g.outgoing(v)) {
                m.edgeScans++;

                if (dist[v] + e.weight() < dist[e.to()]) {
                    dist[e.to()] = dist[v] + e.weight();
                    parent[e.to()] = v;
                }
            }
        }

        return new PathResult(dist, parent, topo.order());
    }
}
