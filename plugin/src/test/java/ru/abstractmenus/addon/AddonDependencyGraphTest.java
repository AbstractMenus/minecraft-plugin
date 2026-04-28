package ru.abstractmenus.addon;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddonDependencyGraphTest {

    @Test
    void empty_returnsEmpty() {
        List<String> order = AddonDependencyGraph.topoSort(Map.of());
        assertEquals(List.of(), order);
    }

    @Test
    void noDependencies_preservesInsertionOrder() {
        Map<String, List<String>> deps = new LinkedHashMap<>();
        deps.put("a", List.of());
        deps.put("b", List.of());
        deps.put("c", List.of());

        assertEquals(List.of("a", "b", "c"), AddonDependencyGraph.topoSort(deps));
    }

    @Test
    void linearChain_sortsDeepToShallow() {
        // c depends on b; b depends on a → enable order a, b, c
        Map<String, List<String>> deps = new LinkedHashMap<>();
        deps.put("c", List.of("b"));
        deps.put("b", List.of("a"));
        deps.put("a", List.of());

        assertEquals(List.of("a", "b", "c"), AddonDependencyGraph.topoSort(deps));
    }

    @Test
    void diamond_isHandled() {
        //     a
        //   /   \
        //  b     c
        //   \   /
        //     d
        Map<String, List<String>> deps = new LinkedHashMap<>();
        deps.put("d", List.of("b", "c"));
        deps.put("c", List.of("a"));
        deps.put("b", List.of("a"));
        deps.put("a", List.of());

        List<String> order = AddonDependencyGraph.topoSort(deps);

        assertTrue(order.indexOf("a") < order.indexOf("b"));
        assertTrue(order.indexOf("a") < order.indexOf("c"));
        assertTrue(order.indexOf("b") < order.indexOf("d"));
        assertTrue(order.indexOf("c") < order.indexOf("d"));
    }

    @Test
    void selfCycle_throws() {
        Map<String, List<String>> deps = Map.of("a", List.of("a"));
        AddonDependencyCycleException ex = assertThrows(
                AddonDependencyCycleException.class,
                () -> AddonDependencyGraph.topoSort(deps));
        assertTrue(ex.getMessage().contains("a"));
    }

    @Test
    void twoNodeCycle_throws() {
        Map<String, List<String>> deps = Map.of(
                "a", List.of("b"),
                "b", List.of("a"));
        assertThrows(AddonDependencyCycleException.class,
                () -> AddonDependencyGraph.topoSort(deps));
    }

    @Test
    void unsatisfied_returnsAddonsWithMissingDeps() {
        Map<String, List<String>> deps = new LinkedHashMap<>();
        deps.put("a", List.of("ghost"));   // ghost not in graph
        deps.put("b", List.of("a"));       // a IS in graph but transitively bad
        deps.put("c", List.of());

        // Both a (direct miss) and b (transitive miss through a) must be
        // flagged. A single-pass implementation would only catch a and let
        // b leak into topoSort.
        Set<String> bad = AddonDependencyGraph.unsatisfied(deps);
        assertEquals(Set.of("a", "b"), bad);
    }

    @Test
    void unsatisfied_transitiveChain() {
        Map<String, List<String>> deps = new LinkedHashMap<>();
        deps.put("d", List.of("c"));
        deps.put("c", List.of("b"));
        deps.put("b", List.of("ghost"));
        deps.put("a", List.of());

        Set<String> bad = AddonDependencyGraph.unsatisfied(deps);
        assertEquals(Set.of("b", "c", "d"), bad);
    }

    @Test
    void unsatisfied_emptyForCleanGraph() {
        Map<String, List<String>> deps = Map.of(
                "a", List.of(),
                "b", List.of("a"));
        assertTrue(AddonDependencyGraph.unsatisfied(deps).isEmpty());
    }
}
