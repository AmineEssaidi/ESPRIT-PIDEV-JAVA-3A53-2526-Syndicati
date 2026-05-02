package com.syndicati.services;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

/**
 * Service to handle Discord Rich Presence integration using com.github.Vatuu:discord-rpc.
 * This is the exact implementation used by many Java games (like Minecraft launchers).
 */
public class DiscordRPCService {

    private static DiscordRPCService instance;
    private final String applicationId = "425407036495495168";
    private boolean initialized = false;
    private long startTimestamp;
    private Thread callbackThread;
    
    private DiscordRPCService() {
        startTimestamp = System.currentTimeMillis() / 1000L;
    }

    public static synchronized DiscordRPCService getInstance() {
        if (instance == null) {
            instance = new DiscordRPCService();
        }
        return instance;
    }

    /**
     * Initialize Discord RPC on a background thread, exactly how games do it.
     */
    public void initialize() {
        if (initialized) return;

        System.out.println("[DiscordRPC] Initializing Game-style Native RPC (Vatuu)...");

        Thread t = new Thread(() -> {
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler((user) -> {
                    System.out.println("[DiscordRPC] Game SDK Ready! Connected as " + user.username + "#" + user.discriminator);
                    updatePresence("In the Lobby", "Preparing for Syndicat");
                })
                .build();
            
            // Native initialization
            DiscordRPC.discordInitialize(applicationId, handlers, true);
            initialized = true;
            
            // Push initial presence immediately for visibility
            updatePresence("In the Lobby", "Preparing for Syndicat");
            
            // Games MUST run a callback loop to process Discord events
            callbackThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && initialized) {
                    DiscordRPC.discordRunCallbacks();
                    try {
                        Thread.sleep(2000); // Check for events every 2 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "DiscordRPC-CallbackLoop");
            callbackThread.setDaemon(true);
            callbackThread.start();
            
        }, "DiscordRPC-Init");
        
        t.setDaemon(true);
        t.start();
    }

    /**
     * Update the rich presence status shown on Discord.
     * Use descriptive strings like "Browsing Dashboard" or "Authentication".
     */
    public void updatePresence(String details, String state) {
        if (!initialized) return;

        DiscordRichPresence presence = new DiscordRichPresence.Builder(state)
                .setDetails(details)
                .setStartTimestamps(startTimestamp)
                .setBigImage("logo", "Syndicati Desktop")
                .build();
        
        // This is where games push the data to the native pipe
        DiscordRPC.discordUpdatePresence(presence);
        System.out.println("[DiscordRPC] Presence Updated: " + details + " | " + state);
    }

    /**
     * Gracefully shut down the RPC connection.
     */
    public void shutdown() {
        if (initialized) {
            initialized = false;
            if (callbackThread != null) {
                callbackThread.interrupt();
            }
            DiscordRPC.discordShutdown();
            System.out.println("[DiscordRPC] Native SDK Shutdown.");
        }
    }
}
