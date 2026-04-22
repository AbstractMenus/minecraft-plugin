package ru.abstractmenus.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark: String.split() vs indexOf/substring for parsing placeholders
 * like "val:myVar.player1" and file extensions.
 *
 * Context in the codebase:
 * - VarPlaceholders: split(":") + split("\\.") on EVERY placeholder
 * - FileUtils.getExtension: split("\\.") on every file at load time
 *
 * split() compiles a regex and allocates a new array on each call.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class StringSplitBenchmark {

    private String varPlaceholder;
    private String fileName;

    @Setup
    public void setup() {
        varPlaceholder = "val:myVariable.playerName";
        fileName = "my_cool_menu.conf";
    }

    // === Placeholder parsing: current (split) ===

    @Benchmark
    public void varParse_split(Blackhole bh) {
        String[] arr = varPlaceholder.split(":");
        if (arr.length > 1) {
            String[] data = arr[1].split("\\.");
            bh.consume(data);
        }
    }

    // === Placeholder parsing: optimized (indexOf) ===

    @Benchmark
    public void varParse_indexOf(Blackhole bh) {
        int colonIdx = varPlaceholder.indexOf(':');
        if (colonIdx != -1 && colonIdx < varPlaceholder.length() - 1) {
            String rest = varPlaceholder.substring(colonIdx + 1);
            int dotIdx = rest.indexOf('.');
            if (dotIdx != -1) {
                String part1 = rest.substring(0, dotIdx);
                String part2 = rest.substring(dotIdx + 1);
                bh.consume(part1);
                bh.consume(part2);
            } else {
                bh.consume(rest);
            }
        }
    }

    // === File extension: current (split with regex) ===

    @Benchmark
    public void fileExt_split(Blackhole bh) {
        String[] arr = fileName.split("\\.");
        String ext = (arr.length > 0) ? arr[arr.length - 1] : null;
        bh.consume(ext);
    }

    // === File extension: optimized (lastIndexOf) ===

    @Benchmark
    public void fileExt_lastIndexOf(Blackhole bh) {
        int idx = fileName.lastIndexOf('.');
        String ext = (idx != -1) ? fileName.substring(idx + 1) : null;
        bh.consume(ext);
    }

    // === Batch: simulate 54 placeholder parses (one menu refresh) ===

    @Benchmark
    public void varParse_split_batch54(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            String[] arr = varPlaceholder.split(":");
            if (arr.length > 1) {
                bh.consume(arr[1].split("\\."));
            }
        }
    }

    @Benchmark
    public void varParse_indexOf_batch54(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            int colonIdx = varPlaceholder.indexOf(':');
            if (colonIdx != -1 && colonIdx < varPlaceholder.length() - 1) {
                String rest = varPlaceholder.substring(colonIdx + 1);
                int dotIdx = rest.indexOf('.');
                if (dotIdx != -1) {
                    bh.consume(rest.substring(0, dotIdx));
                    bh.consume(rest.substring(dotIdx + 1));
                } else {
                    bh.consume(rest);
                }
            }
        }
    }
}
