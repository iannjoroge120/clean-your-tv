package com.gathin.tvhome;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * One line of current weather under the clock, e.g. "Nairobi · 24° ⛅", from Open-Meteo (no API
 * key, no account). config.xml weather_latitude / weather_longitude / weather_place; an empty latitude keeps it
 * off and nothing is fetched. Home asks at most once an hour, only while it is on screen, off the main thread.
 */
final class Weather {
    static final long REFRESH_MS = 60 * 60 * 1000L;
    private static final int TIMEOUT_MS = 8000;

    private Weather() {
    }

    /** The line to show, or null when the request failed (the caller keeps the last good line). */
    static String fetchLine(String latitude, String longitude, String place, boolean fahrenheit) {
        HttpURLConnection c = null;
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude
                    + "&current=temperature_2m,weather_code,is_day&timezone=auto"
                    + (fahrenheit ? "&temperature_unit=fahrenheit" : "");
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            if (c.getResponseCode() != 200) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] buf = new byte[4096];
                for (int n; (n = in.read(buf)) > 0 && out.size() < 64 * 1024; ) out.write(buf, 0, n);
            }
            JSONObject current = new JSONObject(out.toString("UTF-8")).getJSONObject("current");
            long temp = Math.round(current.getDouble("temperature_2m"));
            String sky = describe(current.getInt("weather_code"), current.optInt("is_day", 1) == 1);
            StringBuilder line = new StringBuilder();
            if (!place.isEmpty()) line.append(place).append("  ·  ");
            line.append(String.format(Locale.ROOT, "%d°", temp)).append("  ").append(sky);
            return line.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    /** WMO weather interpretation codes, as Open-Meteo documents them, drawn as an emoji. */
    private static String describe(int code, boolean day) {
        switch (code) {
            case 0: return day ? "\u2600\uFE0F" : "\uD83C\uDF19";                 // sun / moon
            case 1: return day ? "\uD83C\uDF24\uFE0F" : "\uD83C\uDF19";           // sun behind small cloud / moon
            case 2: return day ? "\u26C5" : "\u2601\uFE0F";                        // sun behind cloud / cloud
            case 3: return "\u2601\uFE0F";                                           // cloud
            case 45: case 48: return "\uD83C\uDF2B\uFE0F";                          // fog
            case 51: case 53: case 55: case 56: case 57:
            case 80: case 81: return "\uD83C\uDF26\uFE0F";                          // sun behind rain cloud
            case 61: case 63: case 65: case 66: case 67: case 82: return "\uD83C\uDF27\uFE0F";  // rain cloud
            case 71: case 73: case 75: case 77: case 85: case 86: return "\u2744\uFE0F";     // snowflake
            case 95: case 96: case 99: return "\u26C8\uFE0F";                        // thunder cloud
            default: return "\u2601\uFE0F";
        }
    }
}
