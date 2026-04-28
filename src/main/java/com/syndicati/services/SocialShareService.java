package com.syndicati.services;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SocialShareService {

    /**
     * Shares an event on Facebook
     * Note: Facebook Sharer primarily uses a URL.
     */
    public void shareOnFacebook(String eventTitle, String description) {
        try {
            // Since it's a local app, we use a generic URL or the project's URL
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
            // Fallback for some systems
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
        }
    }
}
