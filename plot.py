import os
import sys

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

MARKERS = {"Kruskal": "s", "Prim": "o", "Kosaraju": "^", "Kahn": "D", "DagShortestPath": "x"}
SIZES = {"Kruskal": 9, "Prim": 8, "Kosaraju": 11, "Kahn": 6, "DagShortestPath": 9}
STYLES = {"sparse": "-", "dense": "--"}

CSV_PATH = "results/results.csv"
PLOT_DIR = "results/plots"

if not os.path.exists(CSV_PATH):
    sys.exit("results/results.csv not found. Run the benchmark first.")

os.makedirs(PLOT_DIR, exist_ok=True)

df = pd.read_csv(CSV_PATH)
df["scans_per_edge"] = df["edge_scans"] / df["m"]

plt.figure(figsize=(10, 6))
for (algo, kind), group in df.groupby(["algorithm", "graph"]):
    plt.loglog(group["n"], group["edge_scans"], marker=MARKERS[algo], markersize=SIZES[algo], linestyle=STYLES[kind], alpha=0.8, label=f"{algo} ({kind})")
plt.xlabel("Number of vertices n")
plt.ylabel("Edge scans")
plt.title("Operations vs n")
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
plt.tight_layout()
plt.savefig(f"{PLOT_DIR}/ops_vs_n.png", dpi=200)
plt.close()

plt.figure(figsize=(10, 6))
for (algo, kind), group in df.groupby(["algorithm", "graph"]):
    plt.semilogx(group["n"], group["scans_per_edge"], marker=MARKERS[algo], markersize=SIZES[algo], linestyle=STYLES[kind], alpha=0.8, label=f"{algo} ({kind})")
plt.xlabel("Number of vertices n")
plt.ylabel("Edge scans / m")
plt.title("Scans per edge")
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
plt.tight_layout()
plt.savefig(f"{PLOT_DIR}/scans_per_edge.png", dpi=200)
plt.close()

plt.figure(figsize=(10, 6))
for kind, group in df[df["algorithm"] == "Kruskal"].groupby("graph"):
    plt.semilogx(group["n"], group["scans_per_edge"], marker="s", linewidth=2, label=f"Kruskal ({kind})")
plt.xlabel("Number of vertices n")
plt.ylabel("Edge scans / m")
plt.title("Share of edges Kruskal examines")
plt.ylim(0, 1.05)
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend()
plt.tight_layout()
plt.savefig(f"{PLOT_DIR}/kruskal_edge_share.png", dpi=200)
plt.close()

slopes = []
for (algo, kind), group in df.groupby(["algorithm", "graph"]):
    slope = np.polyfit(np.log10(group["n"]), np.log10(group["edge_scans"]), 1)[0]
    slopes.append({"algorithm": algo, "graph": kind, "slope": round(float(slope), 3)})

slopes_df = pd.DataFrame(slopes)
slopes_df.to_csv("results/slopes.csv", index=False)

ratios = df[["algorithm", "graph", "n", "m"]].copy()
ratios["ratio"] = np.nan
ratios["formula"] = ""

for i, r in df.iterrows():
    a = r["algorithm"]
    if a == "Kruskal":
        ratios.at[i, "ratio"] = r["dsu_ops"] / r["m"]
        ratios.at[i, "formula"] = "dsu_ops / E"
    elif a == "Prim":
        ratios.at[i, "ratio"] = r["comparisons"] / (r["m"] * np.log2(r["n"]))
        ratios.at[i, "formula"] = "comparisons / (E log2 V)"
    else:
        ratios.at[i, "ratio"] = r["edge_scans"] / (r["n"] + r["m"])
        ratios.at[i, "formula"] = "edge_scans / (V + E)"

ratios["ratio"] = ratios["ratio"].round(4)
ratios.to_csv("results/ratios.csv", index=False)

kr = df[df["algorithm"] == "Kruskal"].copy()
kr["comparisons_over_elogE"] = (kr["comparisons"] / (kr["m"] * np.log2(kr["m"]))).round(4)
kr["scans_over_E"] = kr["scans_per_edge"].round(4)
kr["dsu_ops_over_E"] = (kr["dsu_ops"] / kr["m"]).round(4)
kr["dsu_ops_per_scan"] = (kr["dsu_ops"] / kr["edge_scans"]).round(4)
kr[["graph", "n", "m", "scans_over_E", "dsu_ops_over_E", "dsu_ops_per_scan", "comparisons_over_elogE"]].to_csv(
    "results/kruskal_ratios.csv", index=False
)

print("Slopes of edge_scans (log-log fit)")
print(slopes_df.to_string(index=False))
print()
print("Counter / formula")
print(ratios.pivot_table(index=["algorithm", "graph"], columns="n", values="ratio").to_string())
print()
print("Kruskal")
print(kr[["graph", "n", "m", "scans_over_E", "dsu_ops_over_E", "dsu_ops_per_scan", "comparisons_over_elogE"]].to_string(index=False))
