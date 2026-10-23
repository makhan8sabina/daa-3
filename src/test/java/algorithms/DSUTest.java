package algorithms;

import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DSUTest {
    @Test
    void matchesReferenceAfterRandomUnions() {
        Random rnd = new Random(1);

        for (int round = 0; round < 50; round++) {
            int n = 1 + rnd.nextInt(200);
            DSU dsu = new DSU(n, new Metrics());
            int[] label = new int[n];

            for (int i = 0; i < n; i++) {
                label[i] = i;
            }

            for (int step = 0; step < 3 * n; step++) {
                int a = rnd.nextInt(n);
                int b = rnd.nextInt(n);
                boolean expectedMerge = label[a] != label[b];

                assertEquals(expectedMerge, dsu.union(a, b));

                if (expectedMerge) {
                    int from = label[b];
                    int to = label[a];

                    for (int i = 0; i < n; i++) {
                        if (label[i] == from) {
                            label[i] = to;
                        }
                    }
                }

                int x = rnd.nextInt(n);
                int y = rnd.nextInt(n);
                assertEquals(label[x] == label[y], dsu.find(x) == dsu.find(y));
            }

            Set<Integer> distinct = new HashSet<>();

            for (int l : label) {
                distinct.add(l);
            }

            assertEquals(distinct.size(), dsu.components());
        }
    }

    @Test
    void countsFindAndSuccessfulUnionOperations() {
        Metrics m = new Metrics();
        DSU dsu = new DSU(4, m);

        dsu.find(0);
        assertEquals(1, m.dsuOps);

        assertTrue(dsu.union(0, 1));
        assertEquals(4, m.dsuOps);

        assertFalse(dsu.union(0, 1));
        assertEquals(6, m.dsuOps);
    }

    @Test
    void makeSetResetsStructure() {
        DSU dsu = new DSU(5, new Metrics());
        dsu.union(0, 1);
        dsu.union(2, 3);
        assertEquals(3, dsu.components());

        dsu.makeSet(7);
        assertEquals(7, dsu.components());
        assertTrue(dsu.find(0) != dsu.find(1));
    }

    @Test
    void emptyAndInvalidInput() {
        DSU empty = new DSU(0, new Metrics());
        assertEquals(0, empty.components());

        DSU dsu = new DSU(3, new Metrics());
        assertThrows(IllegalArgumentException.class, () -> dsu.find(3));
        assertThrows(IllegalArgumentException.class, () -> dsu.find(-1));
        assertThrows(IllegalArgumentException.class, () -> new DSU(-1, new Metrics()));
    }

    @Test
    void longChainStaysShallowEnoughToFinish() {
        int n = 200_000;
        DSU dsu = new DSU(n, new Metrics());

        for (int i = 1; i < n; i++) {
            dsu.union(i - 1, i);
        }

        assertEquals(1, dsu.components());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }
}
