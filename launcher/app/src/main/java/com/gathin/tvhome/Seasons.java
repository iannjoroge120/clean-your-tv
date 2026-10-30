package com.gathin.tvhome;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Seasonal easter eggs, by date alone (config.xml seasonal_eggs): on the day the greeting becomes the season's
 * ("HAPPY HALLOWEEN, IAN 🎃") in its colour, and the first Home of the day plays a short shower of its emoji.
 * A theme's festival skin, when set, wins. config.xml birthday ("MM-DD") adds a birthday on top.
 */
final class Seasons {
    final String greeting;
    final String emoji;
    final int accent;
    /** What falls in the shower; several are mixed. */
    final String[] shower;

    private Seasons(String greeting, String emoji, int accent, String... shower) {
        this.greeting = greeting;
        this.emoji = emoji;
        this.accent = accent;
        this.shower = shower;
    }

    private static final String KENYA = "🇰🇪";

    /** The season for {@code day}, or null on an ordinary day. */
    static Seasons today(LocalDate day, String birthday) {
        MonthDay md = MonthDay.from(day);
        if (!birthday.trim().isEmpty()) {
            try {
                if (md.equals(MonthDay.parse("--" + birthday.trim()))) {
                    return new Seasons("Happy birthday", "🎂", 0xFFFF6FB5,
                            "🎈", "🎉", "🎂");
                }
            } catch (Exception ignored) {
                // a malformed birthday is simply no birthday
            }
        }
        int m = day.getMonthValue(), d = day.getDayOfMonth();
        if ((m == 12 && d == 31) || (m == 1 && d == 1)) {
            return new Seasons("Happy new year", "🎆", 0xFFFFC83D, "🎆", "✨", "🎉");
        }
        if (m == 2 && d == 14) {
            return new Seasons("Happy Valentine's", "❤️", 0xFFFF4F8B, "❤️", "💕");
        }
        LocalDate easter = easterSunday(day.getYear());
        if (!day.isBefore(easter.minusDays(2)) && !day.isAfter(easter.plusDays(1))) {
            return new Seasons("Happy Easter", "🐣", 0xFFFFD54F, "🐣", "🥚", "🌸");
        }
        if (m == 6 && d == 1) return new Seasons("Happy Madaraka Day", KENYA, 0xFF2EB872, KENYA);
        if (m == 10 && d == 20) return new Seasons("Happy Mashujaa Day", KENYA, 0xFF2EB872, KENYA);
        if (m == 10 && d >= 29) {
            return new Seasons("Happy Halloween", "🎃", 0xFFFF7A1A, "🎃", "🦇", "👻");
        }
        if (m == 12 && d == 12) return new Seasons("Happy Jamhuri Day", KENYA, 0xFF2EB872, KENYA);
        if (m == 12 && d >= 20 && d <= 26) {
            return new Seasons("Merry Christmas", "🎄", 0xFFE53935, "❄️", "❄️", "🎄");
        }
        return null;
    }

    /** Gregorian Easter Sunday (the anonymous Gregorian algorithm). */
    static LocalDate easterSunday(int year) {
        int a = year % 19, b = year / 100, c = year % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3, h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7, m = (a + 11 * h + 22 * l) / 451;
        int n = h + l - 7 * m + 114;
        return LocalDate.of(year, n / 31, (n % 31) + 1);
    }
}
