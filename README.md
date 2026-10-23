# DAA Assignment 3: Graphs (DSU, MST, SCC, Shortest Paths in a DAG)

Java 17 + Maven project for the "City Network & Job Scheduler" scenario. It contains one graph class,
a DSU with union by size and path compression, Kruskal, Prim, Kosaraju (iterative DFS), Kahn and
shortest paths in a DAG, all with operation counters.

## Layout

```
src/main/java/graph        Edge, Graph (adjacency list, directed and undirected, file input)
src/main/java/algorithms   DSU, MST (Kruskal, Prim), GraphAlgorithms (Kosaraju, Kahn, DAG shortest paths)
src/main/java/metrics      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/benchmark    BenchmarkRunner (generator, self-checks, results.csv)
src/test/java              JUnit 5 tests
data/                      Input graphs
results/                   results.csv, ratios, slopes, plots/
plot.py                    Plots, counter/formula ratios and slopes
REPORT.md                  Report
```

## Input format

```
n m directed|undirected
u v w        (m lines)
```

Vertices are numbered `0 .. n-1`, weights are `int`. Files in `data/`:
`cyclic.txt` (directed, has a cycle), `disconnected.txt` (undirected, three components),
`dag.txt` (directed, acyclic, negative weights), `edge_cases.txt` (self-loop, parallel edges, isolated vertices).

## Build and test

```
mvn test
```

## Run the benchmark

```
mvn -q compile exec:java
```

Without Maven:

```
javac -d target/classes $(find src/main -name '*.java')
java -Xmx2g -cp target/classes benchmark.BenchmarkRunner
```

The benchmark builds the ten graphs with `new Random(42)`, runs all five algorithms on each,
and writes `results/results.csv` (50 rows). The run stops with an exception if:

- Kruskal and Prim return different weights or component counts;
- a topological order is invalid;
- Kosaraju does not scan exactly 2m edges, or Kahn or the DAG relaxation does not scan exactly m;
- any MST weight, SCC count or DAG distance sum differs from the reference table in the assignment.

## Plots, ratios and slopes

```
pip install pandas numpy matplotlib
python plot.py
```

Writes `results/plots/*.png`, `results/ratios.csv`, `results/kruskal_ratios.csv`, `results/slopes.csv`.

## AI use acknowledgement

An AI assistant (Claude, Anthropic) was used while working on this project for code review, for help
with the test suite and for drafting parts of the documentation. I ran the code and the tests myself
and I can explain every part of the implementation.
