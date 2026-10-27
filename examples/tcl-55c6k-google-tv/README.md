# TCL 55C6K (G08, Google TV 14)

`Smart TV Pro`, device `G08`, product `G08_4K_GB`, firmware `UTT2.250416.001 AU02`, on Wi-Fi.

| File | What it is |
|---|---|
| `debloat.txt` | The 27 packages disabled (batches 1 to 4); batch 5 is the setup wizard, for the launcher |
| `batches/` | The same list in the five batches it was applied in, screenshots taken between them |
| `theme/config.json` | The theme used on this set (redirect mode, dock, App library, sky, F1 calendar) |

## Steps used

```bash
TV_HOST=<tv ip> tools/disable.sh examples/tcl-55c6k-google-tv/batches/1-tracking.txt   # then 2, 3, 4
TV_HOST=<tv ip> tools/disable.sh examples/tcl-55c6k-google-tv/batches/5-setupwraith.txt  # Wi-Fi owner was the system uid
TV_HOST=<tv ip> INPUT_KEY_SERVICE=1 launcher/build-and-install.sh
```

Then **redirect mode**, as in [the Q7C notes](../tcl-q7c-google-tv/README.md#working-alternative-google-tv-handles-the-app-buttons):
`pm enable com.google.android.apps.tv.launcherx`, enable only `HomeKeyService` (with `AUTO_START` allowed and
`appops write-settings`), and push `theme/` with `tools/theme.sh`. With launcherx back, Google TV's profile
chooser, the remote's Netflix / YouTube / Prime buttons and the held-volume ramp all work again; launcherx
flashes for half a second on Home before the redirect. Later launcher reinstalls use `STOCK_LAUNCHER=none`.

## Notes

- `itffnd.ywrmngcu.vuvbs` was a sideloaded app with no launcher entry that started at boot and ran a foreground
  data-sync service; it came in with sideloaded streaming APKs. Disabled.
- With launcherx disabled (before redirect mode), Android 14 froze the Cast receiver; `CastKeepAlive` (Home
  binding `CastReceiverService`) kept it at `vis (service)`. In redirect mode launcherx keeps it awake anyway.
- On macOS the stock `adb` got "No route to host" (Local Network privacy); `tools/tv.py` worked.
- Everything stayed disabled across three reboots.
