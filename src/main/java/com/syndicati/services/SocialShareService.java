package com.syndicati.services;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service to handle social media sharing of events.
 */
public class SocialShareService {

    /**
     * Shares an event on Facebook
     */
    public void shareOnFacebook(String eventTitle, String description) {
        try {
            // Using the project's repository URL as the share target
            String projectUrl = "https://github.com/AmineEssaidi/ESPRIT-PIDEV-JAVA-3A53-2526-Syndicati";
            String url = "https://www.facebook.com/sharer/sharer.php?u=" + 
                         URLEncoder.encode(projectUrl, StandardCharsets.UTF_8) +
                         "&quote=" + URLEncoder.encode("Check out this event: " + eventTitle + "\n" + description, StandardCharsets.UTF_8);
            
            openBrowser(url);
        } catch (Exception e) {
            System.err.println("Error sharing on Facebook: " + e.getMessage());
        }
    }

    /**
     * Shares an event on WhatsApp
     */
    public void shareOnWhatsApp(String eventTitle, String location, String date) {
        try {
            String text = "📅 *New Event: " + eventTitle + "*\n" +
                          "📍 Location: " + location + "\n" +
                          "⏰ Date: " + date + "\n\n" +
                          "Join us via Syndicati App!";
            
            String url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            openBrowser(url);
        } catch (Exception e) {
            System.err.println("Error sharing on WhatsApp: " + e.getMessage());
        }
    }

    private void openBrowser(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            // Fallback for systems without full Desktop support
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();
            if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                runtime.exec("open " + url);
            } else {
                runtime.exec("xdg-open " + url);
            }
        }
    }
}
