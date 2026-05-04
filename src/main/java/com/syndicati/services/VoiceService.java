package com.syndicati.services;

import javafx.application.Platform;
import java.util.function.Consumer;

/**
 * Voice recognition service using Windows built-in Speech Recognition Engine
 * via PowerShell System.Speech assembly. No external dependencies required.
 */
public class VoiceService {
    private Consumer<String> onResultListener;
    private Runnable onStartListener;
    private Runnable onStopListener;
    private volatile boolean isListening = false;
    private Process activeProcess;

    public VoiceService() {
        // No WebView needed — uses native Windows SAPI via PowerShell
    }

    public void startListening(Consumer<String> onResult) {
        if (isListening) return;
        this.onResultListener = onResult;
        isListening = true;

        if (onStartListener != null) Platform.runLater(onStartListener::run);

        Thread.startVirtualThread(() -> {
            try {
                // Use Windows System.Speech via PowerShell (no external deps)
                String script =
                    "Add-Type -AssemblyName System.Speech; " +
                    "$r = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                    "$r.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                    "$r.SetInputToDefaultAudioDevice(); " +
                    "$r.BabbleTimeout = [System.TimeSpan]::FromSeconds(10); " +
                    "$r.InitialSilenceTimeout = [System.TimeSpan]::FromSeconds(5); " +
                    "$result = $r.Recognize(); " +
                    "if ($result -ne $null) { Write-Output $result.Text } else { Write-Output '' }; " +
                    "$r.Dispose();";

                ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script
                );
                pb.redirectErrorStream(true);
                activeProcess = pb.start();

                String text = new String(activeProcess.getInputStream().readAllBytes()).trim();
                activeProcess.waitFor();

                if (!text.isEmpty() && onResultListener != null) {
                    Platform.runLater(() -> onResultListener.accept(text));
                }
            } catch (InterruptedException ie) {
                System.out.println("[VoiceService] Listening stopped.");
            } catch (Exception e) {
                System.err.println("[VoiceService] Error: " + e.getMessage());
            } finally {
                isListening = false;
                if (onStopListener != null) Platform.runLater(onStopListener::run);
            }
        });
    }

    public void stopListening() {
        isListening = false;
        if (activeProcess != null && activeProcess.isAlive()) {
            activeProcess.destroyForcibly();
        }
        if (onStopListener != null) Platform.runLater(onStopListener::run);
    }

    public boolean isListening() {
        return isListening;
    }

    public void setOnStart(Runnable onStart) {
        this.onStartListener = onStart;
    }

    public void setOnStop(Runnable onStop) {
        this.onStopListener = onStop;
    }
}
