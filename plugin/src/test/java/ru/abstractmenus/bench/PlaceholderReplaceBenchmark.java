package ru.abstractmenus.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Benchmark: current placeholder-replacement implementation (String.replace
 * in a loop) vs an optimized one (StringBuilder + Matcher).
 *
 * Context in the codebase: PlaceholderDefaultHandler.replace() calls
 * String.replace("%" + placeholder + "%", value) inside a while loop.
 * Every call allocates a new String (immutable), and the "%" + placeholder
 * + "%" concatenation allocates another. At 54 items × 5 placeholders ×
 * 20 TPS that's ~5,400 replacements/sec per player.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PlaceholderReplaceBenchmark {

    private static final Pattern PATTERN = Pattern.compile("%(\\S+)%");

    // Simulated hooks — return fixed values
    private final Map<String, String> hookResults = new HashMap<>();

    // Typical strings from AbstractMenus menu configs
    private String simpleLine;
    private String heavyLine;
    private String noPlaceholders;

    @Setup
    public void setup() {
        hookResults.put("player_name", "Steve");
        hookResults.put("player_level", "42");
        hookResults.put("player_health", "20.0");
        hookResults.put("player_food", "18");
        hookResults.put("var_balance", "15000");
        hookResults.put("server_online", "127");
        hookResults.put("server_tps", "19.8");

        simpleLine = "&aPlayer: %player_name% | Level: %player_level%";
        heavyLine = "&6%player_name% &7| HP: %player_health% | Food: %player_food% | Lvl: %player_level% | Balance: %var_balance% | Online: %server_online% | TPS: %server_tps%";
        noPlaceholders = "&aThis is a simple line with no placeholders at all";
    }

    // === Current implementation (PlaceholderDefaultHandler.replace) ===

    private String replaceCurrent(String str) {
        String result = str;
        Matcher matcher = PATTERN.matcher(str);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replaced = hookResults.get(placeholder);
            // Current code: result.replace("%" + placeholder + "%", replaced)
            result = replaced != null ? result.replace("%" + placeholder + "%", replaced) : result;
        }

        return result;
    }

    // === Optimized implementation (StringBuilder + appendReplacement) ===

    private String replaceOptimized(String str) {
        Matcher matcher = PATTERN.matcher(str);
        StringBuilder sb = new StringBuilder(str.length() + 32);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replaced = hookResults.get(placeholder);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replaced != null ? replaced : matcher.group()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    // === Optimized with early exit ===

    private String replaceWithEarlyExit(String str) {
        if (str.indexOf('%') == -1) return str;  // Fast path: no placeholders

        Matcher matcher = PATTERN.matcher(str);
        StringBuilder sb = new StringBuilder(str.length() + 32);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replaced = hookResults.get(placeholder);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replaced != null ? replaced : matcher.group()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    // ===== Benchmarks =====

    @Benchmark
    public void current_simple(Blackhole bh) {
        bh.consume(replaceCurrent(simpleLine));
    }

    @Benchmark
    public void optimized_simple(Blackhole bh) {
        bh.consume(replaceOptimized(simpleLine));
    }

    @Benchmark
    public void current_heavy(Blackhole bh) {
        bh.consume(replaceCurrent(heavyLine));
    }

    @Benchmark
    public void optimized_heavy(Blackhole bh) {
        bh.consume(replaceOptimized(heavyLine));
    }

    @Benchmark
    public void current_noPlaceholders(Blackhole bh) {
        bh.consume(replaceCurrent(noPlaceholders));
    }

    @Benchmark
    public void earlyExit_noPlaceholders(Blackhole bh) {
        bh.consume(replaceWithEarlyExit(noPlaceholders));
    }

    // === Batch: simulate a 54-slot menu refresh (one string per slot) ===

    @Benchmark
    public void current_fullMenuRefresh(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            bh.consume(replaceCurrent(heavyLine));
        }
    }

    @Benchmark
    public void optimized_fullMenuRefresh(Blackhole bh) {
        for (int i = 0; i < 54; i++) {
            bh.consume(replaceOptimized(heavyLine));
        }
    }
}
