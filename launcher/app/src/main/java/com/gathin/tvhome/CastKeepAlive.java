package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * With the stock Google TV launcher disabled, nothing keeps the Cast receiver above the cached state, and
 * Android 14's cached-app freezer stops it a few minutes after each wake: phones then find the TV slowly or not
 * at all (TCL G08, docs/TROUBLESHOOTING.md, "Casting disappears although the Cast receiver is still running").
 * Binding its CastReceiverService from Home moves it to a visible service and it stops freezing. Bound through
 * the application context so the binding outlives activity recreation; a TV without the receiver, or one
 * that refuses the bind, is left alone.
 */
final class CastKeepAlive {
    private static final ComponentName RECEIVER =
            new ComponentName("com.google.android.apps.mediashell",
                    "com.google.android.apps.mediashell.CastReceiverService");
    private static final String ACTION = "com.google.cast.action.BIND";

    private static ServiceConnection connection;

    private CastKeepAlive() {
    }

    static synchronized void bind(Context context) {
        if (connection != null) return;
        ServiceConnection c = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // The receiver died; BIND_AUTO_CREATE brings it back and reconnects on its own.
            }
        };
        Intent intent = new Intent(ACTION).setComponent(RECEIVER);
        try {
            if (context.getApplicationContext().bindService(intent, c, Context.BIND_AUTO_CREATE)) {
                connection = c;
            } else {
                context.getApplicationContext().unbindService(c);
            }
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Not exported to us on this build, or not installed: nothing to keep awake.
        }
    }
}
