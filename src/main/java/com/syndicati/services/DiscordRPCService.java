package com.syndicati.services;

import javafx.application.Platform;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Discord Rich Presence via PowerShell subprocess.
 * Avoids ANY Java-side named pipe access (which causes JVM crashes on this system).
 * Uses .NET NamedPipeClientStream inside PowerShell — the safe Windows-native approach.
 */
public class DiscordRPCService {

    private static DiscordRPCService instance;
    private final String applicationId = "1500782674531455037";

    private volatile boolean ready       = false;
    private volatile boolean initialized = false;

    private long         startTimestamp;
    private Process      psProcess;
    private PrintWriter  psIn;
    private Thread       pipeThread;

    private String pendingDetails = "Community Portal";
    private String pendingState   = "Browsing Syndicati";

    private DiscordRPCService() {
        startTimestamp = System.currentTimeMillis() / 1000L;
    }

    public static synchronized DiscordRPCService getInstance() {
        if (instance == null) instance = new DiscordRPCService();
        return instance;
    }

    // -------------------------------------------------------------------------

    public void initialize() {
        if (initialized) return;
        initialized = true;
        System.out.println("[DiscordRPC] Starting via PowerShell IPC bridge...");

        pipeThread = Thread.ofPlatform()
            .daemon(true)
            .name("DiscordRPC-PS")
            .unstarted(this::launchBridge);
        pipeThread.start();
    }

    public void updatePresence(String details, String state) {
        pendingDetails = details;
        pendingState   = state;
        if (!ready || psIn == null) return;
        sendPresence(details, state);
    }

    public void shutdown() {
        initialized = false;
        ready       = false;
        if (psIn != null) { psIn.println("QUIT"); psIn.flush(); }
        if (psProcess != null) psProcess.destroyForcibly();
        if (pipeThread != null) pipeThread.interrupt();
        System.out.println("[DiscordRPC] Shutdown.");
    }

    // -------------------------------------------------------------------------

    private void launchBridge() {
        try {
            // Write PowerShell bridge script to a temp file
            File script = File.createTempFile("syndicati_discord_rpc_", ".ps1");
            script.deleteOnExit();

            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(script), StandardCharsets.UTF_8))) {
                w.println(buildPsScript());
            }

            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-File", script.getAbsolutePath(),
                applicationId
            );
            pb.redirectErrorStream(false);
            psProcess = pb.start();

            // Read lines from PS stdout
            BufferedReader out = new BufferedReader(
                new InputStreamReader(psProcess.getInputStream(), StandardCharsets.UTF_8));
            psIn = new PrintWriter(new OutputStreamWriter(
                psProcess.getOutputStream(), StandardCharsets.UTF_8), true);

            String line;
            while ((line = out.readLine()) != null && initialized) {
                if (line.startsWith("READY")) {
                    ready = true;
                    System.out.println("[DiscordRPC] ✅ Ready via PS bridge!");
                    sendPresence(pendingDetails, pendingState);
                } else if (line.startsWith("ERROR")) {
                    System.err.println("[DiscordRPC] " + line);
                } else {
                    System.out.println("[DiscordRPC] " + line);
                }
            }
        } catch (Throwable t) {
            System.out.println("[DiscordRPC] PS bridge unavailable: " + t.getMessage());
        } finally {
            ready = false;
        }
    }

    private void sendPresence(String details, String state) {
        if (psIn == null) return;
        try {
            String nonce = UUID.randomUUID().toString();
            // Send a compact JSON line — PS reads it via ReadLine and forwards to Discord
            String json = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" +
                ProcessHandle.current().pid() +
                ",\"activity\":{\"details\":\"" + esc(details) + "\"" +
                ",\"state\":\"" + esc(state) + "\"" +
                ",\"timestamps\":{\"start\":" + startTimestamp + "}" +
                ",\"assets\":{\"large_image\":\"logo\",\"large_text\":\"Syndicati\"}" +
                "}},\"nonce\":\"" + nonce + "\"}";
            psIn.println(json);
            psIn.flush();
            System.out.println("[DiscordRPC] Pushed: " + details + " | " + state);
        } catch (Throwable t) {
            System.err.println("[DiscordRPC] Send failed: " + t.getMessage());
            ready = false;
        }
    }

    // -------------------------------------------------------------------------
    // PowerShell bridge script (uses .NET NamedPipeClientStream — no Java pipe I/O)
    // -------------------------------------------------------------------------

    private static String buildPsScript() {
        return
            "param([string]$clientId)\r\n" +
            "function Send-Frame($pipe, [int]$op, [string]$json) {\r\n" +
            "  $d = [System.Text.Encoding]::UTF8.GetBytes($json)\r\n" +
            "  $h = New-Object byte[] 8\r\n" +
            "  [System.BitConverter]::GetBytes([int]$op).CopyTo($h,0)\r\n" +
            "  [System.BitConverter]::GetBytes([int]$d.Length).CopyTo($h,4)\r\n" +
            "  $pipe.Write($h,0,8); $pipe.Write($d,0,$d.Length); $pipe.Flush()\r\n" +
            "}\r\n" +
            "function Read-Frame($pipe) {\r\n" +
            "  $h = New-Object byte[] 8\r\n" +
            "  [void]$pipe.Read($h,0,8)\r\n" +
            "  $len = [System.BitConverter]::ToInt32($h,4)\r\n" +
            "  $d = New-Object byte[] $len\r\n" +
            "  [void]$pipe.Read($d,0,$len)\r\n" +
            "  return [System.Text.Encoding]::UTF8.GetString($d)\r\n" +
            "}\r\n" +
            "for ($i=0; $i -le 9; $i++) {\r\n" +
            "  try {\r\n" +
            "    $p = New-Object System.IO.Pipes.NamedPipeClientStream('.',\"discord-ipc-$i\",[System.IO.Pipes.PipeDirection]::InOut)\r\n" +
            "    $p.Connect(2000)\r\n" +
            "    Send-Frame $p 0 \"{`\"v`\":1,`\"client_id`\":`\"$clientId`\"}\"\r\n" +
            "    $r = Read-Frame $p\r\n" +
            "    if ($r -match 'READY') {\r\n" +
            "      [Console]::Out.WriteLine('READY')\r\n" +
            "      [Console]::Out.Flush()\r\n" +
            "      while ($true) {\r\n" +
            "        $line = [Console]::In.ReadLine()\r\n" +
            "        if ($null -eq $line -or $line -eq 'QUIT') { break }\r\n" +
            "        Send-Frame $p 1 $line\r\n" +
            "        try { Read-Frame $p | Out-Null } catch {}\r\n" +
            "        [Console]::Out.WriteLine('OK')\r\n" +
            "        [Console]::Out.Flush()\r\n" +
            "      }\r\n" +
            "      $p.Close(); break\r\n" +
            "    }\r\n" +
            "    $p.Close()\r\n" +
            "  } catch { }\r\n" +
            "}\r\n";
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
