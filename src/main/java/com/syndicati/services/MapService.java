package com.syndicati.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MapService {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/";
    private final OkHttpClient client;

    public MapService() {
        this.client = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();
    }

    /**
     * Geocode an address to get latitude and longitude
     */
    public double[] geocode(String address) throws IOException {
        String url = NOMINATIM_URL + "search?q=" + URLEncoder.encode(address, StandardCharsets.UTF_8) + "&format=json&limit=1";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SyndicatiApp/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            
            String body = response.body().string();
            JSONArray json = new JSONArray(body);
            if (json.length() > 0) {
                JSONObject first = json.getJSONObject(0);
                return new double[]{first.getDouble("lat"), first.getDouble("lon")};
            }
        }
        return null;
    }

    /**
     * Reverse geocode coordinates to get an address
     */
    public String reverseGeocode(double lat, double lon) throws IOException {
        String url = NOMINATIM_URL + "reverse?lat=" + lat + "&lon=" + lon + "&format=json";
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SyndicatiApp/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            
            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            String address = json.optString("display_name", "");
            if (address.isEmpty()) {
                JSONObject addrObj = json.optJSONObject("address");
                if (addrObj != null) {
                    address = addrObj.optString("city", addrObj.optString("town", addrObj.optString("village", "Unknown Location")));
                } else {
                    address = "Location at " + lat + ", " + lon;
                }
            }
            return address;
        }
    }
}
