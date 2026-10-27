# This fork

[Clean Your TV](https://github.com/Gathin23/cleanyourtv) with a few launcher changes, set up on a TCL 55C6K.

- **Chromecast keep-alive** (`CastKeepAlive`): Home binds the Cast receiver so Android 14's freezer leaves it
  alone when the stock Google TV launcher is disabled.
- **Weather** (`Weather`): one line under the top-right buttons, e.g. `NAIROBI · 17° 🌦`, from Open-Meteo (no key),
  at most once an hour while Home is on screen. Set `weather_latitude`, `weather_longitude` and `weather_place` in
  `launcher/app/src/main/res/values/config.xml`; an empty latitude turns it off. This adds the INTERNET permission.
- **F1 line**: `race_always_on` keeps the race panel up between weekends, and `race_one_line` makes it one small
  line under the weather that alternates the next session's countdown and the race time.
- The weather and F1 lines sit on the right under the top buttons, so the clock side stays light.

The TV's list and theme are in [`examples/tcl-55c6k-google-tv`](examples/tcl-55c6k-google-tv/README.md).
