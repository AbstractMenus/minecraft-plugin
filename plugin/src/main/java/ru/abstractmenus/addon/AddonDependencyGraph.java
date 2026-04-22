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
     * Sort addons into enabling order.
     *
     * @param dependencies map from addon name → list of names it depends on.
     *                     Iteration order of the input map is preserved
     *                     among independent addons (pass a
     *                     {@link java.util.LinkedHashMap} for determinism).
     * @return addon names in dependency-first order (deps before dependants)
     * @throws AddonDependencyCycleException if a cycle is detected
     * @throws AddonDependencyException      if a declared dep refers to a
     *                                       name not present in the graph
     */
    public static List<String> topoSort(Map<String, List<String>> dependencies) {
        Set<String> known = dependencies.keySet();

        // Validation: every declared dep must exist in the graph.
        for (Map.Entry<String, List<String>> e : dependencies.entrySet()) {
            for (String dep : e.getValue()) {
                if (!known.contains(dep)) {
                    throw new AddonDependencyException(
                            "Addon '" + e.getKey() + "' depends on unknown addon '" + dep + "'");
                }
            }
        }

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
