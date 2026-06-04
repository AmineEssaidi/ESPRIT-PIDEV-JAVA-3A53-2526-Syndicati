package com.syndicati.utils.concurrent;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Small JavaFX-safe async helper.
 * Blocking IO goes to virtual threads; UI mutations are marshalled back to the FX thread.
 */
public final class FxAsync {
    private static final ExecutorService IO_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final ExecutorService CPU_EXECUTOR = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
        daemonFactory("SyndicatiCpu")
    );

    private FxAsync() {}

    public static <T> CompletableFuture<T> supplyIo(ThrowingSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(wrap(supplier), IO_EXECUTOR);
    }

    public static CompletableFuture<Void> runIo(ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, IO_EXECUTOR);
    }

    public static <T> CompletableFuture<T> supplyCpu(ThrowingSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(wrap(supplier), CPU_EXECUTOR);
    }

    public static void onFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    public static <T> void onFx(CompletableFuture<T> future, Consumer<T> success, Consumer<Throwable> failure) {
        future.whenComplete((value, error) -> onFx(() -> {
            if (error == null) {
                success.accept(value);
            } else {
                failure.accept(error);
            }
        }));
    }

    public static String message(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }

    public static void shutdown() {
        IO_EXECUTOR.shutdownNow();
        CPU_EXECUTOR.shutdownNow();
    }

    private static <T> Supplier<T> wrap(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    private static ThreadFactory daemonFactory(String prefix) {
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + "-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        };
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
