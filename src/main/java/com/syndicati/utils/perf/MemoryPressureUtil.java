package com.syndicati.utils.perf;

import com.syndicati.services.DatabaseService;
import com.syndicati.utils.image.ImageLoaderUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Best-effort memory pressure handling for JavaFX desktop.
 *
 * Notes:
 * - Windows Task Manager shows committed memory, which the JVM may keep reserved.
 * - However, we can still reduce growth by trimming caches and triggering GC under pressure.
 */
public final class MemoryPressureUtil {
    private MemoryPressureUtil() {}

    /** If heap used exceeds this fraction, we trigger a stronger cleanup. */
    private static final double HEAP_USED_RATIO_TRIGGER = 0.70;

    /** Minimum time between GC triggers. */
    private static final long GC_COOLDOWN_MS = 12_000;

    private static volatile long lastGcAt = 0L;

    public static void onViewDisposed() {
        // Always do cheap trims.
        try { DatabaseService.getInstance().sweepExpiredCache(); } catch (Exception ignored) {}
        try { ImageLoaderUtil.trimCache(8); } catch (Exception ignored) {}

        // Only trigger GC when the heap is actually under pressure.
        if (!isHeapUnderPressure()) return;

        long now = System.currentTimeMillis();
        if (now - lastGcAt < GC_COOLDOWN_MS) return;
        lastGcAt = now;

        // Best-effort GC. This won't always reduce Task Manager memory, but it
        // prevents unbounded growth for users with low RAM.
        try { System.gc(); } catch (Exception ignored) {}
    }

    private static boolean isHeapUnderPressure() {
        try {
            MemoryMXBean mx = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = mx.getHeapMemoryUsage();
            long used = heap.getUsed();
            long max = heap.getMax();
            if (max <= 0) return false;
            double ratio = used / (double) max;
            return ratio >= HEAP_USED_RATIO_TRIGGER;
        } catch (Exception ignored) {
            return false;
        }
    }
}

