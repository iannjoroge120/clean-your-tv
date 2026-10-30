package com.gathin.tvhome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * One live football score under the F1 line, e.g. "⚽ CHE 1–0 MUN · 67'", from ESPN's public scoreboard (no key,
 * no account; unofficial, so a changed feed only hides the line). config.xml football_leagues, e.g.
 * "eng.1,uefa.champions"; empty keeps it off. Only matches in play are shown; with several in play, the biggest:
 * Champions League first, then the number of big clubs in it, then both teams' points per game this season.
 */
final class Football {
    /** Poll interval while a match is in play or about to start, and otherwise. */
    static final long LIVE_MS = 60 * 1000L;
    static final long IDLE_MS = 15 * 60 * 1000L;
    private static final Duration SOON = Duration.ofMinutes(15);
    private static final int TIMEOUT_MS = 8000;
    private static final int MAX_BYTES = 1024 * 1024;

    private static final Set<String> BIG_CLUBS = new HashSet<>(Arrays.asList(
            "arsenal", "chelsea", "liverpool", "manchester city", "manchester united", "tottenham hotspur",
            "real madrid", "barcelona", "bayern munich", "paris saint-germain", "internazionale", "juventus",
            "ac milan", "borussia dortmund", "atletico madrid"));

    /** line: the score to show, or null when nothing is in play. busy: poll at LIVE_MS. */
    static final class Result {
        final String line;
        final boolean busy;

        Result(String line, boolean busy) {
            this.line = line;
            this.busy = busy;
        }
    }

    private Football() {
    }

    /** Null when every league failed to load (the caller keeps what it showed and tries again later). */
    static Result fetch(String leagues) {
        String best = null;
        double bestRank = -1;
        boolean busy = false, anyLoaded = false;
        Instant now = Instant.now();
        for (String league : leagues.split(",")) {
            league = league.trim();
            if (league.isEmpty()) continue;
            JSONObject board = load("https://site.api.espn.com/apis/site/v2/sports/soccer/" + league + "/scoreboard");
            if (board == null) continue;
            anyLoaded = true;
            JSONArray events = board.optJSONArray("events");
            if (events == null) continue;
            boolean cup = league.startsWith("uefa.");
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.optJSONObject(i);
                if (event == null) continue;
                JSONObject match = event.optJSONArray("competitions") == null ? null
                        : event.optJSONArray("competitions").optJSONObject(0);
                if (match == null) continue;
                JSONObject status = match.optJSONObject("status");
                JSONObject type = status == null ? null : status.optJSONObject("type");
                if (type == null) continue;
                String state = type.optString("state");
                if ("pre".equals(state)) {
                    try {
                        Instant kickoff = Instant.parse(withSeconds(event.optString("date")));
                        if (Duration.between(now, kickoff).compareTo(SOON) <= 0) busy = true;
                    } catch (Exception ignored) {
                        // an unreadable date only means no early switch to the faster poll
                    }
                    continue;
                }
                if (!"in".equals(state)) continue;
                busy = true;
                JSONArray sides = match.optJSONArray("competitors");
                if (sides == null || sides.length() != 2) continue;
                JSONObject home = side(sides, "home"), away = side(sides, "away");
                if (home == null || away == null) continue;
                double rank = (cup ? 100 : 0) + 10 * (big(home) + big(away)) + pointsPerGame(home) + pointsPerGame(away);
                if (rank <= bestRank) continue;
                bestRank = rank;
                String minute = type.optString("name").contains("HALFTIME") ? "HT" : status.optString("displayClock");
                best = "⚽  " + abbr(home) + " " + home.optString("score", "0") + "–"
                        + away.optString("score", "0") + " " + abbr(away) + (minute.isEmpty() ? "" : "  ·  " + minute);
            }
        }
        return anyLoaded ? new Result(best, busy) : null;
    }

    private static JSONObject side(JSONArray sides, String homeAway) {
        for (int i = 0; i < sides.length(); i++) {
            JSONObject s = sides.optJSONObject(i);
            if (s != null && homeAway.equals(s.optString("homeAway"))) return s;
        }
        return null;
    }

    private static String abbr(JSONObject side) {
        JSONObject team = side.optJSONObject("team");
        return team == null ? "?" : team.optString("abbreviation", team.optString("shortDisplayName", "?"));
    }

    private static int big(JSONObject side) {
        JSONObject team = side.optJSONObject("team");
        if (team == null) return 0;
        String name = Normalizer.normalize(team.optString("displayName"), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return BIG_CLUBS.contains(name) ? 1 : 0;
    }

    /** From the season record "W-D-L"; 0 when there is none. */
    private static double pointsPerGame(JSONObject side) {
        JSONArray records = side.optJSONArray("records");
        if (records == null || records.length() == 0) return 0;
        String[] wdl = records.optJSONObject(0) == null ? new String[0]
                : records.optJSONObject(0).optString("summary").split("-");
        if (wdl.length != 3) return 0;
        try {
            int w = Integer.parseInt(wdl[0].trim()), d = Integer.parseInt(wdl[1].trim()), l = Integer.parseInt(wdl[2].trim());
            int games = w + d + l;
            return games == 0 ? 0 : (3.0 * w + d) / games;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** ESPN writes "2026-10-31T12:30Z"; Instant.parse wants seconds. */
    private static String withSeconds(String date) {
        return date.matches(".*T\\d{2}:\\d{2}Z") ? date.replace("Z", ":00Z") : date;
    }

    private static JSONObject load(String url) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(TIMEOUT_MS);
            c.setReadTimeout(TIMEOUT_MS);
            if (c.getResponseCode() != 200) return null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) > 0; ) {
                    out.write(buf, 0, n);
                    if (out.size() > MAX_BYTES) return null;
                }
            }
            return new JSONObject(out.toString("UTF-8"));
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }
}
