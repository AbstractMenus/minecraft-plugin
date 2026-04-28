package ru.abstractmenus.addon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Topological sort over an addon dependency graph. Pure function — no state.
 *
 * <p>DFS-based post-order traversal. Cycle detection uses the in-progress
 * (temporary/grey) set to identify nodes revisited during an active DFS path.
 */
public final class AddonDependencyGraph {

    private AddonDependencyGraph() {}

    /**
     * Find every addon whose declared dependencies include a name that is
     * not present (or transitively unsatisfied) in the graph. Useful for
     * pre-filtering before {@link #topoSort} so a single bad addon does
     * not poison the whole batch.
     *
     * <p>Runs to a fixed point: if A depends on B and B depends on missing
     * C, both A and B are reported. A single pass would only catch B,
     * leaving A to fail later inside {@code topoSort} or {@code onEnable}.
     *
     * @param dependencies graph (same shape as {@link #topoSort})
     * @return set of addon names whose dependency closure cannot be
     *         satisfied, in iteration order of {@code dependencies}
     */
    public static Set<String> unsatisfied(Map<String, List<String>> dependencies) {
        Set<String> bad = new LinkedHashSet<>();
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<String>> e : dependencies.entrySet()) {
                if (bad.contains(e.getKey())) continue;
                for (String dep : e.getValue()) {
                    if (!dependencies.containsKey(dep) || bad.contains(dep)) {
                        bad.add(e.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);
        return bad;
    }

    /**
     * Sort addons into enabling order. Caller must ensure every dep
     * referenced is present in {@code dependencies} - use
     * {@link #unsatisfied} first to filter out bad nodes. Cycle detection
     * still throws.
     *
     * @param dependencies map from addon name → list of names it depends on.
     *                     Iteration order of the input map is preserved
     *                     among independent addons (pass a
     *                     {@link java.util.LinkedHashMap} for determinism).
     * @return addon names in dependency-first order (deps before dependants)
     * @throws AddonDependencyCycleException if a cycle is detected
     */
    public static List<String> topoSort(Map<String, List<String>> dependencies) {
        Set<String> permanent = new HashSet<>();
        Set<String> temporary = new LinkedHashSet<>();  // order preserved for cycle message
        List<String> order = new ArrayList<>();

        for (String node : dependencies.keySet()) {
            if (!permanent.contains(node)) {
                visit(node, dependencies, permanent, temporary, order);
            }
        }

        return order;
    }

    private static void visit(String node,
                              Map<String, List<String>> deps,
                              Set<String> permanent,
                              Set<String> temporary,
                              List<String> order) {
        if (permanent.contains(node)) return;
        if (temporary.contains(node)) {
            Deque<String> cycle = new ArrayDeque<>();
            boolean found = false;
            for (String n : temporary) {
                if (n.equals(node)) found = true;
                if (found) cycle.addLast(n);
            }
            cycle.addLast(node);
            throw new AddonDependencyCycleException(
                    "Addon dependency cycle: " + String.join(" -> ", cycle));
        }
        temporary.add(node);
        for (String dep : deps.getOrDefault(node, List.of())) {
            visit(dep, deps, permanent, temporary, order);
        }
        temporary.remove(node);
        permanent.add(node);
        order.add(node);
    }
}
