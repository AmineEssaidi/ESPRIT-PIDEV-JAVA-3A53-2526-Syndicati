package com.syndicati.services.observability;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Automatically manages the lifecycle of the LogAI Python worker (Anomaly Detection).
 */
public class LogAIWorkerService {

    private static LogAIWorkerService instance;
    private Process pythonProcess;
    private final LogAIConfig config;

    private LogAIWorkerService() {
        this.config = new LogAIConfig();
        if (config.isEnabled()) {
            startWorker();
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopWorker));
    }

    public static synchronized LogAIWorkerService getInstance() {
        if (instance == null) {
            instance = new LogAIWorkerService();
        }
        return instance;
    }

    public void startWorker() {
        if (pythonProcess != null && pythonProcess.isAlive()) return;

        try {
            // Kill any orphaned process on port 8001
            cleanupPort8001();

            File script = new File("workers/logai-anomaly-detection/anomaly_service.py");
            if (!script.exists()) {
                System.err.println("[LogAIWorker] Script not found: " + script.getAbsolutePath());
                return;
            }

            System.out.println("[LogAIWorker] Starting worker: " + script.getAbsolutePath());
            
            // On Windows, we use 'python' from PATH
            ProcessBuilder pb = new ProcessBuilder("python", script.getAbsolutePath());
            pb.directory(script.getParentFile()); // Run in the worker directory
            pb.redirectErrorStream(true);
            
            pythonProcess = pb.start();

            // Handle logging in a background thread
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[LogAI-Python] " + line);
                    }
                } catch (Exception ignored) {}
            }, "LogAI-Worker-Logger").start();

            // Give it a moment to bind to the port
            Thread.sleep(2000);
            System.out.println("[LogAIWorker] Initialization attempt finished.");

        } catch (Exception e) {
            System.err.println("[LogAIWorker] Failed to start: " + e.getMessage());
        }
    }

    private void cleanupPort8001() {
        try {
            // Windows specific port cleanup
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", 
                "for /f \"tokens=5\" %a in ('netstat -aon ^| findstr 8001') do taskkill /f /pid %a");
            pb.start().waitFor();
        } catch (Exception ignored) {}
    }

    public void stopWorker() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("[LogAIWorker] Shutting down...");
            pythonProcess.destroyForcibly();
            cleanupPort8001();
        }
    }
}
