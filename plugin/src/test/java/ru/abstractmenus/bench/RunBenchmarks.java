package ru.abstractmenus.bench;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Entry point for the JMH benchmarks.
 *
 * Run everything:
 *   ./gradlew jmh
 *
 * Run a single benchmark:
 *   ./gradlew jmh -Pbench=PlaceholderReplace
 *
 * Available benchmarks:
 * - PlaceholderReplaceBenchmark — String.replace() loop vs StringBuilder
 * - StringSplitBenchmark — split() vs indexOf for placeholder parsing
 * - SlotContainsBenchmark — HashSet allocation vs direct iteration
 * - ItemBuildBenchmark — deep-cloning LinkedHashMap vs direct access
 * - ConcurrencyBenchmark — HashMap vs ConcurrentHashMap under different loads
 */
public class RunBenchmarks {

    public static void main(String[] args) throws RunnerException {
        String include = args.length > 0 ? args[0] : "ru.abstractmenus.bench.*";

        Options opt = new OptionsBuilder()
                .include(include)
                .build();

        new Runner(opt).run();
    }
}
