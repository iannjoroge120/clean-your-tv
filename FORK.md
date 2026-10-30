# This fork

[Clean Your TV](https://github.com/Gathin23/cleanyourtv) with a few launcher changes, set up on a TCL 55C6K.

- **Chromecast keep-alive** (`CastKeepAlive`): Home binds the Cast receiver so Android 14's freezer leaves it
  alone when the stock Google TV launcher is disabled.
- **Weather** (`Weather`): one line under the top-right buttons, e.g. `NAIROBI · 17° 🌦`, from Open-Meteo (no key),
  at most once an hour while Home is on screen. Set `weather_latitude`, `weather_longitude` and `weather_place` in
  `launcher/app/src/main/res/values/config.xml`; an empty latitude turns it off. This adds the INTERNET permission.
- **F1 line**: `race_always_on` keeps the race panel up between weekends, and `race_one_line` makes it one small
  line under the weather that alternates the next session's countdown and the race time.
- **Live football** (`Football`): one line under the F1 line, e.g. `⚽ CHE 1–0 MAN · 34'`, only while an EPL or
  Champions League match is in play (the biggest one when several are), from ESPN's public scoreboard. Set
  `football_leagues` in `config.xml`; empty turns it off. Polled every minute around a match, every 15 minutes
  otherwise, only while Home or its clock is on screen.
- **Home again opens the clock** (`home_again_clock`): Home while Home is on screen, or Home twice from an app, opens
  the clock screen, which stays up until a key is pressed and keeps the TV awake (`clock_keep_screen_on`). It shows
  the live score too.
- **Google TV's button picker** is left up in `stock_home_redirect` mode, so the remote's assignable button can be
  set from Google's own list.
- **Seasonal easter eggs** (`Seasons`, `seasonal_eggs`): on Halloween, Christmas, New Year, Valentine's, Easter and
  Kenya's Madaraka, Mashujaa and Jamhuri Day the greeting turns festive ("HAPPY HALLOWEEN, IAN 🎃") and the first
  Home of the day plays a short emoji shower. `birthday` ("MM-DD") adds a birthday.
- **Gear menu**: a Bluetooth row (the TV's Remotes & accessories page). `clock_button` (a moon that opens the clock)
  and `sound_button` exist but are off on this set; Home twice opens the clock.
- The weather and F1 lines sit on the right under the top buttons, so the clock side stays light.

The TV's list and theme are in [`examples/tcl-55c6k-google-tv`](examples/tcl-55c6k-google-tv/README.md).
